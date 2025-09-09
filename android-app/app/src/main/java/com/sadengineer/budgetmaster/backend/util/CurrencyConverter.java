package com.sadengineer.budgetmaster.backend.util;

import android.util.Log;

import com.sadengineer.budgetmaster.backend.constants.UtilConstants;
import com.sadengineer.budgetmaster.backend.entity.Currency;

import java.util.List;

/**
 * Утилитарный класс для конвертации валют
 * Предоставляет методы для работы с обменными курсами
 */
public class CurrencyConverter {
    private static final String TAG = "CurrencyConverter";
    
    /**
     * Конвертировать сумму из одной валюты в другую
     * @param amount сумма в копейках для конвертации
     * @param fromCurrency исходная валюта
     * @param toCurrency целевая валюта
     * @return конвертированная сумма
     */
    public static long convert(long amount, Currency fromCurrency, Currency toCurrency) {
        if (fromCurrency == null || toCurrency == null) {
            Log.e(TAG, UtilConstants.CURRENCY_CONVERTER_ERROR_NULL_CURRENCY);
            return amount;
        }
        // Если валюты одинаковые, возвращаем исходную сумму
        if (fromCurrency.getId() == toCurrency.getId()) {
            return amount;
        }
        // Конвертируем через отображаемую валюту
        long amountInDisplayCurrency = fromCurrency.convertToDisplayCurrency(amount);
        return toCurrency.convertFromDisplayCurrency(amountInDisplayCurrency);
    }
    
    /**
     * Конвертировать сумму в отображаемую валюту
     * @param amount сумма в копейках для конвертации
     * @param currency валюта исходной суммы
     * @return сумма в отображаемой валюте
     */
    public static long convertToDisplayCurrency(long amount, Currency currency) {
        if (currency == null) {
            Log.e(TAG, UtilConstants.CURRENCY_CONVERTER_ERROR_NULL_CURRENCY);
            return amount;
        }
        return currency.convertToDisplayCurrency(amount);
    }
    
    /**
     * Конвертировать сумму из отображаемой валюты
     * @param amount сумма в копейках в отображаемой валюте
     * @param currency целевая валюта
     * @return сумма в целевой валюте
     */
    public static long convertFromDisplayCurrency(long amount, Currency currency) {
        if (currency == null) {
            Log.e(TAG, UtilConstants.CURRENCY_CONVERTER_ERROR_NULL_CURRENCY);
            return amount;
        }
        return currency.convertFromDisplayCurrency(amount);
    }
    
    /**
     * Найти отображаемую валюту в списке валют
     * @param currencies список валют
     * @return отображаемая валюта или null, если не найдена
     */
    public static Currency findDisplayCurrency(List<Currency> currencies) {
        if (currencies == null || currencies.isEmpty()) {
            return null;
        }
        //TODO отображаемой валюты нет, есть валюта для отображения (пересчета)
        for (Currency currency : currencies) {
            if (currency.getExchangeRate() == 1.0) {
                return currency;
            }
        }
        Log.w(TAG, UtilConstants.CURRENCY_CONVERTER_WARNING_DISPLAY_CURRENCY_NOT_FOUND);
        return null;
    }
    
    /**
     * Проверить, есть ли отображаемая валюта в списке
     * @param currencies список валют
     * @return true, если отображаемая валюта найдена
     */
    public static boolean hasDisplayCurrency(List<Currency> currencies) {
        return findDisplayCurrency(currencies) != null;
    }
    
    /**
     * Установить валюту как отображаемую (курс = 1.0)
     * @param currency валюта для установки как отображаемая
     */
    public static void setAsDisplayCurrency(Currency currency) {
        if (currency == null) {
            Log.e(TAG, UtilConstants.CURRENCY_CONVERTER_ERROR_NULL_CURRENCY);
            return;
        }
        currency.setExchangeRate(1.0);
        Log.d(TAG, String.format(UtilConstants.CURRENCY_CONVERTER_INFO_CURRENCY_SET_AS_DISPLAY, currency.getTitle()));
    }
    
    /**
     * Обновить курсы валют относительно новой отображаемой валюты
     * @param currencies список всех валют
     * @param newDisplayCurrency новая отображаемая валюта
     */
    public static void updateExchangeRates(List<Currency> currencies, Currency newDisplayCurrency) {
        if (currencies == null || newDisplayCurrency == null) {
            Log.e(TAG, UtilConstants.CURRENCY_CONVERTER_ERROR_NULL_CURRENCIES_LIST);
            return;
        }
        // Находим старую отображаемую валюту
        Currency oldDisplayCurrency = findDisplayCurrency(currencies);
        if (oldDisplayCurrency == null) {
            Log.w(TAG, UtilConstants.CURRENCY_CONVERTER_WARNING_OLD_DISPLAY_CURRENCY_NOT_FOUND);
            setAsDisplayCurrency(newDisplayCurrency);
            return;
        }
        // Если отображаемая валюта не изменилась, ничего не делаем
        if (oldDisplayCurrency.getId() == newDisplayCurrency.getId()) {
            return;
        }
        // Получаем курс новой отображаемой валюты относительно старой
        double newDisplayCurrencyRate = newDisplayCurrency.getExchangeRate();
        // Обновляем курсы всех валют
        for (Currency currency : currencies) {
            if (currency.getId() == newDisplayCurrency.getId()) {
                // Новая отображаемая валюта
                currency.setExchangeRate(1.0);
            } else if (currency.getId() == oldDisplayCurrency.getId()) {
                // Старая отображаемая валюта становится обычной
                currency.setExchangeRate(1.0 / newDisplayCurrencyRate);
            } else {
                // Остальные валюты: делим на курс новой отображаемой валюты
                currency.setExchangeRate(currency.getExchangeRate() / newDisplayCurrencyRate);
            }
        }
        Log.d(TAG, String.format(UtilConstants.CURRENCY_CONVERTER_INFO_EXCHANGE_RATES_UPDATED, newDisplayCurrency.getTitle()));
    }
}
