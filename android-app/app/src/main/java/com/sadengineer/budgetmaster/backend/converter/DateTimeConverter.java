
package com.sadengineer.budgetmaster.backend.converter;

import androidx.room.TypeConverter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Type Converter для LocalDateTime
 */
public class DateTimeConverter {
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    @TypeConverter
    public static LocalDateTime fromString(String value) {
        return value == null ? null : LocalDateTime.parse(value, formatter);
    }
    
    @TypeConverter
    public static String dateToString(LocalDateTime date) {
        return date == null ? null : date.format(formatter);
    }
} 