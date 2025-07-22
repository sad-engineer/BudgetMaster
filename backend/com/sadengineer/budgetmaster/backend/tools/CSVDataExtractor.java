// -*- coding: utf-8 -*-
package com.sadengineer.budgetmaster.backend.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Универсальный класс для извлечения данных из CSV файлов
 */
public class CSVDataExtractor {
    
    /**
     * Извлекает данные из CSV файла по указанным полям
     * @param csvFilePath путь к CSV файлу
     * @param fieldsToExtract список названий полей для извлечения
     * @return список строк с извлеченными данными
     * @throws IOException если ошибка чтения файла
     */
    public static List<Map<String, String>> extractData(String csvFilePath, List<String> fieldsToExtract) throws IOException {
        List<Map<String, String>> extractedData = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            boolean isFirstLine = true;
            String[] headers = null;
            
            while ((line = reader.readLine()) != null) {
                // Пропускаем пустые строки
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                // Парсим заголовки из первой строки
                if (isFirstLine) {
                    headers = parseCSVLine(line);
                    isFirstLine = false;
                    continue;
                }
                
                // Парсим данные из строки
                String[] values = parseCSVLine(line);
                
                // Проверяем соответствие количества полей
                if (values.length != headers.length) {
                    System.err.println("Предупреждение: количество полей в строке не соответствует заголовкам. Строка: " + line);
                    continue;
                }
                
                // Извлекаем нужные поля
                Map<String, String> rowData = extractFields(headers, values, fieldsToExtract);
                if (rowData != null) {
                    extractedData.add(rowData);
                }
            }
        }
        
        return extractedData;
    }
    
    /**
     * Извлекает данные из CSV файла по указанным полям с валидацией
     * @param csvFilePath путь к CSV файлу
     * @param fieldsToExtract список названий полей для извлечения
     * @param validateFields проверять ли наличие всех указанных полей
     * @return список строк с извлеченными данными
     * @throws IOException если ошибка чтения файла
     * @throws IllegalArgumentException если не найдены указанные поля
     */
    public static List<Map<String, String>> extractData(String csvFilePath, List<String> fieldsToExtract, boolean validateFields) throws IOException {
        List<Map<String, String>> extractedData = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            boolean isFirstLine = true;
            String[] headers = null;
            
            while ((line = reader.readLine()) != null) {
                // Пропускаем пустые строки
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                // Парсим заголовки из первой строки
                if (isFirstLine) {
                    headers = parseCSVLine(line);
                    
                    // Валидируем наличие всех указанных полей
                    if (validateFields) {
                        validateFields(headers, fieldsToExtract);
                    }
                    
                    isFirstLine = false;
                    continue;
                }
                
                // Парсим данные из строки
                String[] values = parseCSVLine(line);
                
                // Проверяем соответствие количества полей
                if (values.length != headers.length) {
                    System.err.println("Предупреждение: количество полей в строке не соответствует заголовкам. Строка: " + line);
                    continue;
                }
                
                // Извлекаем нужные поля
                Map<String, String> rowData = extractFields(headers, values, fieldsToExtract);
                if (rowData != null) {
                    extractedData.add(rowData);
                }
            }
        }
        
        return extractedData;
    }
    
    /**
     * Извлекает данные из CSV файла по индексам полей
     * @param csvFilePath путь к CSV файлу
     * @param fieldIndexes список индексов полей для извлечения (начиная с 0)
     * @return список строк с извлеченными данными
     * @throws IOException если ошибка чтения файла
     */
    public static List<List<String>> extractDataByIndexes(String csvFilePath, List<Integer> fieldIndexes) throws IOException {
        List<List<String>> extractedData = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                // Пропускаем пустые строки
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                // Пропускаем заголовок
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                // Парсим данные из строки
                String[] values = parseCSVLine(line);
                
                // Извлекаем нужные поля по индексам
                List<String> rowData = extractFieldsByIndexes(values, fieldIndexes);
                if (rowData != null) {
                    extractedData.add(rowData);
                }
            }
        }
        
        return extractedData;
    }
    
    /**
     * Получает заголовки CSV файла
     * @param csvFilePath путь к CSV файлу
     * @return массив заголовков
     * @throws IOException если ошибка чтения файла
     */
    public static String[] getHeaders(String csvFilePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line = reader.readLine();
            if (line != null && !line.trim().isEmpty()) {
                return parseCSVLine(line);
            }
        }
        return new String[0];
    }
    
    /**
     * Разбирает строку CSV, учитывая кавычки
     * @param csvLine строка CSV
     * @return массив полей
     */
    private static String[] parseCSVLine(String csvLine) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < csvLine.length(); i++) {
            char c = csvLine.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString().trim());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        
        // Добавляем последнее поле
        fields.add(currentField.toString().trim());
        
        return fields.toArray(new String[0]);
    }
    
    /**
     * Извлекает указанные поля из строки данных
     * @param headers заголовки CSV
     * @param values значения строки
     * @param fieldsToExtract список полей для извлечения
     * @return Map с извлеченными данными или null если ошибка
     */
    private static Map<String, String> extractFields(String[] headers, String[] values, List<String> fieldsToExtract) {
        Map<String, String> rowData = new HashMap<>();
        
        try {
            for (String fieldName : fieldsToExtract) {
                int fieldIndex = findFieldIndex(headers, fieldName);
                if (fieldIndex >= 0 && fieldIndex < values.length) {
                    String value = parseString(values[fieldIndex]);
                    rowData.put(fieldName, value);
                } else {
                    System.err.println("Предупреждение: поле '" + fieldName + "' не найдено в заголовках");
                    rowData.put(fieldName, null);
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка при извлечении полей: " + e.getMessage());
            return null;
        }
        
        return rowData;
    }
    
    /**
     * Извлекает поля по индексам
     * @param values значения строки
     * @param fieldIndexes список индексов полей
     * @return список извлеченных значений или null если ошибка
     */
    private static List<String> extractFieldsByIndexes(String[] values, List<Integer> fieldIndexes) {
        List<String> rowData = new ArrayList<>();
        
        try {
            for (Integer index : fieldIndexes) {
                if (index >= 0 && index < values.length) {
                    String value = parseString(values[index]);
                    rowData.add(value);
                } else {
                    System.err.println("Предупреждение: индекс " + index + " выходит за границы массива");
                    rowData.add(null);
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка при извлечении полей по индексам: " + e.getMessage());
            return null;
        }
        
        return rowData;
    }
    
    /**
     * Находит индекс поля в заголовках
     * @param headers заголовки CSV
     * @param fieldName название поля
     * @return индекс поля или -1 если не найдено
     */
    private static int findFieldIndex(String[] headers, String fieldName) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].equalsIgnoreCase(fieldName)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Валидирует наличие всех указанных полей
     * @param headers заголовки CSV
     * @param fieldsToExtract список полей для проверки
     * @throws IllegalArgumentException если не найдены указанные поля
     */
    private static void validateFields(String[] headers, List<String> fieldsToExtract) {
        List<String> missingFields = new ArrayList<>();
        
        for (String fieldName : fieldsToExtract) {
            if (findFieldIndex(headers, fieldName) == -1) {
                missingFields.add(fieldName);
            }
        }
        
        if (!missingFields.isEmpty()) {
            throw new IllegalArgumentException("Не найдены следующие поля в CSV: " + missingFields);
        }
    }
    
    /**
     * Парсит строку, убирая кавычки
     * @param value строка для парсинга
     * @return строка без кавычек или null если строка пустая
     */
    private static String parseString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String trimmed = value.trim();
        // Убираем кавычки в начале и конце
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed.isEmpty() ? null : trimmed;
    }
    
    /**
     * Пример использования класса
     */
    public static void main(String[] args) {
        try {
            // Пример 1: Извлечение по названиям полей
            String csvFile = "../resorses/currencies.csv";
            List<String> fields = new ArrayList<>();
            fields.add("title");
            fields.add("position");
            
            System.out.println("=== Извлечение данных по названиям полей ===");
            List<Map<String, String>> data1 = extractData(csvFile, fields);
            for (Map<String, String> row : data1) {
                System.out.println("Title: " + row.get("title") + ", Position: " + row.get("position"));
            }
            
            // Пример 2: Извлечение по индексам
            List<Integer> indexes = new ArrayList<>();
            indexes.add(8); // title
            indexes.add(7); // position
            
            System.out.println("\n=== Извлечение данных по индексам ===");
            List<List<String>> data2 = extractDataByIndexes(csvFile, indexes);
            for (List<String> row : data2) {
                System.out.println("Title: " + row.get(0) + ", Position: " + row.get(1));
            }
            
            // Пример 3: Получение заголовков
            System.out.println("\n=== Заголовки CSV файла ===");
            String[] headers = getHeaders(csvFile);
            for (int i = 0; i < headers.length; i++) {
                System.out.println(i + ": " + headers[i]);
            }
            
        } catch (IOException e) {
            System.err.println("Ошибка при работе с файлом: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 