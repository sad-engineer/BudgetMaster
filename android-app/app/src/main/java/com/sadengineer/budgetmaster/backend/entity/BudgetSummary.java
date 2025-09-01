package com.sadengineer.budgetmaster.backend.entity;

/**
 * Модель данных для сводки по бюджетам
 * Содержит агрегированные данные по всем бюджетам категорий в единой валюте
 */
public class BudgetSummary {
    private long totalAmount;

    /**
     * Конструктор по умолчанию
     */
    public BudgetSummary() {
        this.totalAmount = 0L;
    }

    /**
     * Конструктор с параметрами
     * @param totalAmount сумма бюджета
     */
    public BudgetSummary(long totalAmount) {
        this.totalAmount = totalAmount;
    }

    // Геттеры и сеттеры
    public long getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(long totalAmount) {
        this.totalAmount = totalAmount;
    }

    @Override
    public String toString() {
        return "BudgetSummary{" +
                "totalAmount=" + totalAmount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        BudgetSummary that = (BudgetSummary) o;
        return totalAmount == that.totalAmount;
    }

    @Override
    public int hashCode() {
        return (int) (totalAmount ^ (totalAmount >>> 32));
    }          
}