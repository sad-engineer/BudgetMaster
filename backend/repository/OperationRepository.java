package repository;

import model.Operation;
import util.DateTimeUtil;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

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
 * 
 * @author BudgetMaster
 * @version 1.0
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
        return softDelete("operations", id, deletedBy);
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
        return findAll("operations", this::mapRowSafe);
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
        return findAll("operations", "account_id", accountId, this::mapRowSafe);
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
        return findAll("operations", "category_id", categoryId, this::mapRowSafe);
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
        return findAll("operations", "comment", comment, this::mapRowSafe);
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
        return findAll("operations", "currency_id", currencyId, this::mapRowSafe);
    }

    /**
     * Получение операций по дате
     * 
     * <p>Возвращает список всех операций с указанной датой.
     * Поиск выполняется за весь день (от 00:00:00 до 23:59:59).
     * Поиск выполняется независимо от статуса удаления.
     * 
     * @param date дата для поиска (не null)
     * @return список операций с указанной датой (может быть пустым, но не null)
     */
    public List<Operation> findAllByDate(LocalDateTime date) {
        List<Operation> result = new ArrayList<>();
        
        // Получаем начало и конец дня
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = date.toLocalDate().atTime(23, 59, 59, 999999999);
        
        // Форматируем даты для SQLite
        String startDateStr = DateTimeUtil.formatForSqlite(startOfDay);
        String endDateStr = DateTimeUtil.formatForSqlite(endOfDay);
        
        String sql = "SELECT * FROM operations WHERE date >= ? AND date <= ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, startDateStr);
            stmt.setString(2, endDateStr);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Operation op = mapRowSafe(rs);
                if (op != null) {
                    result.add(op);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return result;
    }

    /**
     * Получение операций по типу
     * 
     * <p>Возвращает список всех операций с указанным типом.
     * Поиск выполняется независимо от статуса удаления.
     * 
     * @param type тип операции для поиска (положительное целое число)
     * @return список операций с указанным типом (может быть пустым, но не null)
     */
    public List<Operation> findAllByType(Integer type) {
        return findAll("operations", "type", type, this::mapRowSafe);
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
        return findByColumn("operations", "id", id, this::mapRowSafe);
    }
       
    /**
     * Преобразование строки ResultSet в объект Operation
     * 
     * <p>Парсит все поля из базы данных в соответствующие поля объекта Operation.
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
     *   <li>type (INTEGER) - тип операции</li>
     *   <li>date (TEXT) - дата операции в формате SQLite</li>
     *   <li>amount (INTEGER) - сумма операции в копейках</li>
     *   <li>comment (TEXT) - комментарий к операции</li>
     *   <li>category_id (INTEGER) - ID категории операции</li>
     *   <li>account_id (INTEGER) - ID счета операции</li>
     *   <li>currency_id (INTEGER) - ID валюты операции</li>
     *   <li>to_account_id (INTEGER) - ID целевого счета (может быть null)</li>
     *   <li>to_currency_id (INTEGER) - ID целевой валюты (может быть null)</li>
     *   <li>to_amount (INTEGER) - целевая сумма (может быть null)</li>
     * </ul>
     * 
     * @param rs ResultSet с данными из базы (не null, должен содержать все необходимые поля)
     * @return объект Operation с заполненными данными
     * @throws SQLException при ошибке чтения данных из ResultSet или несоответствии типов данных
     */
    private Operation mapRow(ResultSet rs) throws SQLException {
        Operation op = new Operation();
        
        // Безопасное чтение ID
        Object idObj = rs.getObject("id");
        if (idObj instanceof Long) {
            op.setId(((Long) idObj).intValue());
        } else {
            op.setId((Integer) idObj);
        }
        
        // Читаем даты как строки и парсим их
        String createTimeStr = rs.getString("create_time");
        op.setCreateTime(DateTimeUtil.parseFromSqlite(createTimeStr));
        
        String updateTimeStr = rs.getString("update_time");
        op.setUpdateTime(DateTimeUtil.parseFromSqlite(updateTimeStr));
        
        String deleteTimeStr = rs.getString("delete_time");
        op.setDeleteTime(DateTimeUtil.parseFromSqlite(deleteTimeStr));
        
        op.setCreatedBy(rs.getString("created_by"));
        op.setUpdatedBy(rs.getString("updated_by"));
        op.setDeletedBy(rs.getString("deleted_by"));
        
        // Безопасное чтение числовых полей
        Object typeObj = rs.getObject("type");
        if (typeObj instanceof Long) {
            op.setType(((Long) typeObj).intValue());
        } else {
            op.setType((Integer) typeObj);
        }
        
        // Безопасное чтение даты операции
        String dateStr = rs.getString("date");
        op.setDate(DateTimeUtil.parseFromSqlite(dateStr));
        
        Object amountObj = rs.getObject("amount");
        if (amountObj instanceof Long) {
            op.setAmount(((Long) amountObj).intValue());
        } else {
            op.setAmount((Integer) amountObj);
        }
        
        op.setComment(rs.getString("comment"));
        
        Object categoryIdObj = rs.getObject("category_id");
        if (categoryIdObj instanceof Long) {
            op.setCategoryId(((Long) categoryIdObj).intValue());
        } else {
            op.setCategoryId((Integer) categoryIdObj);
        }
        
        Object accountIdObj = rs.getObject("account_id");
        if (accountIdObj instanceof Long) {
            op.setAccountId(((Long) accountIdObj).intValue());
        } else {
            op.setAccountId((Integer) accountIdObj);
        }
        
        Object currencyIdObj = rs.getObject("currency_id");
        if (currencyIdObj instanceof Long) {
            op.setCurrencyId(((Long) currencyIdObj).intValue());
        } else {
            op.setCurrencyId((Integer) currencyIdObj);
        }
        
        // Обработка nullable полей
        Object toAccountIdObj = rs.getObject("to_account_id");
        if (toAccountIdObj == null) {
            op.setToAccountId(null);
        } else if (toAccountIdObj instanceof Long) {
            op.setToAccountId(((Long) toAccountIdObj).intValue());
        } else {
            op.setToAccountId((Integer) toAccountIdObj);
        }
        
        Object toCurrencyIdObj = rs.getObject("to_currency_id");
        if (toCurrencyIdObj == null) {
            op.setToCurrencyId(null);
        } else if (toCurrencyIdObj instanceof Long) {
            op.setToCurrencyId(((Long) toCurrencyIdObj).intValue());
        } else {
            op.setToCurrencyId((Integer) toCurrencyIdObj);
        }
        
        Object toAmountObj = rs.getObject("to_amount");
        if (toAmountObj == null) {
            op.setToAmount(null);
        } else if (toAmountObj instanceof Long) {
            op.setToAmount(((Long) toAmountObj).intValue());
        } else {
            op.setToAmount((Integer) toAmountObj);
        }
        
        return op;
    }

    /**
     * Безопасное преобразование строки ResultSet в объект Operation
     * 
     * <p>Обрабатывает исключения SQLException и возвращает null при ошибке.
     * Используется в методах findAll и findById для безопасного маппинга данных.
     * Если происходит ошибка при чтении данных, она логируется в консоль.
     * 
     * <p>Этот метод является оберткой над mapRow и обеспечивает безопасность
     * при обработке больших наборов данных, где одна некорректная запись
     * не должна прерывать обработку всего результата.
     * 
     * @param rs ResultSet с данными из базы (не null)
     * @return объект Operation с заполненными данными или null при ошибке
     */
    public Operation mapRowSafe(ResultSet rs) {
        try {
            return mapRow(rs);
        } catch (SQLException e) {
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
        String sql = "INSERT INTO operations (create_time, update_time, delete_time, created_by, updated_by, deleted_by, type, date, amount, comment, category_id, account_id, currency_id, to_account_id, to_currency_id, to_amount) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Форматируем даты в совместимом с SQLite формате, обрабатываем null значения
            String createTimeStr = op.getCreateTime() != null ? 
                DateTimeUtil.formatForSqlite(op.getCreateTime()) : null;
            String updateTimeStr = op.getUpdateTime() != null ? 
                DateTimeUtil.formatForSqlite(op.getUpdateTime()) : null;
            String deleteTimeStr = op.getDeleteTime() != null ? 
                DateTimeUtil.formatForSqlite(op.getDeleteTime()) : null;
            String dateStr = op.getDate() != null ? 
                DateTimeUtil.formatForSqlite(op.getDate()) : null;
            
            stmt.setString(1, createTimeStr);
            stmt.setString(2, updateTimeStr);
            stmt.setString(3, deleteTimeStr);
            stmt.setString(4, op.getCreatedBy());
            stmt.setString(5, op.getUpdatedBy());
            stmt.setString(6, op.getDeletedBy());
            stmt.setInt(7, op.getType());
            stmt.setString(8, dateStr);
            stmt.setInt(9, op.getAmount());
            stmt.setString(10, op.getComment());
            stmt.setInt(11, op.getCategoryId());
            stmt.setInt(12, op.getAccountId());
            stmt.setInt(13, op.getCurrencyId());
            if (op.getToAccountId() != null) stmt.setInt(14, op.getToAccountId()); else stmt.setNull(14, Types.INTEGER);
            if (op.getToCurrencyId() != null) stmt.setInt(15, op.getToCurrencyId()); else stmt.setNull(15, Types.INTEGER);
            if (op.getToAmount() != null) stmt.setInt(16, op.getToAmount()); else stmt.setNull(16, Types.INTEGER);
            stmt.executeUpdate();
            
            // Получаем сгенерированный id
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    op.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        String sql = "UPDATE operations SET create_time=?, update_time=?, delete_time=?, created_by=?, updated_by=?, deleted_by=?, type=?, date=?, amount=?, comment=?, category_id=?, account_id=?, currency_id=?, to_account_id=?, to_currency_id=?, to_amount=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Форматируем даты в совместимом с SQLite формате, обрабатываем null значения
            String createTimeStr = op.getCreateTime() != null ? 
                DateTimeUtil.formatForSqlite(op.getCreateTime()) : null;
            String updateTimeStr = op.getUpdateTime() != null ? 
                DateTimeUtil.formatForSqlite(op.getUpdateTime()) : null;
            String deleteTimeStr = op.getDeleteTime() != null ? 
                DateTimeUtil.formatForSqlite(op.getDeleteTime()) : null;
            String dateStr = op.getDate() != null ? 
                DateTimeUtil.formatForSqlite(op.getDate()) : null;
            
            stmt.setString(1, createTimeStr);
            stmt.setString(2, updateTimeStr);
            stmt.setString(3, deleteTimeStr);
            stmt.setString(4, op.getCreatedBy());
            stmt.setString(5, op.getUpdatedBy());
            stmt.setString(6, op.getDeletedBy());
            stmt.setInt(7, op.getType());
            stmt.setString(8, dateStr);
            stmt.setInt(9, op.getAmount());
            stmt.setString(10, op.getComment());
            stmt.setInt(11, op.getCategoryId());
            stmt.setInt(12, op.getAccountId());
            stmt.setInt(13, op.getCurrencyId());
            if (op.getToAccountId() != null) stmt.setInt(14, op.getToAccountId()); else stmt.setNull(14, Types.INTEGER);
            if (op.getToCurrencyId() != null) stmt.setInt(15, op.getToCurrencyId()); else stmt.setNull(15, Types.INTEGER);
            if (op.getToAmount() != null) stmt.setInt(16, op.getToAmount()); else stmt.setNull(16, Types.INTEGER);
            stmt.setInt(17, op.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return op;
    }

} 