package com.sadengineer.budgetmaster.backend.constants;

/**
 * Утилитарные константы приложения
 * Содержит все текстовые сообщения для логирования и другие утилитарные константы
 */
public class UtilConstants {

    // ========================================
    // СООБЩЕНИЯ ДЛЯ CURRENCY CONVERTER
    // ========================================
    
    public static final String CURRENCY_CONVERTER_ERROR_NULL_CURRENCY = "Валюта не может быть null";
    public static final String CURRENCY_CONVERTER_ERROR_NULL_CURRENCIES_LIST = "Список валют или новая отображаемая валюта не могут быть null";
    public static final String CURRENCY_CONVERTER_WARNING_DISPLAY_CURRENCY_NOT_FOUND = "Отображаемая валюта не найдена в списке валют";
    public static final String CURRENCY_CONVERTER_WARNING_OLD_DISPLAY_CURRENCY_NOT_FOUND = "Старая отображаемая валюта не найдена, устанавливаем новую";
    public static final String CURRENCY_CONVERTER_INFO_CURRENCY_SET_AS_DISPLAY = "Валюта %s установлена как отображаемая";
    public static final String CURRENCY_CONVERTER_INFO_EXCHANGE_RATES_UPDATED = "Курсы валют обновлены. Новая отображаемая валюта: %s";

}
