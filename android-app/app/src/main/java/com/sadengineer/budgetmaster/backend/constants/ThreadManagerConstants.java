package com.sadengineer.budgetmaster.backend.constants;

/**
 * Константы для ThreadManager
 * Содержит конфигурационные параметры и сообщения для логирования
 */
public class ThreadManagerConstants {
    
    // ========================================
    // КОНФИГУРАЦИОННЫЕ ПАРАМЕТРЫ
    // ========================================

    /**
     * Размер пула потоков для ExecutorService
     * Рекомендуется использовать количество ядер процессора
     */
    public static final int THREAD_POOL_SIZE = 4;

    // ========================================
    // СООБЩЕНИЯ ДЛЯ ЛОГИРОВАНИЯ
    // ========================================

    public static final String INFO_SHUTDOWN = "Завершение работы ThreadManager";
    public static final String INFO_SHUTDOWN_NOW = "Принудительное завершение работы ThreadManager";

}
