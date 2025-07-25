// -*- coding: utf-8 -*-
package com.sadengineer.budgetmaster.backend.repository;

import com.sadengineer.budgetmaster.backend.model.Operation;
import com.sadengineer.budgetmaster.backend.util.DateTimeUtil;

import java.time.LocalDateTime;
import java.util.*;
import static com.sadengineer.budgetmaster.backend.constants.RepositoryConstants.*;

/**
 * Репозиторий для работы с операциями в базе данных
 * 
 * <p>Содержит базовые CRUD операции для сущности Operation:
 * <ul>
 *   <li>Создание новых операций (save)</li>
 *   <li>Чтение операций по ID (findById)</li>
 *   <li>Получение всех операций (findAll)</li>
 *   <li>Обновление существующих операций (update)</li>
 *   <li>Мягкое удаление операций (delete)</li>
 * </ul>
 * 
 * <p>Все методы работают с таблицей "operations" и используют
 * безопасное преобразование данных через mapRowSafe.
 */
public class OperationRepository extends BaseRepository implements Repository<Operation, Integer> {

    /**
     * Конструктор репозитория операций
     * 
     * <p>Инициализирует подключение к базе данных SQLite по указанному пути.
     * 
     * @param dbPath путь к файлу базы данных SQLite (например: "budget_master.db")
     */
    public OperationRepository(String dbPath) {
        super(dbPath);
    }

    /**
     * Мягкое удаление операции по ID с указанием пользователя
     * 
     * <p>Устанавливает поля delete_time = текущее время и deleted_by = указанный пользователь.
     * Запись физически не удаляется из базы данных.
     * 
     * @param id ID операции для удаления (положительное целое число)
     * @param deletedBy пользователь, который выполняет удаление (не null, не пустая строка)
     * @return true, если удаление выполнено успешно, false если операция не найдена
     */
    public boolean deleteById(Integer id, String deletedBy) {
        return softDelete(TABLE_OPERATIONS, id, deletedBy);
    }

    /**
     * Получение всех операций из базы данных
     * 
     * <p>Возвращает полный список всех операций, включая как активные, так и удаленные записи.
     * Результат не фильтруется по статусу удаления.
     * 
     * @return список всех операций в базе данных (может быть пустым, но не null)
     */
    @Override
    public List<Operation> findAll() {
        return connection.executeQuery("SELECT * FROM " + TABLE_OPERATIONS, this::mapRowSafe);
    }

    /**
     * Получение операций по ID счета
     * 
     * <p>Возвращает список всех операций с указанным счетом.
     * Поиск выполняется независимо от статуса удаления.
     * 
     * @param accountId ID счета для поиска (положительное целое число)
     * @return список операций с указанным счетом (может быть пустым, но не null)
     */
    public List<Operation> findAllByAccountId(Integer accountId) {
        String sql = "SELECT * FROM " + TABLE_OPERATIONS + " WHERE " + COLUMN_ACCOUNT_ID + " = ?";
        return connection.executeQuery(sql, this::mapRowSafe, accountId);
    }

    /**
     * Получение операций по ID категории
     * 
     * <p>Возвращает список всех операций с указанной категорией.
     * Поиск выполняется независимо от статуса удаления.
     * 
     * @param categoryId ID категории для поиска (положительное целое число)
     * @return список операций с указанной категорией (может быть пустым, но не null)
     */
    public List<Operation> findAllByCategoryId(Integer categoryId) {
        String sql = "SELECT * FROM " + TABLE_OPERATIONS + " WHERE " + COLUMN_CATEGORY_ID + " = ?";
        return connection.executeQuery(sql, this::mapRowSafe, categoryId);
    }

    /**
     * Получение операций по комментарию
     * 
     * <p>Возвращает список всех операций с указанным комментарием.
     * Поиск выполняется независимо от статуса удаления.
     * 
     * @param comment комментарий для поиска (не null, не пустая строка)
     * @return список операций с указанным комментарием (может быть пустым, но не null)
     */
    public List<Operation> findAllByComment(String comment) {
        String sql = "SELECT * FROM " + TABLE_OPERATIONS + " WHERE " + COLUMN_COMMENT + " = ?";
        return connection.executeQuery(sql, this::mapRowSafe, comment);
    }

    /**
     * Получение операций по ID валюты
     * 
     * <p>Возвращает список всех операций с указанной валютой.
     * Поиск выполняется независимо от статуса удаления.
     * 
     * @param currencyId ID валюты для поиска (положительное целое число)
     * @return список операций с указанной валютой (может быть пустым, но не null)
     */
    public List<Operation> findAllByCurrencyId(Integer currencyId) {
        String sql = "SELECT * FROM " + TABLE_OPERATIONS + " WHERE " + COLUMN_CURRENCY_ID + " = ?";
        return connection.executeQuery(sql, this::mapRowSafe, currencyId);
    }

    /**
     * Получение операций по дате
     * 
     * <p>Возвращает список всех операций с указанной датой.
     * Поиск выполняется независимо от статуса удаления.
     * 
     * @param date дата для поиска (не null)
     * @return список операций с указанной датой (может быть пустым, но не null)
     */
    public List<Operation> findAllByDate(LocalDateTime date) {
        List<Operation> result = new ArrayList<>();
        
        // Получаем начало и конец дня для поиска
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        
        String startDateStr = DateTimeUtil.formatForSqlite(startOfDay);
        String endDateStr = DateTimeUtil.formatForSqlite(endOfDay);
        
        String sql = "SELECT * FROM " + TABLE_OPERATIONS + " WHERE " + COLUMN_DATE + " >= ? AND " + COLUMN_DATE + " <= ?";
        
        return connection.executeQuery(sql, this::mapRowSafe, startDateStr, endDateStr);
    }

    /**
     * Получение операций по типу
     * 
     * <p>Возвращает список всех операций указанного типа.
     * Поиск выполняется независимо от статуса удаления.
     * 
     * @param type тип операции для поиска (положительное целое число)
     * @return список операций с указанным типом (может быть пустым, но не null)
     */
    public List<Operation> findAllByType(Integer type) {
        String sql = "SELECT * FROM " + TABLE_OPERATIONS + " WHERE " + COLUMN_TYPE + " = ?";
        return connection.executeQuery(sql, this::mapRowSafe, type);
    }

    /**
     * Поиск операции по уникальному идентификатору
     * 
     * <p>Возвращает операцию независимо от статуса удаления (активная или удаленная).
     * Если операция не найдена, возвращает пустой Optional.
     * 
     * @param id ID операции для поиска (положительное целое число)
     * @return Optional с найденной операцией, если найдена, иначе пустой Optional
     */
    @Override
    public Optional<Operation> findById(Integer id) {
        String sql = "SELECT * FROM " + TABLE_OPERATIONS + " WHERE " + COLUMN_ID + " = ?";
        return connection.executeQuerySingle(sql, this::mapRowSafe, id);
    }

    /**
     * Безопасное преобразование строки ResultRow в объект Operation
     * 
     * <p>Обрабатывает исключения и возвращает null при ошибке.
     * Используется в методах findAll и findById для безопасного маппинга данных.
     * 
     * @param row ResultRow с данными из базы (не null)
     * @return объект Operation с заполненными данными или null при ошибке
     */
    public Operation mapRowSafe(com.sadengineer.budgetmaster.backend.database.DatabaseConnection.ResultRow row) {
        try {
            Operation op = new Operation();
            
            // Безопасное чтение ID
            op.setId(row.getInt(COLUMN_ID));
            
            // Читаем даты как строки и парсим их
            String createTimeStr = row.getString(COLUMN_CREATE_TIME);
            op.setCreateTime(DateTimeUtil.parseFromSqlite(createTimeStr));
            
            String updateTimeStr = row.getString(COLUMN_UPDATE_TIME);
            op.setUpdateTime(DateTimeUtil.parseFromSqlite(updateTimeStr));
            
            String deleteTimeStr = row.getString(COLUMN_DELETE_TIME);
            op.setDeleteTime(DateTimeUtil.parseFromSqlite(deleteTimeStr));
            
            op.setCreatedBy(row.getString(COLUMN_CREATED_BY));
            op.setUpdatedBy(row.getString(COLUMN_UPDATED_BY));
            op.setDeletedBy(row.getString(COLUMN_DELETED_BY));
            
            // Безопасное чтение числовых полей
            op.setType(row.getInt(COLUMN_TYPE));
            
            // Безопасное чтение даты операции
            String dateStr = row.getString(COLUMN_DATE);
            op.setDate(DateTimeUtil.parseFromSqlite(dateStr));
            
            op.setAmount(row.getInt(COLUMN_AMOUNT));
            op.setComment(row.getString(COLUMN_COMMENT));
            op.setCategoryId(row.getInt(COLUMN_CATEGORY_ID));
            op.setAccountId(row.getInt(COLUMN_ACCOUNT_ID));
            op.setCurrencyId(row.getInt(COLUMN_CURRENCY_ID));
            
            // Обработка nullable полей
            Integer toAccountId = row.getInt(COLUMN_TO_ACCOUNT_ID);
            op.setToAccountId(toAccountId);
            
            Integer toCurrencyId = row.getInt(COLUMN_TO_CURRENCY_ID);
            op.setToCurrencyId(toCurrencyId);
            
            Integer toAmount = row.getInt(COLUMN_TO_AMOUNT);
            op.setToAmount(toAmount);
            
            return op;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Сохранение новой операции в базу данных
     * 
     * <p>Создает новую запись в таблице operations с автоматически сгенерированным ID.
     * Все поля объекта operation сохраняются в базу данных.
     * После успешного сохранения ID объекта обновляется сгенерированным значением.
     * 
     * @param op объект операции для сохранения (не null, должен содержать все обязательные поля)
     * @return объект операции с установленным ID (тот же объект, что и входной параметр)
     */
    @Override
    public Operation save(Operation op) {
        String sql = "INSERT INTO " + TABLE_OPERATIONS + " (type, date, amount, comment, category_id, account_id, currency_id, to_account_id, to_currency_id, to_amount, created_by, updated_by, deleted_by, create_time, update_time, delete_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        String createTimeStr = op.getCreateTime() != null ? DateTimeUtil.formatForSqlite(op.getCreateTime()) : null;
        String updateTimeStr = op.getUpdateTime() != null ? DateTimeUtil.formatForSqlite(op.getUpdateTime()) : null;
        String deleteTimeStr = op.getDeleteTime() != null ? DateTimeUtil.formatForSqlite(op.getDeleteTime()) : null;
        String dateStr = op.getDate() != null ? DateTimeUtil.formatForSqlite(op.getDate()) : null;
        
        long id = connection.executeInsert(sql,
            op.getType(),
            dateStr,
            op.getAmount(),
            op.getComment(),
            op.getCategoryId(),
            op.getAccountId(),
            op.getCurrencyId(),
            op.getToAccountId(),
            op.getToCurrencyId(),
            op.getToAmount(),
            op.getCreatedBy(),
            op.getUpdatedBy(),
            op.getDeletedBy(),
            createTimeStr,
            updateTimeStr,
            deleteTimeStr
        );
        
        op.setId((int) id);
        return op;
    }

    /**
     * Обновление существующей операции в базе данных
     * 
     * <p>Обновляет все поля записи по ID операции.
     * Объект op должен содержать валидный ID существующей записи.
     * Все поля записи будут заменены значениями из объекта op.
     * 
     * @param op объект операции с обновленными данными (не null, должен содержать валидный ID)
     * @return обновленный объект операции (тот же объект, что и входной параметр)
     */
    @Override
    public Operation update(Operation op) {
        String sql = "UPDATE " + TABLE_OPERATIONS + " SET type=?, date=?, amount=?, comment=?, category_id=?, account_id=?, currency_id=?, to_account_id=?, to_currency_id=?, to_amount=?, created_by=?, updated_by=?, deleted_by=?, create_time=?, update_time=?, delete_time=? WHERE id=?";
        
        String createTimeStr = op.getCreateTime() != null ? DateTimeUtil.formatForSqlite(op.getCreateTime()) : null;
        String updateTimeStr = op.getUpdateTime() != null ? DateTimeUtil.formatForSqlite(op.getUpdateTime()) : null;
        String deleteTimeStr = op.getDeleteTime() != null ? DateTimeUtil.formatForSqlite(op.getDeleteTime()) : null;
        String dateStr = op.getDate() != null ? DateTimeUtil.formatForSqlite(op.getDate()) : null;
        
        connection.executeUpdate(sql,
            op.getType(),
            dateStr,
            op.getAmount(),
            op.getComment(),
            op.getCategoryId(),
            op.getAccountId(),
            op.getCurrencyId(),
            op.getToAccountId(),
            op.getToCurrencyId(),
            op.getToAmount(),
            op.getCreatedBy(),
            op.getUpdatedBy(),
            op.getDeletedBy(),
            createTimeStr,
            updateTimeStr,
            deleteTimeStr,
            op.getId()
        );
        
        return op;
    }

} 