package com.sadengineer.budgetmaster.start;

/**
 * Data class для хранения всех данных главного экрана
 * Содержит агрегированные данные из разных сервисов
 */
public class MainScreenData {
    private long totalAccountsBalance;      // 1) Общая сумма на счетах
    private long monthlyEarned;             // 2) Заработано за месяц
    private long totalSavingsBalance;       // 3) Общая сумма сбережений
    private long totalBudgetRemaining;      // 4) Общий остаток бюджета
    private long reserveAmount;             // 5) Сумма резерва
    
    // Конструктор по умолчанию
    public MainScreenData() {
        this.totalAccountsBalance = 0L;
        this.monthlyEarned = 0L;
        this.totalSavingsBalance = 0L;
        this.totalBudgetRemaining = 0L;
        this.reserveAmount = 0L;
    }
    
    // Полный конструктор
    public MainScreenData(long totalAccountsBalance, long monthlyEarned, 
                         long totalSavingsBalance, long totalBudgetRemaining, 
                         long reserveAmount) {
        this.totalAccountsBalance = totalAccountsBalance;
        this.monthlyEarned = monthlyEarned;
        this.totalSavingsBalance = totalSavingsBalance;
        this.totalBudgetRemaining = totalBudgetRemaining;
        this.reserveAmount = reserveAmount;
    }
    
    // Геттеры
    public long getTotalAccountsBalance() {
        return totalAccountsBalance;
    }
    
    public long getMonthlyEarned() {
        return monthlyEarned;
    }
    
    public long getTotalSavingsBalance() {
        return totalSavingsBalance;
    }
    
    public long getTotalBudgetRemaining() {
        return totalBudgetRemaining;
    }
    
    public long getReserveAmount() {
        return reserveAmount;
    }
    
    // Сеттеры
    public void setTotalAccountsBalance(long totalAccountsBalance) {
        this.totalAccountsBalance = totalAccountsBalance;
    }
    
    public void setMonthlyEarned(long monthlyEarned) {
        this.monthlyEarned = monthlyEarned;
    }
    
    public void setTotalSavingsBalance(long totalSavingsBalance) {
        this.totalSavingsBalance = totalSavingsBalance;
    }
    
    public void setTotalBudgetRemaining(long totalBudgetRemaining) {
        this.totalBudgetRemaining = totalBudgetRemaining;
    }
    
    public void setReserveAmount(long reserveAmount) {
        this.reserveAmount = reserveAmount;
    }
    
    /**
     * Создать копию объекта с новыми значениями
     */
    public MainScreenData copy() {
        return new MainScreenData(
            this.totalAccountsBalance,
            this.monthlyEarned,
            this.totalSavingsBalance,
            this.totalBudgetRemaining,
            this.reserveAmount
        );
    }
    
    /**
     * Создать копию с обновленными полями
     */
    public MainScreenData copyWith(
            Long totalAccountsBalance,
            Long monthlyEarned,
            Long totalSavingsBalance,
            Long totalBudgetRemaining,
            Long reserveAmount) {
        return new MainScreenData(
            totalAccountsBalance != null ? totalAccountsBalance : this.totalAccountsBalance,
            monthlyEarned != null ? monthlyEarned : this.monthlyEarned,
            totalSavingsBalance != null ? totalSavingsBalance : this.totalSavingsBalance,
            totalBudgetRemaining != null ? totalBudgetRemaining : this.totalBudgetRemaining,
            reserveAmount != null ? reserveAmount : this.reserveAmount
        );
    }
    
    @Override
    public String toString() {
        return "MainScreenData{" +
                "totalAccountsBalance=" + totalAccountsBalance +
                ", monthlyEarned=" + monthlyEarned +
                ", totalSavingsBalance=" + totalSavingsBalance +
                ", totalBudgetRemaining=" + totalBudgetRemaining +
                ", reserveAmount=" + reserveAmount +
                '}';
    }
}
