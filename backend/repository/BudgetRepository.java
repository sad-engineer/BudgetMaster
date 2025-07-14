package repository;

import model.Budget;
import util.DateTimeUtil;
import java.sql.*;
import java.util.*;
import static constants.RepositoryConstants.*;

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
 * 
 * @author BudgetMaster
 * @version 1.0
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
        return findAll(TABLE_BUDGETS, this::mapRowSafe);
    }
        
    /**
     * Поиск бюджета по ID категории
     * 
     * <p>Возвращает бюджет независимо от статуса удаления (активный или удаленный).
     * Если бюджет не найден, возвращает пустой Optional.
     * 
     * @param categoryId ID категории для поиска (положительное целое число)
     * @return Optional с найденным бюджетом, если найден, иначе пустой Optional
     */
    public Optional<Budget> findByCategoryId(Integer categoryId) {
        return findByColumn(TABLE_BUDGETS, COLUMN_CATEGORY_ID, categoryId, this::mapRowSafe);
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
        return findAll(TABLE_BUDGETS, COLUMN_CURRENCY_ID, currencyId, this::mapRowSafe);
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
        return findByColumn(TABLE_BUDGETS, COLUMN_ID, id, this::mapRowSafe);
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
        return findByColumn(TABLE_BUDGETS, COLUMN_POSITION, position, this::mapRowSafe);
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
     * Преобразование строки ResultSet в объект Budget
     * 
     * <p>Парсит все поля из базы данных в соответствующие поля объекта Budget.
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
     *   <li>amount (INTEGER) - сумма бюджета в копейках</li>
     *   <li>currency_id (INTEGER) - ID валюты бюджета</li>
     *   <li>category_id (INTEGER) - ID категории бюджета</li>
     * </ul>
     * 
     * @param rs ResultSet с данными из базы данных (не null)
     * @return объект Budget с заполненными полями (не null)
     * @throws SQLException при ошибке чтения данных из ResultSet
     */
    private Budget mapRow(ResultSet rs) throws SQLException {
        Budget budget = new Budget();
        budget.setId(rs.getInt(COLUMN_ID));
        budget.setCreateTime(DateTimeUtil.parseFromSqlite(rs.getString(COLUMN_CREATE_TIME)));
        budget.setUpdateTime(DateTimeUtil.parseFromSqlite(rs.getString(COLUMN_UPDATE_TIME)));
        budget.setDeleteTime(DateTimeUtil.parseFromSqlite(rs.getString(COLUMN_DELETE_TIME)));
        budget.setCreatedBy(rs.getString(COLUMN_CREATED_BY));
        budget.setUpdatedBy(rs.getString(COLUMN_UPDATED_BY));
        budget.setDeletedBy(rs.getString(COLUMN_DELETED_BY));
        budget.setPosition(rs.getInt(COLUMN_POSITION));
        budget.setAmount(rs.getInt(COLUMN_AMOUNT));
        budget.setCurrencyId(rs.getInt(COLUMN_CURRENCY_ID));
        // Безопасное чтение поля category_id с обработкой NULL значений
        try {
            Integer categoryId = rs.getObject(COLUMN_CATEGORY_ID, Integer.class);
            budget.setCategoryId(categoryId);
        } catch (SQLException e) {
            budget.setCategoryId(null);
        }
        return budget;
    }

    /**
     * Безопасное преобразование строки ResultSet в объект Budget
     * 
     * <p>Обертка над mapRow с обработкой исключений.
     * Если при чтении данных возникает ошибка, метод возвращает null.
     * 
     * @param rs ResultSet с данными из базы данных (не null)
     * @return объект Budget с заполненными полями или null при ошибке
     */
    public Budget mapRowSafe(ResultSet rs) {
        try {
            return mapRow(rs);
        } catch (SQLException e) {
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
        // Используем BUDGET_COLUMNS, исключая id (первый элемент)
        String[] columns = new String[BUDGET_COLUMNS.length - 1];
        System.arraycopy(BUDGET_COLUMNS, 1, columns, 0, BUDGET_COLUMNS.length - 1);
        
        String sql = "INSERT INTO " + TABLE_BUDGETS + " (" + 
            String.join(", ", columns) + ") " +
            "VALUES (" + "?, ".repeat(columns.length - 1) + "?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Форматируем даты в совместимом с SQLite формате, обрабатываем null значения
            String createTimeStr = budget.getCreateTime() != null ? 
                DateTimeUtil.formatForSqlite(budget.getCreateTime()) : null;
            String updateTimeStr = budget.getUpdateTime() != null ? 
                DateTimeUtil.formatForSqlite(budget.getUpdateTime()) : null;
            String deleteTimeStr = budget.getDeleteTime() != null ? 
                DateTimeUtil.formatForSqlite(budget.getDeleteTime()) : null;
            
            // Порядок параметров соответствует BUDGET_COLUMNS (без id)
            stmt.setInt(1, budget.getAmount());
            stmt.setInt(2, budget.getCurrencyId());
            stmt.setObject(3, budget.getCategoryId());
            stmt.setInt(4, budget.getPosition());
            stmt.setString(5, budget.getCreatedBy());
            stmt.setString(6, budget.getUpdatedBy());
            stmt.setString(7, budget.getDeletedBy());
            stmt.setString(8, createTimeStr);
            stmt.setString(9, deleteTimeStr);
            stmt.setString(10, updateTimeStr);
            stmt.executeUpdate();
            
            // Получаем сгенерированный id
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    budget.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
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
        // Используем BUDGET_COLUMNS, исключая id (первый элемент)
        String[] columns = new String[BUDGET_COLUMNS.length - 1];
        System.arraycopy(BUDGET_COLUMNS, 1, columns, 0, BUDGET_COLUMNS.length - 1);
        String setClause = String.join("=?, ", columns) + "=?";
        String sql = "UPDATE " + TABLE_BUDGETS + " SET " + setClause + " WHERE " + COLUMN_ID + "=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Форматируем даты в совместимом с SQLite формате, обрабатываем null значения
            String createTimeStr = budget.getCreateTime() != null ? 
                DateTimeUtil.formatForSqlite(budget.getCreateTime()) : null;
            String updateTimeStr = budget.getUpdateTime() != null ? 
                DateTimeUtil.formatForSqlite(budget.getUpdateTime()) : null;
            String deleteTimeStr = budget.getDeleteTime() != null ? 
                DateTimeUtil.formatForSqlite(budget.getDeleteTime()) : null;
            // Порядок параметров соответствует BUDGET_COLUMNS (без id)
            stmt.setInt(1, budget.getAmount());
            stmt.setInt(2, budget.getCurrencyId());
            stmt.setObject(3, budget.getCategoryId());
            stmt.setInt(4, budget.getPosition());
            stmt.setString(5, budget.getCreatedBy());
            stmt.setString(6, budget.getUpdatedBy());
            stmt.setString(7, budget.getDeletedBy());
            stmt.setString(8, createTimeStr);
            stmt.setString(9, deleteTimeStr);
            stmt.setString(10, updateTimeStr);
            stmt.setInt(11, budget.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return budget;
    }

    
} 