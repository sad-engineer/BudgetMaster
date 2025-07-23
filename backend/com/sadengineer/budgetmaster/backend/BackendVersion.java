package com.sadengineer.budgetmaster.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Класс для получения версии backend из файла version.txt
 *
 * ИСПОЛЬЗОВАНИЕ:
 * String version = BackendVersion.VERSION;
 *
 * ПРИМЕР:
 * System.out.println("Версия backend: " + BackendVersion.VERSION);
 */
public class BackendVersion {
    // Версия backend читается из файла version.txt
    public static final String VERSION = loadVersion();

    private static String loadVersion() {
        String version = "unknown";
        try (InputStream is = BackendVersion.class.getResourceAsStream("/com/sadengineer/budgetmaster/backend/VERSION")) {
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    String line = reader.readLine();
                    if (line != null && !line.isBlank()) {
                        version = line.trim();
                    }
                }
            }
        } catch (IOException e) {
            // Можно залогировать ошибку
        }
        return version;
    }

    public static void main(String[] args) {
        System.out.println("🔍 BACKEND VERSION");
        System.out.println("=".repeat(30));
        System.out.println("📦 Версия: " + VERSION);
    }
} 