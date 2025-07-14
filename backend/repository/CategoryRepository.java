package repository;

import model.Category;
import util.DateTimeUtil;
import java.sql.*;
import java.util.*;
import static constants.RepositoryConstants.*;

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
 * 
 * @author BudgetMaster
 * @version 1.0
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
        return findAll(TABLE_CATEGORIES, this::mapRowSafe);
    }

    /**
     * Получение категорий по типу операции
     * 
     * <p>Возвращает список всех категорий с указанным типом операции.
     * Поиск выполняется независимо от статуса удаления.
     * 
     * @param operationType тип операции категории для поиска (положительное целое число)
     * @return список категорий с указанным типом операции (может быть пустым, но не null)
     */
    public List<Category> findAllByOperationType(Integer operationType) {
        return findAll(TABLE_CATEGORIES, COLUMN_OPERATION_TYPE, operationType, this::mapRowSafe);
    }

        /**
     * Поиск всех категорий по ID родительской категории
     * 
     * <p>Возвращает категорию независимо от статуса удаления (активная или удаленная).
     * Если категория не найдена, возвращает пустой Optional.
     * 
     * @param parentId ID родительской категории для поиска (положительное целое число)
     * @return Optional с найденной категорией, если найдена, иначе пустой Optional
     */
    public List<Category> findAllByParentId(Integer parentId) {
        return findAll(TABLE_CATEGORIES, COLUMN_PARENT_ID, parentId, this::mapRowSafe);
    }

    /**
     * Получение категорий по типу
     * 
     * <p>Возвращает список всех категорий с указанным типом.
     * Поиск выполняется независимо от статуса удаления.
     * 
     * @param type тип категории для поиска (положительное целое число)
     * @return список категорий с указанным типом (может быть пустым, но не null)
     */
    public List<Category> findAllByType(Integer type) {
        return findAll(TABLE_CATEGORIES, COLUMN_TYPE, type, this::mapRowSafe);
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
        return findByColumn(TABLE_CATEGORIES, COLUMN_ID, id, this::mapRowSafe);
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
     * Поиск категории по названию
     * 
     * <p>Возвращает категорию независимо от статуса удаления (активная или удаленная).
     * Если категория не найдена, возвращает пустой Optional.
     * 
     * @param title название категории для поиска (не null, не пустая строка)
     * @return Optional с найденной категорией, если найдена, иначе пустой Optional
     */
    public Optional<Category> findByTitle(String title) {
        return findByColumn(TABLE_CATEGORIES, COLUMN_TITLE, title, this::mapRowSafe);
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
            Integer parentId = rs.getObject(COLUMN_PARENT_ID, Integer.class);
            category.setParentId(parentId);
        } catch (SQLException e) {
            category.setParentId(null);
        }
        return category;
    }

    /**
     * Безопасное преобразование строки ResultSet в объект Category
     * 
     * <p>Обертка над mapRow с обработкой исключений.
     * Если при чтении данных возникает ошибка, метод возвращает null.
     * 
     * @param rs ResultSet с данными из базы данных (не null)
     * @return объект Category с заполненными полями или null при ошибке
     */
    public Category mapRowSafe(ResultSet rs) {
        try {
            return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Category save(Category category) {
        String[] columns = new String[CATEGORY_COLUMNS.length - 1];
        System.arraycopy(CATEGORY_COLUMNS, 1, columns, 0, CATEGORY_COLUMNS.length - 1);
        String sql = "INSERT INTO " + TABLE_CATEGORIES + " (" +
            String.join(", ", columns) + ") VALUES (" + "?, ".repeat(columns.length - 1) + "?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            String createTimeStr = category.getCreateTime() != null ? DateTimeUtil.formatForSqlite(category.getCreateTime()) : null;
            String updateTimeStr = category.getUpdateTime() != null ? DateTimeUtil.formatForSqlite(category.getUpdateTime()) : null;
            String deleteTimeStr = category.getDeleteTime() != null ? DateTimeUtil.formatForSqlite(category.getDeleteTime()) : null;
            stmt.setString(1, category.getTitle());
            stmt.setInt(2, category.getPosition());
            stmt.setInt(3, category.getOperationType());
            stmt.setInt(4, category.getType());
            stmt.setObject(5, category.getParentId());
            stmt.setString(6, category.getCreatedBy());
            stmt.setString(7, category.getUpdatedBy());
            stmt.setString(8, category.getDeletedBy());
            stmt.setString(9, createTimeStr);
            stmt.setString(10, deleteTimeStr);
            stmt.setString(11, updateTimeStr);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    category.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return category;
    }

    @Override
    public Category update(Category category) {
        String[] columns = new String[CATEGORY_COLUMNS.length - 1];
        System.arraycopy(CATEGORY_COLUMNS, 1, columns, 0, CATEGORY_COLUMNS.length - 1);
        String setClause = String.join("=?, ", columns) + "=?";
        String sql = "UPDATE " + TABLE_CATEGORIES + " SET " + setClause + " WHERE " + COLUMN_ID + "=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String createTimeStr = category.getCreateTime() != null ? DateTimeUtil.formatForSqlite(category.getCreateTime()) : null;
            String updateTimeStr = category.getUpdateTime() != null ? DateTimeUtil.formatForSqlite(category.getUpdateTime()) : null;
            String deleteTimeStr = category.getDeleteTime() != null ? DateTimeUtil.formatForSqlite(category.getDeleteTime()) : null;
            stmt.setString(1, category.getTitle());
            stmt.setInt(2, category.getPosition());
            stmt.setInt(3, category.getOperationType());
            stmt.setInt(4, category.getType());
            stmt.setObject(5, category.getParentId());
            stmt.setString(6, category.getCreatedBy());
            stmt.setString(7, category.getUpdatedBy());
            stmt.setString(8, category.getDeletedBy());
            stmt.setString(9, createTimeStr);
            stmt.setString(10, deleteTimeStr);
            stmt.setString(11, updateTimeStr);
            stmt.setInt(12, category.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return category;
    }
} 