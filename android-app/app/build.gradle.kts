plugins {
    id("com.android.application")
}

android {
    namespace = "com.sadengineer.budgetmaster"
    compileSdk = 34 // или ваша версия

    defaultConfig {
        applicationId = "com.sadengineer.budgetmaster" // замените на свой
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        // Читаем версию из файла
        val versionFile = rootProject.file("VERSION")
        val versionNameFromFile = versionFile.readText().trim()
        versionName = versionNameFromFile
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            // ... другие настройки
        }
    }
    // Добавим в BuildConfig
    buildTypes.all {
        buildConfigField("String", "APP_VERSION", "\"${rootProject.file("VERSION").readText().trim()}\"")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    
    // Настройки для отображения предупреждений о deprecated API
    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-Xlint:deprecation")
    }
    packaging {
        resources {
            pickFirsts.add("META-INF/MANIFEST.MF")
            excludes.add("META-INF/*.SF")
            excludes.add("META-INF/*.DSA")
            excludes.add("META-INF/*.RSA")
        }
    }
    

}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    // Подключаем backend JAR файл
    implementation(files("libs/budgetmaster-backend-0.0.012.jar"))
    // Android SQLite (встроен в Android)
    // implementation("org.xerial:sqlite-jdbc:3.44.1.0") // Убираем SQLite JDBC
}