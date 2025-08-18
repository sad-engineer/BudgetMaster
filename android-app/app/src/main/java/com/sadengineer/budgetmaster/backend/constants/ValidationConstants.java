
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
    
    // Максимальные длины
    public static final int MAX_TITLE_LENGTH = 200;      // Максимальная длина названия
    public static final int MAX_COMMENT_LENGTH = 300;    // Максимальная длина комментария
    public static final int MIN_CURRENCY_TITLE_LENGTH = 3; // Минимальная длина названия валюты
    public static final int MAX_CURRENCY_TITLE_LENGTH = 80; // Максимальная длина названия валюты
    public static final int MIN_CURRENCY_SHORT_NAME_LENGTH = 1; // Минимальная длина короткого имени валюты
    public static final int MAX_CURRENCY_SHORT_NAME_LENGTH = 3; // Максимальная длина короткого имени валюты
    
    // Регулярные выражения
    public static final String TITLE_PATTERN = "^[а-яА-Яa-zA-Z0-9\\s\\-\\(\\)]+$";
    public static final String CURRENCY_TITLE_PATTERN = "^[а-яА-Яa-zA-Z0-9\\s\\-]+$";
    public static final String CURRENCY_SHORT_NAME_PATTERN = "^[а-яА-Яa-zA-Z0-9\\s\\-₽$€¥£¢₹₩₪₦₨₴₸₺₼₾₿]+$";
    public static final String COMMENT_PATTERN = "^[а-яА-Яa-zA-Z0-9\\s\\-\\.,!?;:()]+$";
    
    // Сообщения об ошибках
    public static final String ERROR_NULL_ENTITY = "Сущность не может быть null";
    public static final String ERROR_NULL_FIELD = " не может быть null";
    public static final String ERROR_EMPTY_FIELD = " не может быть пустым";
    public static final String ERROR_TOO_LONG = " не может быть длиннее ";
    public static final String ERROR_INVALID_CHARS = " содержит недопустимые символы";
    public static final String ERROR_MUST_BE_POSITIVE = " должен быть положительным";
    public static final String ERROR_CANNOT_BE_NEGATIVE = " не может быть отрицательным";
    public static final String ERROR_MUST_BE_POSITIVE_VALUE = " должно быть положительным";
    public static final String ERROR_CANNOT_BE_NEGATIVE_VALUE = " не может быть отрицательным";
    public static final String ERROR_OUT_OF_RANGE = " должно быть в диапазоне от ";
    public static final String ERROR_CANNOT_BE_FUTURE = " не может быть в будущем";
    public static final String ERROR_SELF_PARENT = "Категория не может быть родителем самой себя";
    public static final String ERROR_UNKNOWN_ENTITY_TYPE = "Неизвестный тип сущности: ";
    
    // Сообщения для типов
    public static final String ERROR_ACCOUNT_TYPE = "Тип счета должен быть " + ACCOUNT_TYPE_CURRENT + 
        " (расчетный), " + ACCOUNT_TYPE_SAVINGS + " (сберегательный) или " + ACCOUNT_TYPE_CREDIT + " (кредитный)";
    
    public static final String ERROR_OPERATION_TYPE = "Тип операции должен быть " + OPERATION_TYPE_EXPENSE + 
        " (расход) или " + OPERATION_TYPE_INCOME + " (доход)";
    
    public static final String ERROR_CATEGORY_TYPE = "Тип категории должен быть " + CATEGORY_TYPE_PARENT + 
        " (родительская) или " + CATEGORY_TYPE_CHILD + " (дочерняя)";
    
    public static final String ERROR_CLOSED_STATUS = "Статус закрытия должен быть " + STATUS_OPEN + 
        " (открыт) или " + STATUS_CLOSED + " (закрыт)";
    
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
} 