// -*- coding: utf-8 -*-
package com.sadengineer.budgetmaster.backend.repository;

import com.sadengineer.budgetmaster.backend.model.Currency;
import com.sadengineer.budgetmaster.backend.util.DateTimeUtil;
import java.util.*;
import static com.sadengineer.budgetmaster.backend.constants.RepositoryConstants.*;

/**
 * Репозиторий для работы с валютами в базе данных
 * 
 * <p>Содержит базовые CRUD операции для сущности Currency:
 * <ul>
 *   <li>Создание новых валют (save)</li>
 *   <li>Чтение валют по ID или названию (findById, findByTitle)</li>
 *   <li>Получение всех валют (findAll)</li>
 *   <li>Обновление существующих валют (update)</li>
 *   <li>Мягкое удаление валют (delete)</li>
 * </ul>
 * 
 * <p>Все методы работают с таблицей "currencies" и используют
 * безопасное преобразование данных через mapRowSafe.
 */
public class CurrencyRepository extends BaseRepository implements Repository<Currency, Integer> {

    /**
     * Конструктор репозитория валют
     * 
     * <p>Инициализирует подключение к базе данных SQLite по указанному пути.
     * 
     * @param dbPath путь к файлу базы данных SQLite (например: "budget_master.db")
     * @throws IllegalArgumentException если dbPath равен null или пустой строке
     */
    public CurrencyRepository(String dbPath) {
        super(dbPath);
    }

    /**
     * Мягкое удаление валюты по ID с указанием пользователя
     * 
     * <p>Устанавливает поля delete_time = текущее время и deleted_by = указанный пользователь.
     * Запись физически не удаляется из базы данных.
     * 
     * @param id ID валюты для удаления (положительное целое число)
     * @param deletedBy пользователь, который выполняет удаление (не null, не пустая строка)
     * @return true, если удаление выполнено успешно, false если валюта не найдена
     */
    public boolean deleteById(Integer id, String deletedBy) {
        return softDelete(TABLE_CURRENCIES, id, deletedBy);
    }

    /**
     * Поиск валюты по позиции
     * 
     * <p>Возвращает валюту с указанной позицией.
     * 
     * @param position позиция валюты для поиска (положительное целое число)    
     * @return Optional с найденной валютой, если найдена, иначе пустой Optional
     */
    public Optional<Currency> findByPosition(int position) {
        return findByColumn(TABLE_CURRENCIES, COLUMN_POSITION, position, this::mapRowSafe);
    }

    /**
     * Мягкое удаление валюты по названию с указанием пользователя
     * 
     * <p>Устанавливает поля delete_time = текущее время и deleted_by = указанный пользователь.
     * Удаляет первую найденную валюту с указанным названием.
     * 
     * @param title название валюты для удаления (не null, не пустая строка)
     * @param deletedBy пользователь, который выполняет удаление (не null, не пустая строка)
     * @return true, если удаление выполнено успешно, false если валюта не найдена
     */
    public boolean deleteByTitle(String title, String deletedBy) {
        return softDelete(TABLE_CURRENCIES, COLUMN_TITLE, title, deletedBy);
    }

    /**
     * Получение всех валют из базы данных
     * 
     * <p>Возвращает полный список всех валют, включая как активные, так и удаленные записи.
     * Результат не фильтруется по статусу удаления.
     * 
     * @return список всех валют в базе данных (может быть пустым, но не null)
     * @throws SQLException при ошибке подключения к базе данных или выполнения запроса
     */
    @Override
    public List<Currency> findAll() {
        return findAll(TABLE_CURRENCIES, this::mapRowSafe);
    }

    /**
     * Поиск валюты по уникальному идентификатору
     * 
     * <p>Возвращает валюту независимо от статуса удаления (активная или удаленная).
     * Если валюта не найдена, возвращает пустой Optional.
     * 
     * @param id ID валюты для поиска (положительное целое число)
     * @return Optional с найденной валютой, если найдена, иначе пустой Optional
     */
    @Override
    public Optional<Currency> findById(Integer id) {
        return findByColumn(TABLE_CURRENCIES, COLUMN_ID, id, this::mapRowSafe);
    }

    /**
     * Поиск валюты по названию (title)
     * 
     * <p>Возвращает первую найденную валюту с указанным названием.
     * Поиск выполняется независимо от статуса удаления.
     * Поиск чувствителен к регистру.
     * 
     * @param title название валюты для поиска (не null, не пустая строка)
     * @return Optional с найденной валютой, если найдена, иначе пустой Optional
     */
    public Optional<Currency> findByTitle(String title) {
        return findByColumn(TABLE_CURRENCIES, COLUMN_TITLE, title, this::mapRowSafe);
    }

    /**
     * Получение максимального значения позиции среди всех валют
     * 
     * <p>Выполняет SQL-запрос для получения максимального значения позиции.
     * Включает как активные, так и удаленные валюты.
     * 
     * @return максимальная позиция, 0 если валют нет
     */
    public int getMaxPosition() {
        return getMaxValue(TABLE_CURRENCIES, COLUMN_POSITION, null);
    }
    


    /**
     * Безопасное преобразование строки ResultRow в объект Currency
     * 
     * <p>Обрабатывает исключения и возвращает null при ошибке.
     * Используется в методах findAll и findById для безопасного маппинга данных.
     * Если происходит ошибка при чтении данных, она логируется в консоль.
     * 
     * <p>Этот метод обеспечивает безопасность при обработке больших наборов данных,
     * где одна некорректная запись не должна прерывать обработку всего результата.
     * 
     * @param row ResultRow с данными из базы (не null)
     * @return объект Currency с заполненными данными или null при ошибке
     */
    public Currency mapRowSafe(com.sadengineer.budgetmaster.backend.database.DatabaseConnection.ResultRow row) {
        try {
            Currency currency = new Currency();
            currency.setId(row.getInt(COLUMN_ID));
            currency.setCreateTime(DateTimeUtil.parseFromSqlite(row.getString(COLUMN_CREATE_TIME)));
            currency.setUpdateTime(DateTimeUtil.parseFromSqlite(row.getString(COLUMN_UPDATE_TIME)));
            currency.setDeleteTime(DateTimeUtil.parseFromSqlite(row.getString(COLUMN_DELETE_TIME)));
            currency.setCreatedBy(row.getString(COLUMN_CREATED_BY));
            currency.setUpdatedBy(row.getString(COLUMN_UPDATED_BY));
            currency.setDeletedBy(row.getString(COLUMN_DELETED_BY));
            currency.setPosition(row.getInt(COLUMN_POSITION));
            currency.setTitle(row.getString(COLUMN_TITLE));
            return currency;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Currency save(Currency currency) {
        String sql = "INSERT INTO " + TABLE_CURRENCIES + " (title, position, created_by, updated_by, deleted_by, create_time, delete_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        String createTimeStr = currency.getCreateTime() != null ? DateTimeUtil.formatForSqlite(currency.getCreateTime()) : null;
        String updateTimeStr = currency.getUpdateTime() != null ? DateTimeUtil.formatForSqlite(currency.getUpdateTime()) : null;
        String deleteTimeStr = currency.getDeleteTime() != null ? DateTimeUtil.formatForSqlite(currency.getDeleteTime()) : null;
        
        long id = connection.executeInsert(sql,
            currency.getTitle(),
            currency.getPosition(),
            currency.getCreatedBy(),
            currency.getUpdatedBy(),
            currency.getDeletedBy(),
            createTimeStr,
            deleteTimeStr,
            updateTimeStr
        );
        
        currency.setId((int) id);
        return currency;
    }

    @Override
    public Currency update(Currency currency) {
        String sql = "UPDATE " + TABLE_CURRENCIES + " SET title=?, position=?, created_by=?, updated_by=?, deleted_by=?, create_time=?, delete_time=?, update_time=? WHERE " + COLUMN_ID + "=?";
        
        String createTimeStr = currency.getCreateTime() != null ? DateTimeUtil.formatForSqlite(currency.getCreateTime()) : null;
        String updateTimeStr = currency.getUpdateTime() != null ? DateTimeUtil.formatForSqlite(currency.getUpdateTime()) : null;
        String deleteTimeStr = currency.getDeleteTime() != null ? DateTimeUtil.formatForSqlite(currency.getDeleteTime()) : null;
        
        connection.executeUpdate(sql,
            currency.getTitle(),
            currency.getPosition(),
            currency.getCreatedBy(),
            currency.getUpdatedBy(),
            currency.getDeletedBy(),
            createTimeStr,
            deleteTimeStr,
            updateTimeStr,
            currency.getId()
        );
        
        return currency;
    }
} 