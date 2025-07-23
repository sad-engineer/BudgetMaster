package com.sadengineer.budgetmaster.backend.database;

/**
 * Провайдер для Dependency Injection базы данных
 * Позволяет подменять реализацию базы данных в runtime
 */
public class DatabaseProvider {
    
    private static DatabaseInterface database;
    
    /**
     * Устанавливает реализацию базы данных
     * @param db реализация DatabaseInterface
     */
    public static void setDatabase(DatabaseInterface db) {
        database = db;
        System.out.println("DatabaseProvider: Установлена реализация базы данных: " + db.getClass().getSimpleName());
    }
    
    /**
     * Получает текущую реализацию базы данных
     * @return реализация DatabaseInterface
     */
    public static DatabaseInterface getDatabase() {
        if (database == null) {
            // По умолчанию используем JDBC
            database = new JDBCDatabaseAdapter();
            System.out.println("DatabaseProvider: Используется JDBC адаптер по умолчанию");
        }
        return database;
    }
    
    /**
     * Проверяет, установлена ли реализация базы данных
     * @return true если реализация установлена
     */
    public static boolean isDatabaseSet() {
        return database != null;
    }
    
    /**
     * Сбрасывает текущую реализацию базы данных
     */
    public static void reset() {
        if (database != null) {
            database.close();
        }
        database = null;
        System.out.println("DatabaseProvider: Реализация базы данных сброшена");
    }
} 