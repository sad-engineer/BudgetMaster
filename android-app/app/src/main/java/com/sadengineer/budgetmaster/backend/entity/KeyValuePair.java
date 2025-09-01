package com.sadengineer.budgetmaster.backend.entity;

import androidx.room.ColumnInfo;
import androidx.room.Ignore;

/**
 * Универсальный класс для хранения пар ключ-значение
 * Используется для результатов SQL запросов с группировкой
 */
public class KeyValuePair {
    
    @ColumnInfo(name = "pair_key")
    private int key;
    
    @ColumnInfo(name = "pair_value")
    private long value;
    
    // Конструктор для Room
    public KeyValuePair() {}
    
    // Конструктор с параметрами
    @Ignore
    public KeyValuePair(int key, long value) {
        this.key = key;
        this.value = value;
    }
    
    // Геттеры и сеттеры
    public int getKey() {
        return key;
    }
    
    public void setKey(int key) {
        this.key = key;
    }
    
    public long getValue() {
        return value;
    }
    
    public void setValue(long value) {
        this.value = value;
    }
}
