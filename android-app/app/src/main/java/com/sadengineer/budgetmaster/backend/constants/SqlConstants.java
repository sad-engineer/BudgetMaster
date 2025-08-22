package com.sadengineer.budgetmaster.backend.constants;


import com.sadengineer.budgetmaster.backend.entity.EntityFilter;

/**
 * Константы для SQL запросов
 */
public class SqlConstants {
    // Строковые значения из enum EntityFilter
    public static final String FILTER_ACTIVE = EntityFilter.ACTIVE.name();
    public static final String FILTER_DELETED = EntityFilter.DELETED.name();
    public static final String FILTER_ALL = EntityFilter.ALL.name();

    // Имена таблиц
    public static final String TABLE_ACCOUNTS = RepositoryConstants.TABLE_ACCOUNTS;
    public static final String TABLE_BUDGETS = RepositoryConstants.TABLE_BUDGETS;
    public static final String TABLE_CATEGORIES = RepositoryConstants.TABLE_CATEGORIES;
    public static final String TABLE_CURRENCIES = RepositoryConstants.TABLE_CURRENCIES;
    public static final String TABLE_OPERATIONS = RepositoryConstants.TABLE_OPERATIONS;

    /**
     * Условие для активных записей
     */
    public static final String ACTIVE_CONDITION = "deleteTime IS NULL";
    
    /**
     * Условие для удаленных записей
     */
    public static final String DELETED_CONDITION = "deleteTime IS NOT NULL";

    /**
     * Условие фильтрации по статусу удаления
     * Используется для фильтрации операций по EntityFilter
     * необходимо в параметрах запроса передать filter = :filter
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     */
    public static final String ENTITY_FILTER_CONDITION = 
        "((:filter = '" + FILTER_ACTIVE + "' AND " + ACTIVE_CONDITION + ") OR " +
        "(:filter = '" + FILTER_DELETED + "' AND " + DELETED_CONDITION + ") OR " +
        "(:filter = '" + FILTER_ALL + "'))";
    
    /**
     * Условие для фильтрации по типу
     * Используется для фильтрации операций по типу
     * необходимо в параметрах запроса передать type = :type
     * @param type тип операции (INCOME, EXPENSE)
     */
    public static final String TYPE_CONDITION = "type = :type";

    /**
     * Условие для фильтрации по счету
     * Используется для фильтрации операций по счету
     * необходимо в параметрах запроса передать accountId = :accountId
     * @param accountId ID счета
     */
    public static final String ACCOUNT_CONDITION = "accountId = :accountId";
    
    /**
     * Условие для фильтрации по категории
     * Используется для фильтрации операций по категории
     * необходимо в параметрах запроса передать categoryId = :categoryId
     * @param categoryId ID категории
     */
    public static final String CATEGORY_CONDITION = "categoryId = :categoryId";

    /**
     * Условие для фильтрации по валюте
     * Используется для фильтрации операций по валюте
     * необходимо в параметрах запроса передать currencyId = :currencyId
     * @param currencyId ID валюты
     */
    public static final String CURRENCY_CONDITION = "currencyId = :currencyId";

    /**
     * Условие для фильтрации по ID
     * Используется для фильтрации операций по ID
     * необходимо в параметрах запроса передать id = :id
     * @param id ID операции
     */
    public static final String ID_CONDITION = "id = :id";

    /**
     * Условие для фильтрации по позиции
     * Используется для фильтрации по позиции
     * необходимо в параметрах запроса передать position = :position
     * @param position позиция
     */
    public static final String POSITION_CONDITION = "position = :position";

    /**
     * Условие для фильтрации по названию
     * Используется для фильтрации по названию
     * необходимо в параметрах запроса передать title = :title
     * @param title название
     */
    public static final String TITLE_CONDITION = "title = :title";

    /**
     * Условие для фильтрации по короткому названию
     * Используется для фильтрации по короткому названию
     * необходимо в параметрах запроса передать shortName = :shortName
     * @param shortName короткое название
     */
    public static final String SHORT_NAME_CONDITION = "shortName = :shortName";

    /**
     * Условие для фильтрации по дате
     * Используется для фильтрации операций по дате
     * необходимо в параметрах запроса передать operationDate = :date
     * @param date дата операции
     */
    public static final String DATE_CONDITION = "operationDate = :date";


    /**
     * Условие для фильтрации по году
     * Используется для фильтрации операций по году
     * необходимо в параметрах запроса передать strftime('%Y', operationDate) = :year
     * @param year год
     */
    public static final String YEAR_CONDITION = "strftime('%Y', operationDate) = :year";
    
    /**
     * Условие для фильтрации по месяцу
     * Используется для фильтрации операций по месяцу
     * необходимо в параметрах запроса передать strftime('%m', operationDate) = :month
     * @param month месяц
     */
    public static final String MONTH_CONDITION = "strftime('%m', operationDate) = :month";
    
    /**
     * Условие для фильтрации по дате
     * Используется для фильтрации операций по дате
     * необходимо в параметрах запроса передать operationDate BETWEEN :startDate AND :endDate
     * @param startDate начало периода
     * @param endDate конец периода
     */
    public static final String DATE_RANGE_CONDITION = "operationDate BETWEEN :startDate AND :endDate";
    
    /**
     * Условие для фильтрации по году и месяцу
     * Используется для фильтрации операций по году и месяцу
     * необходимо в параметрах запроса передать strftime('%Y', operationDate) = :year AND strftime('%m', operationDate) = :month
     * @param year год
     * @param month месяц
     */
    public static final String YEAR_MONTH_CONDITION = "strftime('%Y', operationDate) = :year AND strftime('%m', operationDate) = :month";

    /**
     * Условие сортировки по позиции
     * Используется для сортировки операций по позиции
     * необходимо в параметрах запроса передать position ASC
     */
    public static final String POSITION_SORT_CONDITION = "position ASC";

    /**
     * Условие сортировки по позиции (счета с позицией 0 в конце)
     * Используется для сортировки счетов по позиции
     * необходимо в параметрах запроса передать position ASC
     */
    public static final String POSITION_SORT_CONDITION_0_END = "CASE WHEN position = 0 THEN 1 ELSE 0 END, position ASC";
    
}  