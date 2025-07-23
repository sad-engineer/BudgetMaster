// -*- coding: utf-8 -*-
package com.sadengineer.budgetmaster.backend.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Класс для работы с датой и временем
 * 
 * <p>Содержит методы для форматирования и парсинга даты и времени в строки для SQLite.
 * 
 * <p>Все методы используют UTF-8 кодировку для корректной работы с кириллицей. 
 */
public class DateTimeUtil {
    private static final DateTimeFormatter SQLITE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    /**
     * Получает текущую дату и время
     */
    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }
    
    /**
     * Форматирует LocalDateTime в строку для SQLite
     */
    public static String formatForSqlite(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(SQLITE_FORMATTER);
    }
    
    /**
     * Парсит строку из SQLite в LocalDateTime
     */
    public static LocalDateTime parseFromSqlite(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, SQLITE_FORMATTER);
    }
} 