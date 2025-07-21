pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "BudgetMaster"
include(":app")

android {
    defaultConfig {
        // Читаем версию из файла
        val versionFile = rootProject.file("android-app/VERSION")
        val versionNameFromFile = versionFile.readText().trim()
        versionName = versionNameFromFile
        // Можно также задать versionCode, если нужно
    }

    buildTypes {
        getByName("release") {
            // ...
        }
    }
    // Добавим в BuildConfig
    buildTypes.all {
        buildConfigField("String", "APP_VERSION", "\"${rootProject.file("android-app/VERSION").readText().trim()}\"")
    }
}
