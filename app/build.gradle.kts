plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "ir.siliksama.falhafez"
    compileSdk = 35

    defaultConfig {
        applicationId = "ir.siliksama.falhafez"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        // ⚠️ پرچمِ PREMIUM_UNLOCKED حذف شد.
        // این پرچم از متغیرِ محیطیِ CI خوانده می‌شد و اگر روی true بیلد می‌شد،
        // همهٔ کاربران سطحِ GOLD می‌گرفتند و در نتیجه تمامِ تبلیغات خاموش می‌شد.
        // سطحِ حمایت اکنون فقط از خریدِ واقعی/بازیابیِ خرید می‌آید.

        // کلید اپلیکیشن تپسل — توسط SDK از مانیفست خوانده می‌شود (auto-init).
        addManifestPlaceholders(mapOf("TapsellMediationAppKey" to "tcgrrdhdhqmccrmqjeobdfsppktsqfhdqpijdkrfmkstiersqilbhfojrjblshbosqdkrb"))

        // کلیدِ عمومیِ RSA برای راستی‌آزماییِ رسیدِ خریدِ کافه‌بازار.
        //
        // این کلید عمومی است و طبق طراحیِ Poolakey باید داخلِ APK باشد؛ پس اینکه
        // در مخزن دیده شود مشکلِ امنیتی نیست. قابلِ پیکربندی شدنش برای forkهاست:
        // هرکس نسخهٔ خودش را منتشر می‌کند باید کلیدِ برنامهٔ خودش را بدهد، بدونِ
        // دست‌زدن به کدِ کاتلین.
        //
        //   ./gradlew assembleRelease -PbazaarRsaKey=...
        //   یا متغیرِ محیطیِ BAZAAR_RSA_KEY
        val bazaarRsaKey = (project.findProperty("bazaarRsaKey") as String?)
            ?: System.getenv("BAZAAR_RSA_KEY")
            ?: "MIHNMA0GCSqGSIb3DQEBAQUAA4G7ADCBtwKBrwClfrm71TFwAJKBSejSzOG00paeF8NlWzH2jkJwzNZ4fKoVB2kExuQKlspndvbGx8CD//ZduyEX0gwhNp8l8U3jBHnPJ8Bs/vI3nlVZeQcS3sj3nqbMB49Pw2g+0tr3NqwHe/Rx2z/Dg1FfcNLojZ/6MVFd6tDei9yeKfdm9iAEJR4vWc0Vq/zTbYtvSsY2ZKqfqD8EVUFNo7oY1HgknhIb8IpEVKHozrFqOMy9Dh8CAwEAAQ=="
        buildConfigField("String", "BAZAAR_RSA_KEY", "\"$bazaarRsaKey\"")
    }

    signingConfigs {
        // Release signing: keystore via CI env vars (KEYSTORE_PATH/KEYSTORE_PASSWORD/KEY_ALIAS/KEY_PASSWORD).
        // Falls back to the debug key when none is provided (local smoke builds).
        create("release") {
            val ksPath = System.getenv("KEYSTORE_PATH")
            if (!ksPath.isNullOrBlank() && rootProject.file(ksPath).exists()) {
                storeFile = rootProject.file(ksPath)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val releaseSigning = signingConfigs.getByName("release")
            signingConfig = if (releaseSigning.storeFile?.exists() == true) {
                releaseSigning
            } else {
                signingConfigs.getByName("debug")
            }
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.lottie.compose)

    // تپسل — شبکهٔ اصلی تبلیغات (Mediation SDK، نسخهٔ پایدار از Maven Central)
    implementation(libs.tapsell)
    implementation(libs.tapsell.legacy.adapter)

    // ادیوری — شبکهٔ دومِ تبلیغات (سرویسِ نمایشِ یکتانت).
    // برخلافِ تپ‌سل، کلیدش را در زمانِ اجرا می‌گیرد: Adivery.configure(app, key)
    implementation(libs.adivery)

    // Poolakey — پرداخت درون‌برنامه‌ای کافه‌بازار
    implementation(libs.poolakey)

    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
}
