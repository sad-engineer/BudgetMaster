package repository;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import util.DateTimeUtil;

/**
 * Базовый класс для репозиториев с техническими методами работы с базой данных
 * 
 * <p>Содержит общие методы для всех репозиториев:
 * <ul>
 *   <li>Подключение к базе данных SQLite</li>
 *   <li>Универсальные CRUD операции (findAll, findByColumn, softDelete)</li>
 *   <li>Безопасная работа с соединениями</li>
 * </ul>
 * 
 * <p>Все методы используют UTF-8 кодировку для корректной работы с кириллицей.
 * 
 * @author BudgetMaster
 * @version 1.0
 */
public abstract class BaseRepository {
    protected final String dbPath;
    protected final String url;

    /**
     * Конструктор базового репозитория
     * 
     * <p>Инициализирует подключение к базе данных SQLite по указанному пути.
     * Формирует JDBC URL для подключения к SQLite.
     * 
     * @param dbPath путь к файлу базы данных SQLite (например: "budget_master.db")
     * @throws IllegalArgumentException если dbPath равен null или пустой строке
     */
    public BaseRepository(String dbPath) {
        this.dbPath = dbPath;
        this.url = "jdbc:sqlite:" + dbPath;
    }

    /**
     * Универсальный метод для поиска всех сущностей
     * 
     * <p>Выполняет SQL-запрос для получения всех записей из указанной таблицы.
     * Результаты преобразуются в объекты с помощью переданной функции маппинга.
     * Возвращает полный список всех записей без фильтрации по статусу удаления.
     * 
     * @param tableName имя таблицы для поиска (не null, не пустая строка)
     * @param mapper функция для маппинга ResultSet в сущность (не null)
     * @return список всех сущностей (может быть пустым, но не null)
     * @throws IllegalArgumentException если tableName равен null/пустой строке
     * @throws SQLException при ошибке подключения к базе данных или выполнения запроса
     */
    protected <T> List<T> findAll(String tableName, java.util.function.Function<ResultSet, T> mapper) {
        List<T> result = new ArrayList<>();
        String sql = "SELECT * FROM " + tableName;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(mapper.apply(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Универсальный метод для поиска всех сущностей с опциональным условием
     * 
     * <p>Выполняет SQL-запрос для получения записей из указанной таблицы.
     * Если указаны columnName и value, добавляет условие WHERE для фильтрации.
     * Если columnName или value равны null, возвращает все записи без фильтрации.
     * Результаты преобразуются в объекты с помощью переданной функции маппинга.
     * 
     * @param tableName имя таблицы для поиска (не null, не пустая строка)
     * @param columnName имя столбца для фильтрации (может быть null для получения всех записей)
     * @param value значение в столбце для фильтрации (может быть null для получения всех записей)
     * @param mapper функция для маппинга ResultSet в сущность (не null)
     * @return список всех сущностей (может быть пустым, но не null)
     * @throws IllegalArgumentException если tableName равен null/пустой строке
     * @throws SQLException при ошибке подключения к базе данных или выполнения запроса
     */
    protected <T> List<T> findAll(String tableName, String columnName, Object value, java.util.function.Function<ResultSet, T> mapper) {
        List<T> result = new ArrayList<>();
        
        // Формируем SQL запрос в зависимости от наличия условий
        String sql;
        boolean hasCondition = columnName != null && value != null;
        
        if (hasCondition) {
            sql = "SELECT * FROM " + tableName + " WHERE " + columnName + " = ?";
        } else {
            sql = "SELECT * FROM " + tableName;
        }
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Устанавливаем параметр только если есть условие
            if (hasCondition) {
                stmt.setObject(1, value);
            }
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(mapper.apply(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Универсальный метод для поиска сущности по столбцу и значению
     * 
     * <p>Выполняет SQL-запрос для поиска записи по указанному столбцу и значению.
     * Использует PreparedStatement для безопасного выполнения запроса.
     * Результат преобразуется в объект с помощью переданной функции маппинга.
     * 
     * @param tableName имя таблицы для поиска (не null, не пустая строка)
     * @param columnName имя столбца для поиска (не null, не пустая строка)
     * @param value значение в столбце для поиска (не null)
     * @param mapper функция для маппинга ResultSet в сущность (не null)
     * @return Optional с найденной сущностью, если найдена, иначе пустой Optional
     * @throws IllegalArgumentException если tableName или columnName равны null/пустой строке, или value равен null
     * @throws SQLException при ошибке подключения к базе данных или выполнения запроса
     */
    protected <T> Optional<T> findByColumn(String tableName, String columnName, Object value, java.util.function.Function<ResultSet, T> mapper) {
        String sql = "SELECT * FROM " + tableName + " WHERE " + columnName + " = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, value);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapper.apply(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Получение соединения с базой данных
     * 
     * <p>Создает новое соединение с SQLite и устанавливает UTF-8 кодировку
     * для корректной работы с кириллическими символами.
     * 
     * @return активное соединение с базой данных
     * @throws SQLException при ошибке подключения к базе данных
     */
    protected Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(url);
        // Устанавливаем UTF-8 кодировку для соединения
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA encoding = 'UTF-8'");
        }
        return conn;
    }

    /**
     * Получение пути к базе данных
     * 
     * @return путь к файлу базы данных SQLite
     */
    public String getDbPath() {
        return dbPath;
    }
    
    /**
     * Получение максимального значения столбца с условием
     * 
     * <p>Выполняет SQL-запрос для получения максимального значения указанного столбца
     * с опциональным условием WHERE. Не загружает все записи, только одно значение.
     * 
     * @param tableName имя таблицы (не null, не пустая строка)
     * @param columnName имя столбца для поиска максимума (не null, не пустая строка)
     * @param whereCondition условие WHERE (если не нужно - пустая строка)
     * @return максимальное значение столбца, 0 если записей нет или произошла ошибка
     */
    protected int getMaxValue(String tableName, String columnName, String whereCondition) {
        String sql = "SELECT MAX(" + columnName + ") FROM " + tableName;
        if (whereCondition != null && !whereCondition.trim().isEmpty()) {
            sql += " WHERE " + whereCondition;
        }
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Выполнение мягкого удаления сущности по ID
     * 
     * <p>Устанавливает поля delete_time = текущее время и deleted_by = указанный пользователь.
     * Запись физически не удаляется из базы данных, только помечается как удаленная.
     * Использует DateTimeUtil для правильного форматирования даты для SQLite.
     * 
     * @param tableName имя таблицы для обновления (не null, не пустая строка)
     * @param id ID сущности для удаления (положительное целое число)
     * @param deletedBy пользователь, который удалил сущность (не null, не пустая строка)
     * @return true, если удаление выполнено успешно, false если запись не найдена или произошла ошибка
     * @throws IllegalArgumentException если tableName равен null/пустой строке, id <= 0 или deletedBy равен null/пустой строке
     * @throws SQLException при ошибке подключения к базе данных или выполнения запроса
     */
    protected boolean softDelete(String tableName, Integer id, String deletedBy) {
        String sql = "UPDATE " + tableName + " SET delete_time = ?, deleted_by = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Используем DateTimeUtil для правильного форматирования даты для SQLite
            String deleteTime = DateTimeUtil.formatForSqlite(java.time.LocalDateTime.now());
            stmt.setString(1, deleteTime);
            stmt.setString(2, deletedBy);
            stmt.setInt(3, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Выполнение мягкого удаления сущности по столбцу и значению
     * 
     * <p>Устанавливает поля delete_time = текущее время и deleted_by = указанный пользователь.
     * Запись физически не удаляется из базы данных, только помечается как удаленная.
     * Использует DateTimeUtil для правильного форматирования даты для SQLite.
     * 
     * @param tableName имя таблицы для обновления (не null, не пустая строка)
     * @param columnName имя столбца для поиска записи (не null, не пустая строка)
     * @param value значение в столбце для поиска записи (не null)
     * @param deletedBy пользователь, который удалил сущность (не null, не пустая строка)
     * @return true, если удаление выполнено успешно, false если запись не найдена или произошла ошибка
     * @throws IllegalArgumentException если tableName, columnName или deletedBy равны null/пустой строке, или value равен null
     * @throws SQLException при ошибке подключения к базе данных или выполнения запроса
     */
    protected boolean softDelete(String tableName, String columnName, Object value, String deletedBy) {
        String sql = "UPDATE " + tableName + " SET delete_time = ?, deleted_by = ? WHERE " + columnName + " = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Используем DateTimeUtil для правильного форматирования даты для SQLite
            String deleteTime = DateTimeUtil.formatForSqlite(java.time.LocalDateTime.now());
            stmt.setString(1, deleteTime);
            stmt.setString(2, deletedBy);
            stmt.setObject(3, value);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }  
} 