package com.sadengineer.budgetmaster.accounts.usecase;

/**
 * Базовый интерфейс для Use Cases с параметрами
 * @param <Params> тип параметров
 * @param <Result> тип результата
 */
public interface BaseUseCaseWithParams<Params, Result> {
    
    /**
     * Выполняет Use Case с параметрами
     * @param params параметры для выполнения операции
     * @return результат выполнения операции
     */
    Result execute(Params params);
}
