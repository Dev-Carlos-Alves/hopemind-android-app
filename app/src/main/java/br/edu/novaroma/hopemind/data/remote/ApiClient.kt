package br.edu.novaroma.hopemind.data.remote

import android.content.Context
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

val AppJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    coerceInputValues = true
}

/**
 * A API do HopeMind guarda a sessão em cookies httpOnly (access + refresh).
 * No app, o OkHttp faz o papel do navegador: este CookieJar guarda os cookies
 * em SharedPreferences para a sessão sobreviver ao fechar o app.
 */
class PersistentCookieJar(context: Context) : CookieJar {
    private val prefs = context.getSharedPreferences("hopemind.cookies", Context.MODE_PRIVATE)
    private val cache = mutableMapOf<String, Cookie>()

    init {
        prefs.all.values.filterIsInstance<String>().forEach { raw ->
            val parts = raw.split('\u0000', limit = 2)
            if (parts.size == 2) {
                parts[0].toHttpUrlOrNull()?.let { url -> Cookie.parse(url, parts[1])?.let { cache[key(it)] = it } }
            }
        }
    }

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val editor = prefs.edit()
        cookies.forEach { c ->
            val k = key(c)
            if (c.expiresAt < System.currentTimeMillis()) {
                cache.remove(k); editor.remove(k)
            } else {
                cache[k] = c; editor.putString(k, "${url.scheme}://${url.host}:${url.port}/\u0000$c")
            }
        }
        editor.apply()
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        cache.entries.removeAll { it.value.expiresAt < now }
        return cache.values.filter { it.matches(url) }
    }

    @Synchronized
    fun clear() {
        cache.clear(); prefs.edit().clear().apply()
    }

    private fun key(c: Cookie) = "${c.domain}|${c.path}|${c.name}"
}

/** Ao receber 401, tenta uma única renovação em /auth/refresh e repete a requisição. */
private class RefreshAuthenticator(private val baseUrl: String, private val cookieJar: CookieJar) : Authenticator {
    private val plain = OkHttpClient.Builder().cookieJar(cookieJar).connectTimeout(8, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).build()

    override fun authenticate(route: Route?, response: Response): Request? {
        val path = response.request.url.encodedPath
        if (path.endsWith("/auth/refresh") || path.endsWith("/auth/login") || response.priorResponse != null) return null
        val refresh = Request.Builder()
            .url(baseUrl + "auth/refresh")
            .post(ByteArray(0).toRequestBody(null))
            .build()
        val ok = runCatching { plain.newCall(refresh).execute().use { it.isSuccessful } }.getOrDefault(false)
        return if (ok) response.request.newBuilder().build() else null
    }
}

class ApiClient(context: Context, val baseUrl: String) {
    val cookies = PersistentCookieJar(context)

    private val http = OkHttpClient.Builder()
        .cookieJar(cookies)
        .authenticator(RefreshAuthenticator(baseUrl, cookies))
        .connectTimeout(8, TimeUnit.SECONDS)
        // The free-tier server "sleeps" after inactivity and can take 30-50s to wake up on
        // the first request after that; 15s used to time out mid-wake-up.
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    val api: HopeMindApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(http)
        .addConverterFactory(AppJson.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(HopeMindApi::class.java)
}
