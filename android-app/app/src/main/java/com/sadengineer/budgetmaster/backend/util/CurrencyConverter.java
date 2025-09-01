package com.sadengineer.budgetmaster.backend.util;

import android.util.Log;

import com.sadengineer.budgetmaster.backend.entity.Currency;
import androidx.core.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
            Log.e(TAG, "Валюта не может быть null");
            return amount;
        }
        
        // Если валюты одинаковые, возвращаем исходную сумму
        if (fromCurrency.getId() == toCurrency.getId()) {
            return amount;
        }
        
        // Конвертируем через главную валюту
        long amountInMainCurrency = fromCurrency.convertToMainCurrency(amount);
        return toCurrency.convertFromMainCurrency(amountInMainCurrency);
    }
    
    /**
     * Конвертировать сумму в главную валюту
     * @param amount сумма в копейках для конвертации
     * @param currency валюта исходной суммы
     * @return сумма в главной валюте
     */
    public static long convertToMainCurrency(long amount, Currency currency) {
        if (currency == null) {
            Log.e(TAG, "Валюта не может быть null");
            return amount;
        }
        
        return currency.convertToMainCurrency(amount);
    }
    
    /**
     * Конвертировать сумму из главной валюты
     * @param amount сумма в копейках в главной валюте
     * @param currency целевая валюта
     * @return сумма в целевой валюте
     */
    public static long convertFromMainCurrency(long amount, Currency currency) {
        if (currency == null) {
            Log.e(TAG, "Валюта не может быть null");
            return amount;
        }
        
        return currency.convertFromMainCurrency(amount);
    }
    
    /**
     * Найти главную валюту в списке валют
     * @param currencies список валют
     * @return главная валюта или null, если не найдена
     */
    public static Currency findMainCurrency(List<Currency> currencies) {
        if (currencies == null || currencies.isEmpty()) {
            return null;
        }
        
        //TODO главной валюты нет, есть валюта для отображения (пересчета)
        for (Currency currency : currencies) {
            if (currency.getExchangeRate() == 1.0) {
                return currency;
            }
        }
        
        Log.w(TAG, "Главная валюта не найдена в списке валют");
        return null;
    }
    
    /**
     * Проверить, есть ли главная валюта в списке
     * @param currencies список валют
     * @return true, если главная валюта найдена
     */
    public static boolean hasMainCurrency(List<Currency> currencies) {
        return findMainCurrency(currencies) != null;
    }
    
    /**
     * Установить валюту как главную (курс = 1.0)
     * @param currency валюта для установки как главная
     */
    public static void setAsMainCurrency(Currency currency) {
        if (currency == null) {
            Log.e(TAG, "Валюта не может быть null");
            return;
        }
        
        currency.setExchangeRate(1.0);
        Log.d(TAG, "Валюта " + currency.getTitle() + " установлена как главная");
    }
    
    /**
     * Обновить курсы валют относительно новой главной валюты
     * @param currencies список всех валют
     * @param newMainCurrency новая главная валюта
     */
    public static void updateExchangeRates(List<Currency> currencies, Currency newMainCurrency) {
        if (currencies == null || newMainCurrency == null) {
            Log.e(TAG, "Список валют или новая главная валюта не могут быть null");
            return;
        }
        
        // Находим старую главную валюту
        Currency oldMainCurrency = findMainCurrency(currencies);
        if (oldMainCurrency == null) {
            Log.w(TAG, "Старая главная валюта не найдена, устанавливаем новую");
            setAsMainCurrency(newMainCurrency);
            return;
        }
        
        // Если главная валюта не изменилась, ничего не делаем
        if (oldMainCurrency.getId() == newMainCurrency.getId()) {
            return;
        }
        
        // Получаем курс новой главной валюты относительно старой
        double newMainCurrencyRate = newMainCurrency.getExchangeRate();
        
        // Обновляем курсы всех валют
        for (Currency currency : currencies) {
            if (currency.getId() == newMainCurrency.getId()) {
                // Новая главная валюта
                currency.setExchangeRate(1.0);
            } else if (currency.getId() == oldMainCurrency.getId()) {
                // Старая главная валюта становится обычной
                currency.setExchangeRate(1.0 / newMainCurrencyRate);
            } else {
                // Остальные валюты: делим на курс новой главной валюты
                currency.setExchangeRate(currency.getExchangeRate() / newMainCurrencyRate);
            }
        }
        
        Log.d(TAG, "Курсы валют обновлены. Новая главная валюта: " + newMainCurrency.getTitle());
    }
    
    /**
     * Валидация курса валюты
     * @param exchangeRate курс для проверки
     * @return true, если курс валиден
     * TODO перенести в валидацию
     */
    public static boolean isValidExchangeRate(double exchangeRate) {
        return exchangeRate > 0.0 && !Double.isInfinite(exchangeRate) && !Double.isNaN(exchangeRate);
    }
}
