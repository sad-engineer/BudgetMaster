// -*- coding: utf-8 -*-
package com.sadengineer.budgetmaster.backend.repository;

import com.sadengineer.budgetmaster.backend.model.Account;
import com.sadengineer.budgetmaster.backend.util.DateTimeUtil;

import java.util.*;
import static com.sadengineer.budgetmaster.backend.constants.RepositoryConstants.*;

/**
 * Репозиторий для работы со счетами в базе данных
 * 
 * <p>Содержит базовые CRUD операции для сущности Account:
 * <ul>
 *   <li>Создание новых счетов (save)</li>
 *   <li>Чтение счетов по ID (findById)</li>
 *   <li>Получение всех счетов (findAll)</li>
 *   <li>Обновление существующих счетов (update)</li>
 *   <li>Мягкое удаление счетов (delete)</li>
 * </ul>
 * 
 * <p>Все методы работают с таблицей "accounts" и используют
 * безопасное преобразование данных через mapRowSafe.
 */
public class AccountRepository extends BaseRepository implements Repository<Account, Integer> {

    /**
     * Конструктор репозитория счетов
     * 
     * <p>Инициализирует подключение к базе данных SQLite по указанному пути.
     * 
     * @param dbPath путь к файлу базы данных SQLite (например: "budget_master.db")
     */
    public AccountRepository(String dbPath) {
        super(dbPath);
    }

    /**
     * Мягкое удаление счета по ID с указанием пользователя
     * 
     * <p>Устанавливает поля delete_time = текущее время и deleted_by = указанный пользователь.
     * Запись физически не удаляется из базы данных.
     * 
     * @param id ID счета для удаления (положительное целое число)
     * @param deletedBy пользователь, который выполняет удаление (не null, не пустая строка)
     * @return true, если удаление выполнено успешно, false если счет не найден
     */
    public boolean deleteById(Integer id, String deletedBy) {
        return softDelete(TABLE_ACCOUNTS, id, deletedBy);
    }

    /**
     * Мягкое удаление счета по названию с указанием пользователя
     * 
     * <p>Устанавливает поля delete_time = текущее время и deleted_by = указанный пользователь.
     * Запись физически не удаляется из базы данных.
     * 
     * @param title название счета для удаления (не null, не пустая строка)
     * @param deletedBy пользователь, который выполняет удаление (не null, не пустая строка)
     * @return true, если удаление выполнено успешно, false если счет не найден
     */
    public boolean deleteByTitle(String title, String deletedBy) {
        return softDelete(TABLE_ACCOUNTS, COLUMN_TITLE, title, deletedBy);
    }

    /**
     * Получение всех счетов из базы данных
     * 
     * <p>Возвращает полный список всех счетов, включая как активные, так и удаленные записи.
     * Результат не фильтруется по статусу удаления.
     * 
     * @return список всех счетов в базе данных (может быть пустым, но не null)
     */
    @Override
    public List<Account> findAll() {
        return connection.executeQuery("SELECT * FROM " + TABLE_ACCOUNTS, this::mapRowSafe);
    }

    /**
     * Получение счетов по ID валюты
     * 
     * <p>Возвращает список всех счетов с указанной валютой.
     * Поиск выполняется независимо от статуса удаления.
     * 
     * @param currencyId ID валюты для поиска (положительное целое число)
     * @return список счетов с указанной валютой (может быть пустым, но не null)
     */
    public List<Account> findAllByCurrencyId(Integer currencyId) {
        String sql = "SELECT * FROM " + TABLE_ACCOUNTS + " WHERE " + COLUMN_CURRENCY_ID + " = ?";
        return connection.executeQuery(sql, this::mapRowSafe, currencyId);
    }

    /**
     * Получение счетов по типу счета
     * 
     * <p>Возвращает список всех счетов указанного типа.
     * Поиск выполняется независимо от статуса удаления.
     * 
     * @param type тип счета для поиска (положительное целое число)
     * @return список счетов с указанным типом (может быть пустым, но не null)
     */
    public List<Account> findAllByType(Integer type) {
        String sql = "SELECT * FROM " + TABLE_ACCOUNTS + " WHERE " + COLUMN_TYPE + " = ?";
        return connection.executeQuery(sql, this::mapRowSafe, type);
    }

    /**
     * Получение счетов по статусу закрытия
     * 
     * <p>Возвращает список всех счетов с указанным статусом закрытия.
     * Поиск выполняется независимо от статуса удаления.
     * 
     * @param closed статус закрытия счета (0 - открыт, 1 - закрыт)
     * @return список счетов с указанным статусом закрытия (может быть пустым, но не null)
     */
    public List<Account> findAllByClosed(Integer closed) {
        String sql = "SELECT * FROM " + TABLE_ACCOUNTS + " WHERE " + COLUMN_CLOSED + " = ?";
        return connection.executeQuery(sql, this::mapRowSafe, closed);
    }

    /**
     * Поиск счета по уникальному идентификатору
     * 
     * <p>Возвращает счет независимо от статуса удаления (активный или удаленный).
     * Если счет не найден, возвращает пустой Optional.
     * 
     * @param id ID счета для поиска (положительное целое число)
     * @return Optional с найденным счетом, если найден, иначе пустой Optional
     */
    @Override
    public Optional<Account> findById(Integer id) {
        String sql = "SELECT * FROM " + TABLE_ACCOUNTS + " WHERE " + COLUMN_ID + " = ?";
        return connection.executeQuerySingle(sql, this::mapRowSafe, id);
    }

    /**
     * Поиск счета по названию (title)
     * 
     * <p>Возвращает первый найденный счет с указанным названием.
     * Поиск выполняется независимо от статуса удаления.
     * Поиск чувствителен к регистру.
     * 
     * @param title название счета для поиска (не null, не пустая строка)
     * @return Optional с найденным счетом, если найден, иначе пустой Optional
     */
    public Optional<Account> findByTitle(String title) {
        String sql = "SELECT * FROM " + TABLE_ACCOUNTS + " WHERE " + COLUMN_TITLE + " = ?";
        return connection.executeQuerySingle(sql, this::mapRowSafe, title);
    }

    /**
     * Поиск счета по позиции
     * 
     * <p>Возвращает счет независимо от статуса удаления (активный или удаленный).
     * Если счет не найден, возвращает пустой Optional.
     * 
     * @param position позиция счета для поиска (положительное целое число)
     * @return счет, если найден, иначе null
     */
    public Optional<Account> findByPosition(Integer position) { 
        String sql = "SELECT * FROM " + TABLE_ACCOUNTS + " WHERE " + COLUMN_POSITION + " = ?";
        return connection.executeQuerySingle(sql, this::mapRowSafe, position);
    }

    /**
     * Получение максимального значения позиции среди всех счетов
     * 
     * <p>Выполняет SQL-запрос для получения максимального значения позиции.
     * Включает как активные, так и удаленные счета.
     * 
     * @return максимальная позиция, 0 если счетов нет
     */
    public int getMaxPosition() {
        return getMaxValue(TABLE_ACCOUNTS, COLUMN_POSITION, null);
    }

    /**
     * Безопасное преобразование строки ResultRow в объект Account
     * 
     * <p>Обертка над mapRow с обработкой исключений.
     * Если при чтении данных возникает ошибка, метод возвращает null.
     * 
     * @param row ResultRow с данными из базы данных (не null)
     * @return объект Account с заполненными полями или null при ошибке
     */
    public Account mapRowSafe(com.sadengineer.budgetmaster.backend.database.DatabaseConnection.ResultRow row) {
        try {
            Account account = new Account();
            account.setId(row.getInt(COLUMN_ID));
            account.setCreateTime(DateTimeUtil.parseFromSqlite(row.getString(COLUMN_CREATE_TIME)));
            account.setUpdateTime(DateTimeUtil.parseFromSqlite(row.getString(COLUMN_UPDATE_TIME)));
            account.setDeleteTime(DateTimeUtil.parseFromSqlite(row.getString(COLUMN_DELETE_TIME)));
            account.setCreatedBy(row.getString(COLUMN_CREATED_BY));
            account.setUpdatedBy(row.getString(COLUMN_UPDATED_BY));
            account.setDeletedBy(row.getString(COLUMN_DELETED_BY));
            account.setPosition(row.getInt(COLUMN_POSITION));
            account.setTitle(row.getString(COLUMN_TITLE));
            account.setAmount(row.getInt(COLUMN_AMOUNT));
            account.setType(row.getInt(COLUMN_TYPE));
            account.setCurrencyId(row.getInt(COLUMN_CURRENCY_ID));
            account.setClosed(row.getInt(COLUMN_CLOSED));
            
            // Безопасное чтение полей кредитных карт с обработкой NULL значений
            // Используем getInt() который уже обрабатывает NULL как null
            account.setCreditCardLimit(row.getInt(COLUMN_CREDIT_CARD_LIMIT));
            account.setCreditCardCategoryId(row.getInt(COLUMN_CREDIT_CARD_CATEGORY_ID));
            account.setCreditCardCommissionCategoryId(row.getInt(COLUMN_CREDIT_CARD_COMMISSION_CATEGORY_ID));
            
            return account;
        } catch (Exception e) {
            System.err.println("❌ Ошибка в mapRowSafe: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Сохранение нового счета в базу данных
     * 
     * <p>Создает новую запись в таблице accounts с автоматически сгенерированным ID.
     * Все поля объекта account сохраняются в базу данных.
     * После успешного сохранения ID объекта обновляется сгенерированным значением.
     * 
     * @param account объект счета для сохранения (не null, должен содержать все обязательные поля)
     * @return объект счета с установленным ID (тот же объект, что и входной параметр)
     */
    @Override
    public Account save(Account account) {
        String sql = "INSERT INTO " + TABLE_ACCOUNTS + " (title, position, amount, type, currency_id, closed, credit_card_limit, credit_card_category_id, credit_card_commission_category_id, created_by, updated_by, deleted_by, create_time, update_time, delete_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
            String createTimeStr = account.getCreateTime() != null ? DateTimeUtil.formatForSqlite(account.getCreateTime()) : null;
            String updateTimeStr = account.getUpdateTime() != null ? DateTimeUtil.formatForSqlite(account.getUpdateTime()) : null;
            String deleteTimeStr = account.getDeleteTime() != null ? DateTimeUtil.formatForSqlite(account.getDeleteTime()) : null;
        
        long id = connection.executeInsert(sql,
            account.getTitle(),
            account.getPosition(),
            account.getAmount(),
            account.getType(),
            account.getCurrencyId(),
            account.getClosed(),
            account.getCreditCardLimit(),
            account.getCreditCardCategoryId(),
            account.getCreditCardCommissionCategoryId(),
            account.getCreatedBy(),
            account.getUpdatedBy(),
            account.getDeletedBy(),
            createTimeStr,
            updateTimeStr,
            deleteTimeStr
        );
        
        account.setId((int) id);
        return account;
    }

    /**
     * Обновление существующего счета в базе данных
     * 
     * <p>Обновляет все поля записи по ID счета.
     * Объект account должен содержать валидный ID существующей записи.
     * Все поля записи будут заменены значениями из объекта account.
     * 
     * @param account объект счета с обновленными данными (не null, должен содержать валидный ID)
     * @return обновленный объект счета (тот же объект, что и входной параметр)
     */
    @Override
    public Account update(Account account) {
        String sql = "UPDATE " + TABLE_ACCOUNTS + " SET title=?, position=?, amount=?, type=?, currency_id=?, closed=?, credit_card_limit=?, credit_card_category_id=?, credit_card_commission_category_id=?, created_by=?, updated_by=?, deleted_by=?, create_time=?, update_time=?, delete_time=? WHERE id=?";
        
            String createTimeStr = account.getCreateTime() != null ? DateTimeUtil.formatForSqlite(account.getCreateTime()) : null;
            String updateTimeStr = account.getUpdateTime() != null ? DateTimeUtil.formatForSqlite(account.getUpdateTime()) : null;
            String deleteTimeStr = account.getDeleteTime() != null ? DateTimeUtil.formatForSqlite(account.getDeleteTime()) : null;
        
        connection.executeUpdate(sql,
            account.getTitle(),
            account.getPosition(),
            account.getAmount(),
            account.getType(),
            account.getCurrencyId(),
            account.getClosed(),
            account.getCreditCardLimit(),
            account.getCreditCardCategoryId(),
            account.getCreditCardCommissionCategoryId(),
            account.getCreatedBy(),
            account.getUpdatedBy(),
            account.getDeletedBy(),
            createTimeStr,
            updateTimeStr,
            deleteTimeStr,
            account.getId()
        );
        
        return account;
    }
} 