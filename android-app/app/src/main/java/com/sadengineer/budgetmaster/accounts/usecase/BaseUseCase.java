package com.sadengineer.budgetmaster.accounts.usecase;

/**
 * Базовый интерфейс для всех Use Cases
 * Определяет общую структуру для выполнения бизнес-операций
 */
public interface BaseUseCase {
    
    /**
     * Выполняет Use Case
     * @param <T> тип возвращаемых данных
     * @return результат выполнения операции
     */
    <T> T execute();
}
