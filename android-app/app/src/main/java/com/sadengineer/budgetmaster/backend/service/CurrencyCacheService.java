package com.sadengineer.budgetmaster.backend.service;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sadengineer.budgetmaster.backend.entity.Currency;

import java.util.HashMap;
import java.util.Map;

/**
 * Сервис для кэширования валют
 * Предоставляет быстрый доступ к названиям валют по ID
 */
public class CurrencyCacheService {
    private static final String TAG = "CurrencyCacheService";
    
    private final CurrencyService mCurrencyService;
    private final Map<Integer, String> mCurrencyCache = new HashMap<>();
    private final MutableLiveData<Boolean> mCacheLoaded = new MutableLiveData<>(false);
    
    public CurrencyCacheService(Context context, String userName) {
        mCurrencyService = new CurrencyService(context, userName);
        loadCurrencies();
    }
    
    /**
     * Загружает валюты в кэш
     */
    private void loadCurrencies() {
        mCurrencyCache.clear();
        mCurrencyService.getAll().observeForever(currencies -> {
            if (currencies != null) {
                for (Currency currency : currencies) {
                    if (currency.getShortName() != null && !currency.getShortName().isEmpty()) {
                        mCurrencyCache.put(currency.getId(), currency.getShortName());
                    }
                }
                Log.d(TAG, "Кэш валют загружен: " + mCurrencyCache.size() + " валют");
                mCacheLoaded.setValue(true);
            }
        });
    }
    
    /**
     * Возвращает короткое название валюты по ID
     * @param currencyId ID валюты
     * @return короткое название валюты или "RUB" по умолчанию
     */
    public String getCurrencyShortName(int currencyId) {
        return mCurrencyCache.getOrDefault(currencyId, "RUB");
    }
    
    /**
     * Проверяет, загружен ли кэш
     * @return true если кэш загружен
     */
    public LiveData<Boolean> isCacheLoaded() {
        return mCacheLoaded;
    }
    
    /**
     * Возвращает количество валют в кэше
     * @return количество валют
     */
    public int getCacheSize() {
        return mCurrencyCache.size();
    }
    
    /**
     * Очищает кэш
     */
    public void clearCache() {
        mCurrencyCache.clear();
        mCacheLoaded.setValue(false);
    }
    
    /**
     * Перезагружает кэш
     */
    public void reloadCache() {
        loadCurrencies();
    }
}
