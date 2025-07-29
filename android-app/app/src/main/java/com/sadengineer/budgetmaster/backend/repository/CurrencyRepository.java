// -*- coding: utf-8 -*-
package com.sadengineer.budgetmaster.backend.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sadengineer.budgetmaster.backend.dao.CurrencyDao;
import com.sadengineer.budgetmaster.backend.database.BudgetMasterDatabase;
import com.sadengineer.budgetmaster.backend.entity.Currency;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository класс для работы с Currency Entity
 */
public class CurrencyRepository {
    
    private final CurrencyDao currencyDao;
    private final ExecutorService executorService;
    
    public CurrencyRepository(Context context) {
        BudgetMasterDatabase database = BudgetMasterDatabase.getDatabase(context);
        this.currencyDao = database.currencyDao();
        this.executorService = Executors.newFixedThreadPool(4);
    }
    
    // Получить все активные валюты
    public LiveData<List<Currency>> getAllActiveCurrencies() {
        MutableLiveData<List<Currency>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Currency> currencies = currencyDao.getAllActiveCurrencies();
            liveData.postValue(currencies);
        });
        return liveData;
    }
    
    // Получить все валюты
    public LiveData<List<Currency>> getAllCurrencies() {
        MutableLiveData<List<Currency>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Currency> currencies = currencyDao.getAllCurrencies();
            liveData.postValue(currencies);
        });
        return liveData;
    }
    
    // Получить валюту по ID
    public LiveData<Currency> getCurrencyById(int id) {
        MutableLiveData<Currency> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            Currency currency = currencyDao.getCurrencyById(id);
            liveData.postValue(currency);
        });
        return liveData;
    }
    
    // Получить валюту по названию
    public LiveData<Currency> getCurrencyByTitle(String title) {
        MutableLiveData<Currency> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            Currency currency = currencyDao.getCurrencyByTitle(title);
            liveData.postValue(currency);
        });
        return liveData;
    }
    
    // Получить валюту по коду
    public LiveData<Currency> getCurrencyByCode(String code) {
        MutableLiveData<Currency> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            Currency currency = currencyDao.getCurrencyByCode(code);
            liveData.postValue(currency);
        });
        return liveData;
    }
    
    // Получить валюту по умолчанию
    public LiveData<Currency> getDefaultCurrency() {
        MutableLiveData<Currency> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            Currency currency = currencyDao.getDefaultCurrency();
            liveData.postValue(currency);
        });
        return liveData;
    }
    
    // Получить валюту по позиции
    public LiveData<Currency> getCurrencyByPosition(int position) {
        MutableLiveData<Currency> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            Currency currency = currencyDao.getCurrencyByPosition(position);
            liveData.postValue(currency);
        });
        return liveData;
    }
    
    // Вставить новую валюту
    public void insertCurrency(Currency currency, String createdBy) {
        executorService.execute(() -> {
            currency.setCreateTime(LocalDateTime.now());
            currency.setCreatedBy(createdBy);
            currency.setUpdateTime(LocalDateTime.now());
            currency.setUpdatedBy(createdBy);
            currencyDao.insertCurrency(currency);
        });
    }
    
    // Обновить валюту
    public void updateCurrency(Currency currency, String updatedBy) {
        executorService.execute(() -> {
            currency.setUpdateTime(LocalDateTime.now());
            currency.setUpdatedBy(updatedBy);
            currencyDao.updateCurrency(currency);
        });
    }
    
    // Удалить валюту (soft delete)
    public void deleteCurrency(int currencyId, String deletedBy) {
        executorService.execute(() -> {
            currencyDao.softDeleteCurrency(currencyId, LocalDateTime.now().toString(), deletedBy);
        });
    }
    
    // Удалить валюту по названию
    public void deleteCurrencyByTitle(String title, String deletedBy) {
        executorService.execute(() -> {
            currencyDao.softDeleteCurrencyByTitle(title, LocalDateTime.now().toString(), deletedBy);
        });
    }
    
    // Получить все удаленные валюты
    public LiveData<List<Currency>> getAllDeletedCurrencies() {
        MutableLiveData<List<Currency>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Currency> currencies = currencyDao.getAllDeletedCurrencies();
            liveData.postValue(currencies);
        });
        return liveData;
    }
    
    // Получить максимальную позицию
    public LiveData<Integer> getMaxPosition() {
        MutableLiveData<Integer> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            Integer maxPos = currencyDao.getMaxPosition();
            liveData.postValue(maxPos != null ? maxPos : 0);
        });
        return liveData;
    }
    
    // Восстановить валюту
    public void restoreCurrency(int currencyId, String updatedBy) {
        executorService.execute(() -> {
            currencyDao.restoreCurrency(currencyId, LocalDateTime.now().toString(), updatedBy);
        });
    }
    
    // Получить количество активных валют
    public LiveData<Integer> getActiveCurrenciesCount() {
        MutableLiveData<Integer> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            int count = currencyDao.getActiveCurrenciesCount();
            liveData.postValue(count);
        });
        return liveData;
    }
} 