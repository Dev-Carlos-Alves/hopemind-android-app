import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

// URL da API NestJS do HopeMind. Por padrão, a hospedada no Render (funciona sem o PC ligado).
// Para testar contra a API local durante o desenvolvimento, defina no local.properties:
//   hopemind.apiUrl=http://10.0.2.2:3000/api/          (emulador; 10.0.2.2 = "localhost do PC")
//   hopemind.apiUrl=http://<IP-do-PC>:3000/api/         (celular físico, mesma rede Wi-Fi)
// (ou altere direto no app, em "Servidor" na tela de login ou em Ajustes).
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val apiUrl: String = localProps.getProperty("hopemind.apiUrl") ?: "https://hopemind-api.onrender.com/api/"

android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        // java.time (datas das sessões) no Android 7 precisa de desugaring.
        isCoreLibraryDesugaringEnabled = true
    }

    namespace = "br.edu.novaroma.hopemind"
    compileSdk = 35
    defaultConfig {
        applicationId = "br.edu.novaroma.hopemind"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "API_BASE_URL", "\"$apiUrl\"")
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }

    // Screenshots das telas na JVM (Robolectric + Roborazzi), sem precisar de emulador:
    //   ./gradlew testDebugUnitTest --tests "*ScreenshotTest*"   → PNGs em app/build/outputs/roborazzi
    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.all { it.systemProperty("roborazzi.test.record", "true") }
    }
}

// Mantém o bytecode do Kotlin no mesmo alvo do Java (21), qualquer que seja o JDK do Android Studio.
kotlin {
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21) }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.17")
    testImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
    testImplementation("androidx.compose.ui:ui-test-junit4")
    testImplementation("io.github.takahirom.roborazzi:roborazzi:1.39.0")
    testImplementation("io.github.takahirom.roborazzi:roborazzi-compose:1.39.0")
}
