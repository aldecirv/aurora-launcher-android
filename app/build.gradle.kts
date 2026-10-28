plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Keystore fixo (fornecido pelo CI via secrets). Sem ele, usa a assinatura debug local.
val auroraKeystorePath: String? = System.getenv("AURORA_KEYSTORE_PATH")

android {
    namespace = "org.aurora.launcher"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.aurora.launcher"
        minSdk = 23
        targetSdk = 35
        versionCode = 3
        versionName = "1.3"
    }

    signingConfigs {
        if (auroraKeystorePath != null) {
            create("aurora") {
                storeFile = file(auroraKeystorePath)
                storePassword = System.getenv("AURORA_KEYSTORE_PASS")
                keyAlias = System.getenv("AURORA_KEY_ALIAS")
                keyPassword = System.getenv("AURORA_KEY_PASS")
            }
        }
    }

    buildTypes {
        debug {
            // Garante a mesma assinatura em todas as builds (atualização sem desinstalar).
            if (auroraKeystorePath != null) {
                signingConfig = signingConfigs.getByName("aurora")
            }
        }
        release {
            isMinifyEnabled = false
            if (auroraKeystorePath != null) {
                signingConfig = signingConfigs.getByName("aurora")
            }
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("com.google.android.material:material:1.12.0")
}
