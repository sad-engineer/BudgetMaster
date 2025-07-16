// -*- coding: utf-8 -*-
package tools;

import model.Currency;                    
import service.CurrencyService;          
import tools.CSVDataExtractor;            

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Скрипт для загрузки данных о валютах из CSV файла в базу данных
 */
public class CurrencyDataLoader {
    
    private static final String CSV_FILE_PATH = "../resorses/currencies.csv";
    private static final String USER = "data_loader";
    

        public static void main(String[] args) {
        try {
            // Регистрируем JDBC-драйвер
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC driver not found!");
            e.printStackTrace();
            return;
        }
        
        try {
            // Создаем сервис (база данных уже существует)
            CurrencyService currencyService = new CurrencyService(USER);
            
            // Извлекаем данные из CSV с помощью CSVDataExtractor
            List<String> fieldsToExtract = new ArrayList<>();
            fieldsToExtract.add("title");
            
            System.out.println("Извлекаем данные из CSV файла...");
            List<Map<String, String>> extractedData = CSVDataExtractor.extractData(CSV_FILE_PATH, fieldsToExtract);
            
            System.out.println("Найдено " + extractedData.size() + " валют для обработки.");
            
            int processedCount = 0;
            int createdCount = 0;
            
            // Обрабатываем каждую извлеченную запись
            for (Map<String, String> row : extractedData) {
                try {
                    String title = row.get("title");
                    
                    if (title == null || title.trim().isEmpty()) {
                        System.err.println("❌ Название валюты не может быть пустым");
                        continue;
                    }
                    
                    processedCount++;
                    System.out.println("Обрабатываем валюту: " + title);
                    
                    // Вызываем currencyService.get(title) - автоматически создаст или найдет валюту
                    Currency currency = currencyService.get(title);
                    
                    if (currency != null) {
                        System.out.println("✅ Валюта '" + title + "' обработана: " + currency);
                        createdCount++;
                    } else {
                        System.err.println("❌ Ошибка при обработке валюты '" + title + "'");
                    }
                    
                } catch (Exception e) {
                    System.err.println("❌ Ошибка при обработке записи: " + row + " - " + e.getMessage());
                }
            }
            
            System.out.println("\n=== РЕЗУЛЬТАТ ОБРАБОТКИ ===");
            System.out.println("Обработано строк: " + processedCount);
            System.out.println("Обработано валют: " + createdCount);
            
            // Выводим все валюты в базе данных
            System.out.println("\n=== ВСЕ ВАЛЮТЫ В БАЗЕ ДАННЫХ ===");
            List<Currency> allCurrencies = currencyService.getAll();
            for (Currency currency : allCurrencies) {
                System.out.println(currency);
            }
            
        } catch (IOException e) {
            System.err.println("Ошибка при чтении CSV файла: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Ошибка при извлечении данных: " + e.getMessage());
            e.printStackTrace();
        }
    } 
}