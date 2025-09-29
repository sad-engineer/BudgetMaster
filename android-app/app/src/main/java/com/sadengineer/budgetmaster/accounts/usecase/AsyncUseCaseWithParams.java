package com.sadengineer.budgetmaster.accounts.usecase;

import java.util.concurrent.CompletableFuture;

/**
 * Интерфейс для асинхронных Use Cases с параметрами
 * @param <Params> тип параметров
 * @param <Result> тип результата
 */
public interface AsyncUseCaseWithParams<Params, Result> {
    
    /**
     * Выполняет асинхронную операцию с параметрами
     * @param params параметры для выполнения операции
     * @return CompletableFuture с результатом
     */
    CompletableFuture<Result> executeAsync(Params params);
}
