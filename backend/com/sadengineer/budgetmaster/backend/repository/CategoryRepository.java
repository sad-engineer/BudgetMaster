// -*- coding: utf-8 -*-
package com.sadengineer.budgetmaster.backend.repository;

import com.sadengineer.budgetmaster.backend.model.Category;
import com.sadengineer.budgetmaster.backend.util.DateTimeUtil;
import java.sql.*;
import java.util.*;
import static com.sadengineer.budgetmaster.backend.constants.RepositoryConstants.*;

/**
 * Репозиторий для работы с категориями в базе данных
 * 
 * <p>Содержит базовые CRUD операции для сущности Category:
 * <ul>
 *   <li>Создание новых категорий (save)</li>
 *   <li>Чтение категорий по ID (findById)</li>
 *   <li>Получение всех категорий (findAll)</li>
 *   <li>Обновление существующих категорий (update)</li>
 *   <li>Мягкое удаление категорий (delete)</li>
 * </ul>
 * 
 * <p>Все методы работают с таблицей "categories" и используют
 * безопасное преобразование данных через mapRowSafe.
 */
public class CategoryRepository extends BaseRepository implements Repository<Category, Integer> {

    /**
     * Конструктор репозитория категорий
     * 
     * <p>Инициализирует подключение к базе данных SQLite по указанному пути.
     * 
     * @param dbPath путь к файлу базы данных SQLite (например: "budget_master.db")
     */
    public CategoryRepository(String dbPath) {
        super(dbPath);
    }

    /**
     * Мягкое удаление категории по ID с указанием пользователя
     * 
     * <p>Устанавливает поля delete_time = текущее время и deleted_by = указанный пользователь.
     * Запись физически не удаляется из базы данных.
     * 
     * @param id ID категории для удаления (положительное целое число)
     * @param deletedBy пользователь, который выполняет удаление (не null, не пустая строка)
     * @return true, если удаление выполнено успешно, false если категория не найдена
     */
    public boolean deleteById(Integer id, String deletedBy) {
        return softDelete(TABLE_CATEGORIES, id, deletedBy);
    }

    /**
     * Мягкое удаление категории по title с указанием пользователя
     * 
     * <p>Устанавливает поля delete_time = текущее время и deleted_by = указанный пользователь.
     * Запись физически не удаляется из базы данных.
     * 
     * @param title title категории для удаления (не null, не пустая строка)
     * @param deletedBy пользователь, который выполняет удаление (не null, не пустая строка)
     * @return true, если удаление выполнено успешно, false если категория не найдена
     */
    public boolean deleteByTitle(String title, String deletedBy) {
        return softDelete(TABLE_CATEGORIES, COLUMN_TITLE, title, deletedBy);
    }

    /**
     * Получение всех категорий из базы данных
     * 
     * <p>Возвращает полный список всех категорий, включая как активные, так и удаленные записи.
     * Результат не фильтруется по статусу удаления.
     * 
     * @return список всех категорий в базе данных (может быть пустым, но не null)
     */
    @Override
    public List<Category> findAll() {
        return connection.executeQuery("SELECT * FROM " + TABLE_CATEGORIES, this::mapRowSafe);
    }

    /**
     * Получение категорий по типу операции
     * 
     * <p>Возвращает список всех категорий с указанным типом операции.
     * Поиск выполняется независимо от статуса удаления.
     * 
     * @param operationType тип операции для поиска (положительное целое число)
     * @return список категорий с указанным типом операции (может быть пустым, но не null)
     */
    public List<Category> findAllByOperationType(Integer operationType) {
        String sql = "SELECT * FROM " + TABLE_CATEGORIES + " WHERE " + COLUMN_OPERATION_TYPE + " = ?";
        return connection.executeQuery(sql, this::mapRowSafe, operationType);
    }

        /**
     * Получение категорий по ID родительской категории
     * 
     * <p>Возвращает список всех подкатегорий указанной родительской категории.
     * Поиск выполняется независимо от статуса удаления.
     * 
     * @param parentId ID родительской категории для поиска (положительное целое число)
     * @return список подкатегорий (может быть пустым, но не null)
     */
    public List<Category> findAllByParentId(Integer parentId) {
        String sql = "SELECT * FROM " + TABLE_CATEGORIES + " WHERE " + COLUMN_PARENT_ID + " = ?";
        return connection.executeQuery(sql, this::mapRowSafe, parentId);
    }

    /**
     * Получение категорий по типу
     * 
     * <p>Возвращает список всех категорий указанного типа.
     * Поиск выполняется независимо от статуса удаления.
     * 
     * @param type тип категории для поиска (положительное целое число)
     * @return список категорий с указанным типом (может быть пустым, но не null)
     */
    public List<Category> findAllByType(Integer type) {
        String sql = "SELECT * FROM " + TABLE_CATEGORIES + " WHERE " + COLUMN_TYPE + " = ?";
        return connection.executeQuery(sql, this::mapRowSafe, type);
    }

    /**
     * Поиск категории по уникальному идентификатору
     * 
     * <p>Возвращает категорию независимо от статуса удаления (активная или удаленная).
     * Если категория не найдена, возвращает пустой Optional.
     * 
     * @param id ID категории для поиска (положительное целое число)
     * @return Optional с найденной категорией, если найдена, иначе пустой Optional
     */
    @Override
    public Optional<Category> findById(Integer id) {
        String sql = "SELECT * FROM " + TABLE_CATEGORIES + " WHERE " + COLUMN_ID + " = ?";
        return connection.executeQuerySingle(sql, this::mapRowSafe, id);
    }

    /**
     * Поиск категории по позиции
     * 
     * <p>Возвращает категорию независимо от статуса удаления (активная или удаленная).
     * Если категория не найдена, возвращает пустой Optional.
     * 
     * @param position позиция категории для поиска (положительное целое число)    
     * @return категория, если найдена, иначе null
     */
    public Optional<Category> findByPosition(int position) {
        String sql = "SELECT * FROM " + TABLE_CATEGORIES + " WHERE " + COLUMN_POSITION + " = ?";
        return connection.executeQuerySingle(sql, this::mapRowSafe, position);
    }

    /**
     * Получение максимального значения позиции среди всех категорий
     * 
     * <p>Выполняет SQL-запрос для получения максимального значения позиции.
     * Включает как активные, так и удаленные категории.
     * 
     * @return максимальная позиция, 0 если категорий нет
     */
    public int getMaxPosition() {
        return getMaxValue(TABLE_CATEGORIES, COLUMN_POSITION, null);
    }

    /**
     * Поиск категории по названию (title)
     * 
     * <p>Возвращает первую найденную категорию с указанным названием.
     * Поиск выполняется независимо от статуса удаления.
     * Поиск чувствителен к регистру.
     * 
     * @param title название категории для поиска (не null, не пустая строка)
     * @return Optional с найденной категорией, если найдена, иначе пустой Optional
     */
    public Optional<Category> findByTitle(String title) {
        String sql = "SELECT * FROM " + TABLE_CATEGORIES + " WHERE " + COLUMN_TITLE + " = ?";
        return connection.executeQuerySingle(sql, this::mapRowSafe, title);
    }

    /**
     * Преобразование строки ResultSet в объект Category
     * 
     * <p>Парсит все поля из базы данных в соответствующие поля объекта Category.
     * Метод обрабатывает преобразование дат из строкового формата SQLite в LocalDateTime.
     * Обеспечивает безопасное чтение числовых полей с поддержкой типов Long и Integer.
     * 
     * <p>Ожидаемая структура ResultSet:
     * <ul>
     *   <li>id (INTEGER) - уникальный идентификатор</li>
     *   <li>create_time (TEXT) - дата создания в формате SQLite</li>
     *   <li>update_time (TEXT) - дата обновления в формате SQLite</li>
     *   <li>delete_time (TEXT) - дата удаления в формате SQLite</li>
     *   <li>created_by (TEXT) - пользователь, создавший запись</li>
     *   <li>updated_by (TEXT) - пользователь, обновивший запись</li>
     *   <li>deleted_by (TEXT) - пользователь, удаливший запись</li>
     *   <li>position (INTEGER) - позиция в списке</li>
     *   <li>title (TEXT) - название категории</li>
     *   <li>operation_type (INTEGER) - тип операции категории</li>
     *   <li>type (INTEGER) - тип категории</li>
     *   <li>parent_id (INTEGER) - ID родительской категории</li>
     * </ul>
     * 
     * @param rs ResultSet с данными из базы данных (не null)
     * @return объект Category с заполненными полями (не null)
     * @throws SQLException при ошибке чтения данных из ResultSet
     */
    private Category mapRow(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setId(rs.getInt(COLUMN_ID));
        category.setCreateTime(DateTimeUtil.parseFromSqlite(rs.getString(COLUMN_CREATE_TIME)));
        category.setUpdateTime(DateTimeUtil.parseFromSqlite(rs.getString(COLUMN_UPDATE_TIME)));
        category.setDeleteTime(DateTimeUtil.parseFromSqlite(rs.getString(COLUMN_DELETE_TIME)));
        category.setCreatedBy(rs.getString(COLUMN_CREATED_BY));
        category.setUpdatedBy(rs.getString(COLUMN_UPDATED_BY));
        category.setDeletedBy(rs.getString(COLUMN_DELETED_BY));
        category.setPosition(rs.getInt(COLUMN_POSITION));
        category.setTitle(rs.getString(COLUMN_TITLE));
        category.setOperationType(rs.getInt(COLUMN_OPERATION_TYPE));
        category.setType(rs.getInt(COLUMN_TYPE));
        // Безопасное чтение поля parent_id с обработкой NULL значений
        try {
            Object parentIdObj = rs.getObject(COLUMN_PARENT_ID);
            Integer parentId = (parentIdObj != null) ? (Integer) parentIdObj : null;
            category.setParentId(parentId);
        } catch (SQLException e) {
            category.setParentId(null);
        }
        return category;
    }

    /**
     * Безопасное преобразование строки ResultRow в объект Category
     * 
     * <p>Обертка над mapRow с обработкой исключений.
     * Если при чтении данных возникает ошибка, метод возвращает null.
     * 
     * @param row ResultRow с данными из базы данных (не null)
     * @return объект Category с заполненными полями или null при ошибке
     */
    public Category mapRowSafe(com.sadengineer.budgetmaster.backend.database.DatabaseConnection.ResultRow row) {
        try {
            Category category = new Category();
            category.setId(row.getInt(COLUMN_ID));
            category.setCreateTime(DateTimeUtil.parseFromSqlite(row.getString(COLUMN_CREATE_TIME)));
            category.setUpdateTime(DateTimeUtil.parseFromSqlite(row.getString(COLUMN_UPDATE_TIME)));
            category.setDeleteTime(DateTimeUtil.parseFromSqlite(row.getString(COLUMN_DELETE_TIME)));
            category.setCreatedBy(row.getString(COLUMN_CREATED_BY));
            category.setUpdatedBy(row.getString(COLUMN_UPDATED_BY));
            category.setDeletedBy(row.getString(COLUMN_DELETED_BY));
            category.setPosition(row.getInt(COLUMN_POSITION));
            category.setTitle(row.getString(COLUMN_TITLE));
            category.setOperationType(row.getInt(COLUMN_OPERATION_TYPE));
            category.setType(row.getInt(COLUMN_TYPE));
            
            // Безопасное чтение поля parent_id с обработкой NULL значений
            try {
                Integer parentId = row.getInt(COLUMN_PARENT_ID);
                category.setParentId(parentId);
            } catch (Exception e) {
                category.setParentId(null);
            }
            
            return category;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Создание новой категории
     * 
     * <p>Вставляет новую категорию в базу данных.
     * 
     * @param category объект Category для создания (не null)
     * @return созданная категория с заполненными полями (не null)  
     */
    @Override
    public Category save(Category category) {
        String sql = "INSERT INTO " + TABLE_CATEGORIES + " (title, position, operation_type, type, parent_id, created_by, updated_by, deleted_by, create_time, update_time, delete_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
            String createTimeStr = category.getCreateTime() != null ? DateTimeUtil.formatForSqlite(category.getCreateTime()) : null;
            String updateTimeStr = category.getUpdateTime() != null ? DateTimeUtil.formatForSqlite(category.getUpdateTime()) : null;
            String deleteTimeStr = category.getDeleteTime() != null ? DateTimeUtil.formatForSqlite(category.getDeleteTime()) : null;
        
        long id = connection.executeInsert(sql,
            category.getTitle(),
            category.getPosition(),
            category.getOperationType(),
            category.getType(),
            category.getParentId(),
            category.getCreatedBy(),
            category.getUpdatedBy(),
            category.getDeletedBy(),
            createTimeStr,
            updateTimeStr,
            deleteTimeStr
        );
        
        category.setId((int) id);
        return category;
    }

    /**
     * Обновление категории
     * 
     * <p>Обновляет существующую категорию в базе данных.
     * 
     * @param category объект Category для обновления (не null) 
     * @return обновленная категория с заполненными полями (не null)
     */
    @Override
    public Category update(Category category) {
        String sql = "UPDATE " + TABLE_CATEGORIES + " SET title=?, position=?, operation_type=?, type=?, parent_id=?, created_by=?, updated_by=?, deleted_by=?, create_time=?, update_time=?, delete_time=? WHERE id=?";
        
            String createTimeStr = category.getCreateTime() != null ? DateTimeUtil.formatForSqlite(category.getCreateTime()) : null;
            String updateTimeStr = category.getUpdateTime() != null ? DateTimeUtil.formatForSqlite(category.getUpdateTime()) : null;
            String deleteTimeStr = category.getDeleteTime() != null ? DateTimeUtil.formatForSqlite(category.getDeleteTime()) : null;
        
        connection.executeUpdate(sql,
            category.getTitle(),
            category.getPosition(),
            category.getOperationType(),
            category.getType(),
            category.getParentId(),
            category.getCreatedBy(),
            category.getUpdatedBy(),
            category.getDeletedBy(),
            createTimeStr,
            updateTimeStr,
            deleteTimeStr,
            category.getId()
        );
        
        return category;
    }
} 