package com.sadengineer.budgetmaster.backend.entity;

/**
 * Фильтр для работы с бюджетами
 */
public enum EntityFilter {
    /**
     * Только активные бюджеты (deleteTime IS NULL)
     */
    ACTIVE,
    
    /**
     * Только удаленные бюджеты (deleteTime IS NOT NULL)
     */
    DELETED,
    
    /**
     * Все бюджеты (включая активные и удаленные)
     */
    ALL
} 