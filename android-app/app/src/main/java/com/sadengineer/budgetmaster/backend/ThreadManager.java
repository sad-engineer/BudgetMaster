package com.sadengineer.budgetmaster.backend;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Менеджер потоков для централизованного управления ExecutorService
 * Предоставляет общий пул потоков для всех сервисов приложения
 */
public class ThreadManager {
    private static final String TAG = "ThreadManager";
    private static final int THREAD_POOL_SIZE = 4;
    
    private static final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    
    /**
     * Получить общий ExecutorService для всех сервисов
     * @return ExecutorService
     */
    public static ExecutorService getExecutor() {
        return executorService;
    }
    
    /**
     * Завершить работу ExecutorService
     * Должен вызываться при завершении работы приложения
     */
    public static void shutdown() {
        if (!executorService.isShutdown()) {
            Log.d(TAG, "Завершение работы ThreadManager");
            executorService.shutdown();
        }
    }
    
    /**
     * Принудительно завершить работу ExecutorService
     * Использовать только в крайних случаях
     */
    public static void shutdownNow() {
        if (!executorService.isShutdown()) {
            Log.d(TAG, "Принудительное завершение работы ThreadManager");
            executorService.shutdownNow();
        }
    }
    
    /**
     * Проверить, завершена ли работа ExecutorService
     * @return true, если ExecutorService завершен
     */
    public static boolean isShutdown() {
        return executorService.isShutdown();
    }
}
