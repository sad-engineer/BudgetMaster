package repository;

import model.Account;
import util.DateTimeUtil;
import java.sql.*;
import java.util.*;

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
 * 
 * @author BudgetMaster
 * @version 1.0
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
        return softDelete("accounts", id, deletedBy);
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
        return softDelete("accounts", "title", title, deletedBy);
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
        return findAll("accounts", this::mapRowSafe);
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
        return findAll("accounts", "currency_id", currencyId, this::mapRowSafe);
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
        return findAll("accounts", "type", type, this::mapRowSafe);
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
        return findByColumn("accounts", "id", id, this::mapRowSafe);
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
        return findByColumn("accounts", "title", title, this::mapRowSafe);
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
        return findByColumn("accounts", "position", position, this::mapRowSafe);
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
        return getMaxValue("accounts", "position", null);
    }

    /**
     * Преобразование строки ResultSet в объект Account
     * 
     * <p>Парсит все поля из базы данных в соответствующие поля объекта Account.
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
     *   <li>title (TEXT) - название счета</li>
     *   <li>amount (INTEGER) - баланс счета в копейках</li>
     *   <li>type (INTEGER) - тип счета</li>
     *   <li>currency_id (INTEGER) - ID валюты счета</li>
     *   <li>closed (INTEGER) - статус закрытия счета (0 - открыт, 1 - закрыт)</li>
     *   <li>credit_card_limit (INTEGER) - лимит кредитной карты в копейках</li>
     *   <li>credit_card_category_id (INTEGER) - ID категории кредитной карты</li>
     *   <li>credit_card_commission_category_id (INTEGER) - ID категории комиссии кредитной карты</li>
     * </ul>
     * 
     * @param rs ResultSet с данными из базы данных (не null)
     * @return объект Account с заполненными полями (не null)
     * @throws SQLException при ошибке чтения данных из ResultSet
     */
    private Account mapRow(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setId(rs.getInt("id"));
        account.setCreateTime(DateTimeUtil.parseFromSqlite(rs.getString("create_time")));
        account.setUpdateTime(DateTimeUtil.parseFromSqlite(rs.getString("update_time")));
        account.setDeleteTime(DateTimeUtil.parseFromSqlite(rs.getString("delete_time")));
        account.setCreatedBy(rs.getString("created_by"));
        account.setUpdatedBy(rs.getString("updated_by"));
        account.setDeletedBy(rs.getString("deleted_by"));
        account.setPosition(rs.getInt("position"));
        account.setTitle(rs.getString("title"));
        account.setAmount(rs.getInt("amount"));
        account.setType(rs.getInt("type"));
        account.setCurrencyId(rs.getInt("currency_id"));
        account.setClosed(rs.getInt("closed"));
        // Безопасное чтение полей кредитных карт с обработкой NULL значений
        try {
            Integer creditCardLimit = rs.getObject("credit_card_limit", Integer.class);
            account.setCreditCardLimit(creditCardLimit);
        } catch (SQLException e) {
            account.setCreditCardLimit(null);
        }
        
        try {
            Integer creditCardCategoryId = rs.getObject("credit_card_category_id", Integer.class);
            account.setCreditCardCategoryId(creditCardCategoryId);
        } catch (SQLException e) {
            account.setCreditCardCategoryId(null);
        }
        
        try {
            Integer creditCardCommissionCategoryId = rs.getObject("credit_card_commission_category_id", Integer.class);
            account.setCreditCardCommissionCategoryId(creditCardCommissionCategoryId);
        } catch (SQLException e) {
            account.setCreditCardCommissionCategoryId(null);
        }
        return account;
    }

    /**
     * Безопасное преобразование строки ResultSet в объект Account
     * 
     * <p>Обертка над mapRow с обработкой исключений.
     * Если при чтении данных возникает ошибка, метод возвращает null.
     * 
     * @param rs ResultSet с данными из базы данных (не null)
     * @return объект Account с заполненными полями или null при ошибке
     */
    public Account mapRowSafe(ResultSet rs) {
        try {
            return mapRow(rs);
        } catch (SQLException e) {
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
        String sql = "INSERT INTO accounts (create_time, update_time, delete_time, created_by, updated_by, deleted_by, position, title, amount, type, currency_id, closed, credit_card_limit, credit_card_category_id, credit_card_commission_category_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Форматируем даты в совместимом с SQLite формате, обрабатываем null значения
            String createTimeStr = account.getCreateTime() != null ? 
                DateTimeUtil.formatForSqlite(account.getCreateTime()) : null;
            String updateTimeStr = account.getUpdateTime() != null ? 
                DateTimeUtil.formatForSqlite(account.getUpdateTime()) : null;
            String deleteTimeStr = account.getDeleteTime() != null ? 
                DateTimeUtil.formatForSqlite(account.getDeleteTime()) : null;
            
            stmt.setString(1, createTimeStr);
            stmt.setString(2, updateTimeStr);
            stmt.setString(3, deleteTimeStr);
            stmt.setString(4, account.getCreatedBy());
            stmt.setString(5, account.getUpdatedBy());
            stmt.setString(6, account.getDeletedBy());
            stmt.setInt(7, account.getPosition());
            stmt.setString(8, account.getTitle());
            stmt.setInt(9, account.getAmount());
            stmt.setInt(10, account.getType());
            stmt.setInt(11, account.getCurrencyId());
            stmt.setInt(12, account.getClosed());
            stmt.setObject(13, account.getCreditCardLimit());
            stmt.setObject(14, account.getCreditCardCategoryId());
            stmt.setObject(15, account.getCreditCardCommissionCategoryId());
            stmt.executeUpdate();
            
            // Получаем сгенерированный id
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    account.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
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
        String sql = "UPDATE accounts SET create_time=?, update_time=?, delete_time=?, created_by=?, updated_by=?, deleted_by=?, position=?, title=?, amount=?, type=?, currency_id=?, closed=?, credit_card_limit=?, credit_card_category_id=?, credit_card_commission_category_id=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Форматируем даты в совместимом с SQLite формате, обрабатываем null значения
            String createTimeStr = account.getCreateTime() != null ? 
                DateTimeUtil.formatForSqlite(account.getCreateTime()) : null;
            String updateTimeStr = account.getUpdateTime() != null ? 
                DateTimeUtil.formatForSqlite(account.getUpdateTime()) : null;
            String deleteTimeStr = account.getDeleteTime() != null ? 
                DateTimeUtil.formatForSqlite(account.getDeleteTime()) : null;
            
            stmt.setString(1, createTimeStr);
            stmt.setString(2, updateTimeStr);
            stmt.setString(3, deleteTimeStr);
            stmt.setString(4, account.getCreatedBy());
            stmt.setString(5, account.getUpdatedBy());
            stmt.setString(6, account.getDeletedBy());
            stmt.setInt(7, account.getPosition());
            stmt.setString(8, account.getTitle());
            stmt.setInt(9, account.getAmount());
            stmt.setInt(10, account.getType());
            stmt.setInt(11, account.getCurrencyId());
            stmt.setInt(12, account.getClosed());
            stmt.setObject(13, account.getCreditCardLimit());
            stmt.setObject(14, account.getCreditCardCategoryId());
            stmt.setObject(15, account.getCreditCardCommissionCategoryId());
            stmt.setInt(16, account.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return account;
    }
} 