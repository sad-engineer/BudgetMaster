package com.sadengineer.budgetmaster.backend.entity;

/**
 * Модель данных для сводки по счетам
 * Содержит агрегированные данные по всем типам счетов в единой валюте
 */
public class AccountSummary {
    private long currentAccountsAmount;
    private long savingsAccountsAmount;
    private long creditAccountsAmount;
    private long totalAmount;
    
    /**
     * Конструктор по умолчанию
     */
    public AccountSummary() {
        this.currentAccountsAmount = 0L;
        this.savingsAccountsAmount = 0L;
        this.creditAccountsAmount = 0L;
        this.totalAmount = 0L;
    }
    
    /**
     * Конструктор с параметрами
     * @param currentAccountsAmount сумма на текущих счетах
     * @param savingsAccountsAmount сумма на сберегательных счетах
     * @param creditAccountsAmount сумма на кредитных счетах
     */
    public AccountSummary(long currentAccountsAmount, long savingsAccountsAmount, long creditAccountsAmount) {
        this.currentAccountsAmount = currentAccountsAmount;
        this.savingsAccountsAmount = savingsAccountsAmount;
        this.creditAccountsAmount = creditAccountsAmount;
        this.totalAmount = currentAccountsAmount + savingsAccountsAmount + creditAccountsAmount;
    }
    
    // Геттеры и сеттеры
    public long getCurrentAccountsAmount() { 
        return currentAccountsAmount; 
    }
    
    public void setCurrentAccountsAmount(long amount) { 
        this.currentAccountsAmount = amount; 
    }
    
    public long getSavingsAccountsAmount() { 
        return savingsAccountsAmount; 
    }
    
    public void setSavingsAccountsAmount(long amount) { 
        this.savingsAccountsAmount = amount; 
    }
    
    public long getCreditAccountsAmount() { 
        return creditAccountsAmount; 
    }
    
    public void setCreditAccountsAmount(long amount) { 
        this.creditAccountsAmount = amount; 
    }
    
    public long getTotalAmount() { 
        return totalAmount; 
    }
    
    public void setTotalAmount(long amount) { 
        this.totalAmount = amount; 
    }
    
    /**
     * Пересчитать общую сумму на основе всех типов счетов
     */
    public void recalculateTotal() {
        this.totalAmount = currentAccountsAmount + savingsAccountsAmount + creditAccountsAmount;
    }
    
    @Override
    public String toString() {
        return "AccountSummary{" +
                "currentAccountsAmount=" + currentAccountsAmount +
                ", savingsAccountsAmount=" + savingsAccountsAmount +
                ", creditAccountsAmount=" + creditAccountsAmount +
                ", totalAmount=" + totalAmount +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        AccountSummary that = (AccountSummary) o;
        
        if (currentAccountsAmount != that.currentAccountsAmount) return false;
        if (savingsAccountsAmount != that.savingsAccountsAmount) return false;
        if (creditAccountsAmount != that.creditAccountsAmount) return false;
        return totalAmount == that.totalAmount;
    }
    
    @Override
    public int hashCode() {
        int result = (int) (currentAccountsAmount ^ (currentAccountsAmount >>> 32));
        result = 31 * result + (int) (savingsAccountsAmount ^ (savingsAccountsAmount >>> 32));
        result = 31 * result + (int) (creditAccountsAmount ^ (creditAccountsAmount >>> 32));
        result = 31 * result + (int) (totalAmount ^ (totalAmount >>> 32));
        return result;
    }
}
