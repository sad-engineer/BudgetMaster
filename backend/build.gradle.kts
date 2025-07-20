plugins {
    id("java-library")
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

// Настройка sourceSets для правильной компиляции
sourceSets {
    main {
        java {
            srcDirs(".")
        }
    }
}

// Настройка для создания JAR файла
tasks.jar {
    archiveBaseName.set("budgetmaster-backend")
    archiveVersion.set("1.0.0")
    
    // Включаем все зависимости в JAR (fat JAR)
    from(configurations.runtimeClasspath.map { config ->
        config.map { if (it.isDirectory) it else zipTree(it) }
    })
    
    // Исключаем файлы подписи
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    
    // Устанавливаем main class если нужно
    manifest {
        attributes(
            "Implementation-Title" to "BudgetMaster Backend",
            "Implementation-Version" to "1.0.0"
        )
    }
}

dependencies {
    // JDBC для desktop
    implementation("org.xerial:sqlite-jdbc:3.44.1.0")
    
    // Тестирование
    testImplementation("junit:junit:4.13.2")
} 