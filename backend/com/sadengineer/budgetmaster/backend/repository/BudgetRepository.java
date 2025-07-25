// -*- coding: utf-8 -*-
package com.sadengineer.budgetmaster.backend.repository;

import com.sadengineer.budgetmaster.backend.model.Budget;
import com.sadengineer.budgetmaster.backend.util.DateTimeUtil;

import java.util.*;
import static com.sadengineer.budgetmaster.backend.constants.RepositoryConstants.*;

/**
 * Репозиторий для работы с бюджетами в базе данных
 * 
 * <p>Содержит базовые CRUD операции для сущности Budget:
 * <ul>
 *   <li>Создание новых бюджетов (save)</li>
 *   <li>Чтение бюджетов по ID (findById)</li>
 *   <li>Получение всех бюджетов (findAll)</li>
 *   <li>Обновление существующих бюджетов (update)</li>
 *   <li>Мягкое удаление бюджетов (delete)</li>
 * </ul>
 * 
 * <p>Все методы работают с таблицей "budgets" и используют
 * безопасное преобразование данных через mapRowSafe.
 */
public class BudgetRepository extends BaseRepository implements Repository<Budget, Integer> {

    /**
     * Конструктор репозитория бюджетов
     * 
     * <p>Инициализирует подключение к базе данных SQLite по указанному пути.
     * 
     * @param dbPath путь к файлу базы данных SQLite (например: "budget_master.db")
     */
    public BudgetRepository(String dbPath) {
        super(dbPath);
    }

    /**
     * Мягкое удаление бюджета по ID с указанием пользователя
     * 
     * <p>Устанавливает поля delete_time = текущее время и deleted_by = указанный пользователь.
     * Запись физически не удаляется из базы данных.
     * 
     * @param id ID бюджета для удаления (положительное целое число)
     * @param deletedBy пользователь, который выполняет удаление (не null, не пустая строка)
     * @return true, если удаление выполнено успешно, false если бюджет не найден
     */
    public boolean deleteById(Integer id, String deletedBy) {
        return softDelete(TABLE_BUDGETS, id, deletedBy);
    }

    /**
     * Мягкое удаление бюджета по ID категории с указанием пользователя
     * 
     * <p>Устанавливает поля delete_time = текущее время и deleted_by = указанный пользователь.
     * Запись физически не удаляется из базы данных.
     * 
     * @param categoryId ID категории для удаления (положительное целое число)
     * @param deletedBy пользователь, который выполняет удаление (не null, не пустая строка)
     * @return true, если удаление выполнено успешно, false если бюджет не найден
     */
    public boolean deleteByCategoryId(Integer categoryId, String deletedBy) {
        return softDelete(TABLE_BUDGETS, COLUMN_CATEGORY_ID, categoryId, deletedBy);
    }
    
    /**
     * Получение всех бюджетов из базы данных
     * 
     * <p>Возвращает полный список всех бюджетов, включая как активные, так и удаленные записи.
     * Результат не фильтруется по статусу удаления.
     * 
     * @return список всех бюджетов в базе данных (может быть пустым, но не null)
     */
    @Override
    public List<Budget> findAll() {
        return connection.executeQuery("SELECT * FROM " + TABLE_BUDGETS, this::mapRowSafe);
    }
    
    /**
     * Поиск бюджета по ID категории
     * 
     * <p>Возвращает первый найденный бюджет с указанной категорией.
     * Поиск выполняется независимо от статуса удаления.
     * Если бюджет не найден, возвращает пустой Optional.
     * 
     * @param categoryId ID категории для поиска (положительное целое число)
     * @return Optional с найденным бюджетом, если найден, иначе пустой Optional
     */
    public Optional<Budget> findByCategoryId(Integer categoryId) {
        String sql = "SELECT * FROM " + TABLE_BUDGETS + " WHERE " + COLUMN_CATEGORY_ID + " = ? LIMIT 1";
        return connection.executeQuerySingle(sql, this::mapRowSafe, categoryId);
    }

    /**
     * Получение бюджетов по ID валюты
     * 
     * <p>Возвращает список всех бюджетов с указанной валютой.
     * Поиск выполняется независимо от статуса удаления.
     * 
     * @param currencyId ID валюты для поиска (положительное целое число)
     * @return список бюджетов с указанной валютой (может быть пустым, но не null)
     */
    public List<Budget> findAllByCurrencyId(Integer currencyId) {
        String sql = "SELECT * FROM " + TABLE_BUDGETS + " WHERE " + COLUMN_CURRENCY_ID + " = ?";
        return connection.executeQuery(sql, this::mapRowSafe, currencyId);
    }   

    /**
     * Поиск бюджета по уникальному идентификатору
     * 
     * <p>Возвращает бюджет независимо от статуса удаления (активный или удаленный).
     * Если бюджет не найден, возвращает пустой Optional.
     * 
     * @param id ID бюджета для поиска (положительное целое число)
     * @return Optional с найденным бюджетом, если найден, иначе пустой Optional
     */
    @Override
    public Optional<Budget> findById(Integer id) {
        String sql = "SELECT * FROM " + TABLE_BUDGETS + " WHERE " + COLUMN_ID + " = ?";
        return connection.executeQuerySingle(sql, this::mapRowSafe, id);
    }

    /**
     * Поиск бюджета по позиции
     * 
     * <p>Возвращает бюджет независимо от статуса удаления (активный или удаленный).
     * Если бюджет не найден, возвращает пустой Optional.
     * 
     * @param position позиция бюджета для поиска (положительное целое число)
     * @return бюджет, если найден, иначе null
     */
    public Optional<Budget> findByPosition(Integer position) { 
        String sql = "SELECT * FROM " + TABLE_BUDGETS + " WHERE " + COLUMN_POSITION + " = ?";
        return connection.executeQuerySingle(sql, this::mapRowSafe, position);
    }

    /**
     * Получение максимального значения позиции среди всех бюджетов
     * 
     * <p>Выполняет SQL-запрос для получения максимального значения позиции.
     * Включает как активные, так и удаленные бюджеты.
     * 
     * @return максимальная позиция, 0 если бюджетов нет
     */
    public int getMaxPosition() {
        return getMaxValue(TABLE_BUDGETS, COLUMN_POSITION, null);
    }

    /**
     * Безопасное преобразование строки ResultRow в объект Budget
     * 
     * <p>Обертка над mapRow с обработкой исключений.
     * Если при чтении данных возникает ошибка, метод возвращает null.
     * 
     * @param row ResultRow с данными из базы данных (не null)
     * @return объект Budget с заполненными полями или null при ошибке
     */
    public Budget mapRowSafe(com.sadengineer.budgetmaster.backend.database.DatabaseConnection.ResultRow row) {
        try {
            Budget budget = new Budget();
            budget.setId(row.getInt(COLUMN_ID));
            budget.setCreateTime(DateTimeUtil.parseFromSqlite(row.getString(COLUMN_CREATE_TIME)));
            budget.setUpdateTime(DateTimeUtil.parseFromSqlite(row.getString(COLUMN_UPDATE_TIME)));
            budget.setDeleteTime(DateTimeUtil.parseFromSqlite(row.getString(COLUMN_DELETE_TIME)));
            budget.setCreatedBy(row.getString(COLUMN_CREATED_BY));
            budget.setUpdatedBy(row.getString(COLUMN_UPDATED_BY));
            budget.setDeletedBy(row.getString(COLUMN_DELETED_BY));
            budget.setPosition(row.getInt(COLUMN_POSITION));
            budget.setAmount(row.getInt(COLUMN_AMOUNT));
            budget.setCurrencyId(row.getInt(COLUMN_CURRENCY_ID));
            
            // Безопасное чтение поля category_id с обработкой NULL значений
            Integer categoryId = row.getInt(COLUMN_CATEGORY_ID);
            budget.setCategoryId(categoryId);
            
            return budget;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Сохранение нового бюджета в базу данных
     * 
     * <p>Создает новую запись в таблице budgets с автоматически сгенерированным ID.
     * Все поля объекта budget сохраняются в базу данных.
     * После успешного сохранения ID объекта обновляется сгенерированным значением.
     * 
     * @param budget объект бюджета для сохранения (не null, должен содержать все обязательные поля)
     * @return объект бюджета с установленным ID (тот же объект, что и входной параметр)
     */
    @Override
    public Budget save(Budget budget) {
        String sql = "INSERT INTO " + TABLE_BUDGETS + " (amount, currency_id, category_id, position, created_by, updated_by, deleted_by, create_time, update_time, delete_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        String createTimeStr = budget.getCreateTime() != null ? DateTimeUtil.formatForSqlite(budget.getCreateTime()) : null;
        String updateTimeStr = budget.getUpdateTime() != null ? DateTimeUtil.formatForSqlite(budget.getUpdateTime()) : null;
        String deleteTimeStr = budget.getDeleteTime() != null ? DateTimeUtil.formatForSqlite(budget.getDeleteTime()) : null;
            
        long id = connection.executeInsert(sql,
            budget.getAmount(),
            budget.getCurrencyId(),
            budget.getCategoryId(),
            budget.getPosition(),
            budget.getCreatedBy(),
            budget.getUpdatedBy(),
            budget.getDeletedBy(),
            createTimeStr,
            updateTimeStr,
            deleteTimeStr
        );
        
        budget.setId((int) id);
        return budget;
    }

    /**
     * Обновление существующего бюджета в базе данных
     * 
     * <p>Обновляет все поля записи по ID бюджета.
     * Объект budget должен содержать валидный ID существующей записи.
     * Все поля записи будут заменены значениями из объекта budget.
     * 
     * @param budget объект бюджета с обновленными данными (не null, должен содержать валидный ID)
     * @return обновленный объект бюджета (тот же объект, что и входной параметр)
     */
    @Override
    public Budget update(Budget budget) {
        String sql = "UPDATE " + TABLE_BUDGETS + " SET amount=?, currency_id=?, category_id=?, position=?, created_by=?, updated_by=?, deleted_by=?, create_time=?, update_time=?, delete_time=? WHERE id=?";
        
        String createTimeStr = budget.getCreateTime() != null ? DateTimeUtil.formatForSqlite(budget.getCreateTime()) : null;
        String updateTimeStr = budget.getUpdateTime() != null ? DateTimeUtil.formatForSqlite(budget.getUpdateTime()) : null;
        String deleteTimeStr = budget.getDeleteTime() != null ? DateTimeUtil.formatForSqlite(budget.getDeleteTime()) : null;
        
        connection.executeUpdate(sql,
            budget.getAmount(),
            budget.getCurrencyId(),
            budget.getCategoryId(),
            budget.getPosition(),
            budget.getCreatedBy(),
            budget.getUpdatedBy(),
            budget.getDeletedBy(),
            createTimeStr,
            updateTimeStr,
            deleteTimeStr,
            budget.getId()
        );
        
        return budget;
    }   
} 