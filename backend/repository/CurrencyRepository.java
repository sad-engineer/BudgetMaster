// -*- coding: utf-8 -*-
package repository;

import model.Currency;
import util.DateTimeUtil;
import java.sql.*;
import java.util.*;
import static constants.RepositoryConstants.*;

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
     * Преобразование строки ResultSet в объект Currency
     * 
     * <p>Парсит все поля из базы данных в соответствующие поля объекта Currency.
     * Метод обрабатывает преобразование дат из строкового формата SQLite в LocalDateTime.
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
     *   <li>title (TEXT) - название валюты</li>
     * </ul>
     * 
     * @param rs ResultSet с данными из базы (не null, должен содержать все необходимые поля)
     * @return объект Currency с заполненными данными
     * @throws SQLException при ошибке чтения данных из ResultSet или несоответствии типов данных
     */
    private Currency mapRow(ResultSet rs) throws SQLException {
        Currency currency = new Currency();
        currency.setId(rs.getInt(COLUMN_ID));
        
        // Читаем даты как строки и парсим их
        String createTimeStr = rs.getString("create_time");
        currency.setCreateTime(DateTimeUtil.parseFromSqlite(createTimeStr));
        
        String updateTimeStr = rs.getString("update_time");
        currency.setUpdateTime(DateTimeUtil.parseFromSqlite(updateTimeStr));
        
        String deleteTimeStr = rs.getString("delete_time");
        currency.setDeleteTime(DateTimeUtil.parseFromSqlite(deleteTimeStr));
        
        currency.setCreatedBy(rs.getString("created_by"));
        currency.setUpdatedBy(rs.getString("updated_by"));
        currency.setDeletedBy(rs.getString("deleted_by"));
        currency.setPosition(rs.getInt(COLUMN_POSITION));
        currency.setTitle(rs.getString(COLUMN_TITLE));
        return currency;
    }

    /**
     * Безопасное преобразование строки ResultSet в объект Currency
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
     * @return объект Currency с заполненными данными или null при ошибке
     */
    public Currency mapRowSafe(ResultSet rs) {
        try {
            return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Currency save(Currency currency) {
        String[] columns = new String[CURRENCY_COLUMNS.length - 1];
        System.arraycopy(CURRENCY_COLUMNS, 1, columns, 0, CURRENCY_COLUMNS.length - 1);
        String sql = "INSERT INTO " + TABLE_CURRENCIES + " (" +
            String.join(", ", columns) + ") VALUES (" + "?, ".repeat(columns.length - 1) + "?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            String createTimeStr = currency.getCreateTime() != null ? DateTimeUtil.formatForSqlite(currency.getCreateTime()) : null;
            String updateTimeStr = currency.getUpdateTime() != null ? DateTimeUtil.formatForSqlite(currency.getUpdateTime()) : null;
            String deleteTimeStr = currency.getDeleteTime() != null ? DateTimeUtil.formatForSqlite(currency.getDeleteTime()) : null;
            stmt.setString(1, currency.getTitle());
            stmt.setInt(2, currency.getPosition());
            stmt.setString(3, currency.getCreatedBy());
            stmt.setString(4, currency.getUpdatedBy());
            stmt.setString(5, currency.getDeletedBy());
            stmt.setString(6, createTimeStr);
            stmt.setString(7, deleteTimeStr);
            stmt.setString(8, updateTimeStr);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    currency.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return currency;
    }

    @Override
    public Currency update(Currency currency) {
        String[] columns = new String[CURRENCY_COLUMNS.length - 1];
        System.arraycopy(CURRENCY_COLUMNS, 1, columns, 0, CURRENCY_COLUMNS.length - 1);
        String setClause = String.join("=?, ", columns) + "=?";
        String sql = "UPDATE " + TABLE_CURRENCIES + " SET " + setClause + " WHERE " + COLUMN_ID + "=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String createTimeStr = currency.getCreateTime() != null ? DateTimeUtil.formatForSqlite(currency.getCreateTime()) : null;
            String updateTimeStr = currency.getUpdateTime() != null ? DateTimeUtil.formatForSqlite(currency.getUpdateTime()) : null;
            String deleteTimeStr = currency.getDeleteTime() != null ? DateTimeUtil.formatForSqlite(currency.getDeleteTime()) : null;
            stmt.setString(1, currency.getTitle());
            stmt.setInt(2, currency.getPosition());
            stmt.setString(3, currency.getCreatedBy());
            stmt.setString(4, currency.getUpdatedBy());
            stmt.setString(5, currency.getDeletedBy());
            stmt.setString(6, createTimeStr);
            stmt.setString(7, deleteTimeStr);
            stmt.setString(8, updateTimeStr);
            stmt.setInt(9, currency.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return currency;
    }
} 