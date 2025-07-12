package repository;

import model.Budget;
import util.DateTimeUtil;
import java.sql.*;
import java.util.*;

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
        return softDelete("budgets", id, deletedBy);
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
        return softDelete("budgets", "category_id", categoryId, deletedBy);
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
        return findAll("budgets", this::mapRowSafe);
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
        return findByColumn("budgets", "category_id", categoryId, this::mapRowSafe);
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
        return findAll("budgets", "currency_id", currencyId, this::mapRowSafe);
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
        return findByColumn("budgets", "id", id, this::mapRowSafe);
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
        return findByColumn("budgets", "position", position, this::mapRowSafe);
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
        return getMaxValue("budgets", "position", null);
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
        budget.setId(rs.getInt("id"));
        budget.setCreateTime(DateTimeUtil.parseFromSqlite(rs.getString("create_time")));
        budget.setUpdateTime(DateTimeUtil.parseFromSqlite(rs.getString("update_time")));
        budget.setDeleteTime(DateTimeUtil.parseFromSqlite(rs.getString("delete_time")));
        budget.setCreatedBy(rs.getString("created_by"));
        budget.setUpdatedBy(rs.getString("updated_by"));
        budget.setDeletedBy(rs.getString("deleted_by"));
        budget.setPosition(rs.getInt("position"));
        budget.setAmount(rs.getInt("amount"));
        budget.setCurrencyId(rs.getInt("currency_id"));
        // Безопасное чтение поля category_id с обработкой NULL значений
        try {
            Integer categoryId = rs.getObject("category_id", Integer.class);
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
        String sql = "INSERT INTO budgets (create_time, update_time, delete_time, created_by, updated_by, deleted_by, position, amount, currency_id, category_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Форматируем даты в совместимом с SQLite формате, обрабатываем null значения
            String createTimeStr = budget.getCreateTime() != null ? 
                DateTimeUtil.formatForSqlite(budget.getCreateTime()) : null;
            String updateTimeStr = budget.getUpdateTime() != null ? 
                DateTimeUtil.formatForSqlite(budget.getUpdateTime()) : null;
            String deleteTimeStr = budget.getDeleteTime() != null ? 
                DateTimeUtil.formatForSqlite(budget.getDeleteTime()) : null;
            
            stmt.setString(1, createTimeStr);
            stmt.setString(2, updateTimeStr);
            stmt.setString(3, deleteTimeStr);
            stmt.setString(4, budget.getCreatedBy());
            stmt.setString(5, budget.getUpdatedBy());
            stmt.setString(6, budget.getDeletedBy());
            stmt.setInt(7, budget.getPosition());
            stmt.setInt(8, budget.getAmount());
            stmt.setInt(9, budget.getCurrencyId());
            stmt.setObject(10, budget.getCategoryId());
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
        String sql = "UPDATE budgets SET create_time=?, update_time=?, delete_time=?, created_by=?, updated_by=?, deleted_by=?, position=?, amount=?, currency_id=?, category_id=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Форматируем даты в совместимом с SQLite формате, обрабатываем null значения
            String createTimeStr = budget.getCreateTime() != null ? 
                DateTimeUtil.formatForSqlite(budget.getCreateTime()) : null;
            String updateTimeStr = budget.getUpdateTime() != null ? 
                DateTimeUtil.formatForSqlite(budget.getUpdateTime()) : null;
            String deleteTimeStr = budget.getDeleteTime() != null ? 
                DateTimeUtil.formatForSqlite(budget.getDeleteTime()) : null;
            
            stmt.setString(1, createTimeStr);
            stmt.setString(2, updateTimeStr);
            stmt.setString(3, deleteTimeStr);
            stmt.setString(4, budget.getCreatedBy());
            stmt.setString(5, budget.getUpdatedBy());
            stmt.setString(6, budget.getDeletedBy());
            stmt.setInt(7, budget.getPosition());
            stmt.setInt(8, budget.getAmount());
            stmt.setInt(9, budget.getCurrencyId());
            stmt.setObject(10, budget.getCategoryId());
            stmt.setInt(11, budget.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return budget;
    }

    
} 