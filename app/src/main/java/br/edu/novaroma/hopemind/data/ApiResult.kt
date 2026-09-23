package br.edu.novaroma.hopemind.data

import br.edu.novaroma.hopemind.data.remote.AppJson
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/** Erro com mensagem pronta para mostrar ao usuário. */
class AppError(message: String, val status: Int? = null) : Exception(message)

/**
 * Executa uma chamada à API e converte falhas em [AppError] com a mensagem do backend
 * (o NestJS devolve `message` como texto ou lista de textos).
 */
suspend fun <T> apiCall(fallback: String = "Algo deu errado. Tente novamente.", block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: HttpException) {
        val body = e.response()?.errorBody()?.string().orEmpty()
        val message = runCatching {
            when (val m = AppJson.parseToJsonElement(body).jsonObject["message"]) {
                is JsonArray -> (m.firstOrNull() as? JsonPrimitive)?.content
                is JsonPrimitive -> m.content
                else -> null
            }
        }.getOrNull()
        val text = when {
            e.code() == 429 -> "Muitas tentativas. Aguarde um minuto e tente de novo."
            !message.isNullOrBlank() -> message
            else -> fallback
        }
        Result.failure(AppError(text, e.code()))
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        Result.failure(AppError("Não foi possível conectar ao servidor. Verifique se a API do HopeMind está rodando."))
    } catch (e: Exception) {
        Result.failure(AppError(fallback))
    }
