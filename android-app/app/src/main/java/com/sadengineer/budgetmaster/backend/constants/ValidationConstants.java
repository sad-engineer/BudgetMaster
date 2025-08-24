
package com.sadengineer.budgetmaster.backend.constants;

/**
 * Константы для валидации
 */
public class ValidationConstants {
    
    // Ссылки на типы из ModelConstants (для удобства использования в валидаторах)
    public static final int ACCOUNT_TYPE_CURRENT = ModelConstants.ACCOUNT_TYPE_CURRENT;    // Расчетный счет
    public static final int ACCOUNT_TYPE_SAVINGS = ModelConstants.ACCOUNT_TYPE_SAVINGS;    // Сберегательный счет
    public static final int ACCOUNT_TYPE_CREDIT = ModelConstants.ACCOUNT_TYPE_CREDIT;     // Кредитный счет
    
    public static final int OPERATION_TYPE_EXPENSE = ModelConstants.OPERATION_TYPE_EXPENSE;  // Расход
    public static final int OPERATION_TYPE_INCOME = ModelConstants.OPERATION_TYPE_INCOME;   // Доход
    
    public static final int CATEGORY_TYPE_PARENT = ModelConstants.CATEGORY_TYPE_PARENT;    // Родительская категория
    public static final int CATEGORY_TYPE_CHILD = ModelConstants.CATEGORY_TYPE_CHILD;     // Дочерняя категория
    
    public static final int STATUS_OPEN = ModelConstants.ACCOUNT_STATUS_OPEN;             // Открыт
    public static final int STATUS_CLOSED = ModelConstants.ACCOUNT_STATUS_CLOSED;           // Закрыт
    
    // Длины
    public static final int MAX_TITLE_LENGTH = 200;      // Максимальная длина названия
    public static final int MIN_TITLE_LENGTH = 1;        // Минимальная длина названия
    public static final int MAX_COMMENT_LENGTH = 300;    // Максимальная длина комментария
    public static final int MIN_COMMENT_LENGTH = 1;      // Минимальная длина комментария
    public static final int MIN_CURRENCY_TITLE_LENGTH = 3; // Минимальная длина названия валюты
    public static final int MAX_CURRENCY_TITLE_LENGTH = 80; // Максимальная длина названия валюты
    public static final int MIN_CURRENCY_SHORT_NAME_LENGTH = 1; // Минимальная длина короткого имени валюты
    public static final int MAX_CURRENCY_SHORT_NAME_LENGTH = 3; // Максимальная длина короткого имени валюты
    
    // Значения
    public static final double MIN_AMOUNT_VALUE = 0.01; // Минимальное значение суммы
    public static final double MAX_AMOUNT_VALUE = ModelConstants.MAX_AMOUNT; // Максимальное значение суммы

    // Регулярные выражения
    public static final String TITLE_PATTERN = "^[а-яА-Яa-zA-Z0-9\\s\\-\\(\\)]+$";
    public static final String CURRENCY_SHORT_NAME_PATTERN = "^[а-яА-Яa-zA-Z0-9\\s\\-₽$€¥£¢₹₩₪₦₨₴₸₺₼₾₿]+$";
    public static final String COMMENT_PATTERN = "^[а-яА-Яa-zA-Z0-9\\s\\-\\.,!?;:()]+$";

    // Сообщения об ошибках для валют
    public static final String ERROR_CURRENCY_NULL = "Валюта не может быть null";
    public static final String ERROR_CURRENCY_TITLE_ALREADY_EXISTS = "Валюта с названием '%s' уже существует";
    public static final String ERROR_CURRENCY_SHORT_NAME_ALREADY_EXISTS = "Валюта с коротким именем '%s' уже существует";
    public static final String ERROR_CURRENCY_TITLE_EMPTY = "Название валюты не может быть пустым";
    public static final String ERROR_CURRENCY_SHORT_NAME_EMPTY = "Короткое название валюты не может быть пустым";
    public static final String ERROR_CURRENCY_TITLE_TOO_SHORT = "Название валюты должно содержать минимум %d символов";
    public static final String ERROR_CURRENCY_TITLE_TOO_LONG = "Название валюты не может быть длиннее %d символов";
    public static final String ERROR_CURRENCY_SHORT_NAME_TOO_SHORT = "Короткое название валюты должно содержать минимум %d символов";
    public static final String ERROR_CURRENCY_SHORT_NAME_TOO_LONG = "Короткое название валюты не может быть длиннее %d символов";
    public static final String ERROR_CURRENCY_TITLE_INVALID_CHARS = "Название валюты содержит недопустимые символы";
    public static final String ERROR_CURRENCY_SHORT_NAME_INVALID_CHARS = "Короткое название валюты содержит недопустимые символы";
    public static final String ERROR_CURRENCY_ID_INVALID = "ID валюты должен быть положительным";
    public static final String ERROR_CURRENCY_POSITION_INVALID = "Позиция валюты должна быть положительной";
    public static final String ERROR_CURRENCY_CREATE_TIME_INVALID = "Время создания валюты не может быть в будущем";
    public static final String ERROR_CURRENCY_UPDATE_TIME_INVALID = "Время обновления валюты не может быть в будущем";
    public static final String ERROR_CURRENCY_CREATED_BY_INVALID = "Имя пользователя, создавшего валюту, не может быть пустым";
    public static final String ERROR_CURRENCY_UPDATED_BY_INVALID = "Имя пользователя, обновившего валюту, не может быть пустым";
    
    // Сообщения об ошибках для счетов
    public static final String ERROR_ACCOUNT_TITLE_EMPTY = "Название счета не может быть пустым";
    public static final String ERROR_ACCOUNT_TITLE_TOO_SHORT = "Название счета должно содержать минимум %d символов";
    public static final String ERROR_ACCOUNT_TITLE_TOO_LONG = "Название счета не может быть длиннее %d символов";
    public static final String ERROR_ACCOUNT_TITLE_INVALID_CHARS = "Название счета содержит недопустимые символы";
    public static final String ERROR_ACCOUNT_BALANCE_EMPTY = "Баланс счета не может быть пустым";
    public static final String ERROR_ACCOUNT_BALANCE_MIN = "Баланс счета не может быть меньше %f";
    public static final String ERROR_ACCOUNT_BALANCE_MAX = "Баланс счета не может быть больше %f";
    public static final String ERROR_ACCOUNT_TYPE_INVALID = "Тип счета должен быть одним из допустимых значений";
    public static final String ERROR_CURRENCY_ID_MIN = "ID валюты не может быть меньше %d";
    public static final String ERROR_CURRENCY_ID_MAX = "ID валюты не может быть больше %d";
    public static final String ERROR_CLOSED_STATUS = "Статус закрытия должен быть одним из допустимых значений: " + STATUS_OPEN + " (открыт) или " + STATUS_CLOSED + " (закрыт)";

    // Сообщения об ошибках для бюджетов
    public static final String ERROR_BUDGET_AMOUNT_MIN = "Сумма бюджета не может быть меньше %f";
    public static final String ERROR_BUDGET_AMOUNT_MAX = "Сумма бюджета не может быть больше %f";
    public static final String ERROR_BUDGET_CATEGORY_ID_INVALID = "ID категории бюджета должен быть положительным";
    public static final String ERROR_BUDGET_CATEGORY_ID_NOT_FOUND = "ID категории бюджета нет в базе";
    public static final String ERROR_BUDGET_CURRENCY_ID_INVALID = "ID валюты бюджета должен быть положительным";
    public static final String ERROR_BUDGET_CURRENCY_ID_NOT_FOUND = "ID валюты бюджета нет в базе";
    public static final String ERROR_BUDGET_AMOUNT_EMPTY = "Сумма бюджета не может быть пустой";
    
    // Сообщения об ошибках для категорий
    public static final String ERROR_CATEGORY_TITLE_EMPTY = "Название категории не может быть пустым";
    public static final String ERROR_CATEGORY_TITLE_TOO_SHORT = "Название категории должно содержать минимум %d символов";
    public static final String ERROR_CATEGORY_TITLE_TOO_LONG = "Название категории не может быть длиннее %d символов";
    public static final String ERROR_CATEGORY_TITLE_INVALID_CHARS = "Название категории содержит недопустимые символы";
    public static final String ERROR_CATEGORY_OPERATION_TYPE_INVALID = "Тип операции категории должен быть одним из допустимых значений: " + OPERATION_TYPE_EXPENSE + " (расход) или " + OPERATION_TYPE_INCOME + " (доход)";
    public static final String ERROR_CATEGORY_TYPE_INVALID = "Тип категории должен быть одним из допустимых значений: " + CATEGORY_TYPE_PARENT + " (родительская) или " + CATEGORY_TYPE_CHILD + " (дочерняя)";
    public static final String ERROR_CATEGORY_PARENT_ID_INVALID = "ID родителя категории не может быть меньше %d";
    public static final String ERROR_CATEGORY_PARENT_ID_NOT_FOUND = "ID родителя категории нет в базе";
    
    // Сообщения об ошибках для операций
    public static final String ERROR_OPERATION_TYPE_INVALID = "Тип операции должен быть одним из допустимых значений: " + OPERATION_TYPE_EXPENSE + " (расход) или " + OPERATION_TYPE_INCOME + " (доход)";
    public static final String ERROR_OPERATION_DATE_EMPTY = "Дата операции не может быть пустой";
    public static final String ERROR_OPERATION_AMOUNT_INVALID = "Сумма операции должна быть в допустимом диапазоне";
    public static final String ERROR_OPERATION_COMMENT_EMPTY = "Комментарий операции не может быть пустым";
    public static final String ERROR_OPERATION_COMMENT_TOO_SHORT = "Комментарий операции должен содержать минимум %d символов";
    public static final String ERROR_OPERATION_COMMENT_TOO_LONG = "Комментарий операции не может быть длиннее %d символов";
    public static final String ERROR_OPERATION_COMMENT_INVALID_CHARS = "Комментарий операции содержит недопустимые символы";
    public static final String ERROR_OPERATION_CATEGORY_ID_INVALID = "ID категории операции не может быть меньше %d";
    public static final String ERROR_OPERATION_CATEGORY_ID_NOT_FOUND = "ID категории операции нет в базе";
    public static final String ERROR_OPERATION_ACCOUNT_ID_INVALID = "ID счета операции не может быть меньше %d";
    public static final String ERROR_OPERATION_ACCOUNT_ID_NOT_FOUND = "ID счета операции нет в базе";
    public static final String ERROR_OPERATION_CURRENCY_ID_INVALID = "ID валюты операции не может быть меньше %d";
    public static final String ERROR_OPERATION_CURRENCY_ID_NOT_FOUND = "ID валюты операции нет в базе";
    public static final String ERROR_OPERATION_AMOUNT_EMPTY = "Сумма операции не может быть пустой";
        
    
} 