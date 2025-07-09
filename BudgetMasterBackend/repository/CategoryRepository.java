package repository;

import model.Category;
import util.DateTimeUtil;
import java.sql.*;
import java.util.*;

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
        return softDelete("categories", id, deletedBy);
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
        return softDelete("categories", "title", title, deletedBy);
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
        return findAll("categories", this::mapRowSafe);
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
        return findAll("categories", "operation_type", operationType, this::mapRowSafe);
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
        return findAll("categories", "parent_id", parentId, this::mapRowSafe);
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
        return findAll("categories", "type", type, this::mapRowSafe);
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
        return findByColumn("categories", "id", id, this::mapRowSafe);
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
        return getMaxValue("categories", "position", null);
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
        return findByColumn("categories", "title", title, this::mapRowSafe);
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
        category.setId(rs.getInt("id"));
        category.setCreateTime(DateTimeUtil.parseFromSqlite(rs.getString("create_time")));
        category.setUpdateTime(DateTimeUtil.parseFromSqlite(rs.getString("update_time")));
        category.setDeleteTime(DateTimeUtil.parseFromSqlite(rs.getString("delete_time")));
        category.setCreatedBy(rs.getString("created_by"));
        category.setUpdatedBy(rs.getString("updated_by"));
        category.setDeletedBy(rs.getString("deleted_by"));
        category.setPosition(rs.getInt("position"));
        category.setTitle(rs.getString("title"));
        category.setOperationType(rs.getInt("operation_type"));
        category.setType(rs.getInt("type"));
        category.setParentId(rs.getObject("parent_id", Integer.class));
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

    /**
     * Сохранение новой категории в базу данных
     * 
     * <p>Создает новую запись в таблице categories с автоматически сгенерированным ID.
     * Все поля объекта category сохраняются в базу данных.
     * После успешного сохранения ID объекта обновляется сгенерированным значением.
     * 
     * @param category объект категории для сохранения (не null, должен содержать все обязательные поля)
     * @return объект категории с установленным ID (тот же объект, что и входной параметр)
     */
    @Override
    public Category save(Category category) {
        String sql = "INSERT INTO categories (create_time, update_time, delete_time, created_by, updated_by, deleted_by, position, title, operation_type, type, parent_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Форматируем даты в совместимом с SQLite формате, обрабатываем null значения
            String createTimeStr = category.getCreateTime() != null ? 
                DateTimeUtil.formatForSqlite(category.getCreateTime()) : null;
            String updateTimeStr = category.getUpdateTime() != null ? 
                DateTimeUtil.formatForSqlite(category.getUpdateTime()) : null;
            String deleteTimeStr = category.getDeleteTime() != null ? 
                DateTimeUtil.formatForSqlite(category.getDeleteTime()) : null;
            
            stmt.setString(1, createTimeStr);
            stmt.setString(2, updateTimeStr);
            stmt.setString(3, deleteTimeStr);
            stmt.setString(4, category.getCreatedBy());
            stmt.setString(5, category.getUpdatedBy());
            stmt.setString(6, category.getDeletedBy());
            stmt.setInt(7, category.getPosition());
            stmt.setString(8, category.getTitle());
            stmt.setInt(9, category.getOperationType());
            stmt.setInt(10, category.getType());
            if (category.getParentId() != null) stmt.setInt(11, category.getParentId()); else stmt.setNull(11, Types.INTEGER);
            stmt.executeUpdate();
            
            // Получаем сгенерированный id
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

    /**
     * Обновление существующей категории в базе данных
     * 
     * <p>Обновляет все поля записи по ID категории.
     * Объект category должен содержать валидный ID существующей записи.
     * Все поля записи будут заменены значениями из объекта category.
     * 
     * @param category объект категории с обновленными данными (не null, должен содержать валидный ID)
     * @return обновленный объект категории (тот же объект, что и входной параметр)
     */
    @Override
    public Category update(Category category) {
        String sql = "UPDATE categories SET create_time=?, update_time=?, delete_time=?, created_by=?, updated_by=?, deleted_by=?, position=?, title=?, operation_type=?, type=?, parent_id=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Форматируем даты в совместимом с SQLite формате, обрабатываем null значения
            String createTimeStr = category.getCreateTime() != null ? 
                DateTimeUtil.formatForSqlite(category.getCreateTime()) : null;
            String updateTimeStr = category.getUpdateTime() != null ? 
                DateTimeUtil.formatForSqlite(category.getUpdateTime()) : null;
            String deleteTimeStr = category.getDeleteTime() != null ? 
                DateTimeUtil.formatForSqlite(category.getDeleteTime()) : null;
            
            stmt.setString(1, createTimeStr);
            stmt.setString(2, updateTimeStr);
            stmt.setString(3, deleteTimeStr);
            stmt.setString(4, category.getCreatedBy());
            stmt.setString(5, category.getUpdatedBy());
            stmt.setString(6, category.getDeletedBy());
            stmt.setInt(7, category.getPosition());
            stmt.setString(8, category.getTitle());
            stmt.setInt(9, category.getOperationType());
            stmt.setInt(10, category.getType());
            if (category.getParentId() != null) stmt.setInt(11, category.getParentId()); else stmt.setNull(11, Types.INTEGER);
            stmt.setInt(12, category.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return category;
    }
} 