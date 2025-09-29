package com.sadengineer.budgetmaster.accounts.usecase;

import java.util.concurrent.CompletableFuture;

/**
 * Интерфейс для асинхронных Use Cases
 * @param <Result> тип результата
 */
public interface AsyncUseCase<Result> {
    
    /**
     * Выполняет асинхронную операцию
     * @return CompletableFuture с результатом
     */
    CompletableFuture<Result> executeAsync();
}
