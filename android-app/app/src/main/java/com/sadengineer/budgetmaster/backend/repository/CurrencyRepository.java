// -*- coding: utf-8 -*-
package com.sadengineer.budgetmaster.backend.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.sadengineer.budgetmaster.backend.dao.CurrencyDao;
import com.sadengineer.budgetmaster.backend.database.BudgetMasterDatabase;
import com.sadengineer.budgetmaster.backend.entity.Currency;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository класс для работы с Currency Entity
 */
public class CurrencyRepository {
    
    private final CurrencyDao dao;
    
    public CurrencyRepository(Context context) {
        BudgetMasterDatabase database = BudgetMasterDatabase.getDatabase(context);
        this.dao = database.currencyDao();
    }
    
    /**
     * Получить все валюты (включая удаленные)
     * @return LiveData со списком всех валют
     */
    public LiveData<List<Currency>> getAll() {
        return dao.getAll();
    }
    
    /**
     * Получить все активные валюты
     * @return LiveData со списком активных валют
     */
    public LiveData<List<Currency>> getAllActive() {
        return dao.getAllActive();
    }
    
    /**
     * Получить все удаленные валюты
     * @return LiveData со списком удаленных валют
     */
    public LiveData<List<Currency>> getAllDeleted() {
        return dao.getAllDeleted();
    }
    
    /**
     * Получить валюту по ID (включая удаленные)
     * @param id ID валюты
     * @return LiveData с валютой
     */
    public LiveData<Currency> getById(int id) {
        return dao.getById(id);
    }
    
    /**
     * Получить валюту по названию (включая удаленные)
     * @param title название валюты
     * @return LiveData с валютой
     */
    public LiveData<Currency> getByTitle(String title) {
        return dao.getByTitle(title);
    }
    
    /**
     * Получить валюту по позиции (включая удаленные)
     * @param position позиция валюты
     * @return LiveData с валютой
     */
    public LiveData<Currency> getByPosition(int position) {
        return dao.getByPosition(position);
    }
    
    /**
     * Вставить новую валюту
     * @param currency валюта для вставки
     * @return LiveData с вставленной валютой. Если валюта не была вставлена, то будет выброшено исключение.
     */
    public LiveData<Currency> insert(Currency currency) {
        long id = dao.insert(currency);
        if (id == -1) {
            throw new IllegalArgumentException("Не удалось вставить валюту");
        }
        return dao.getById((int)id);
    }
    
    /**
     * Обновить валюту
     * @param currency валюта для обновления
     */
    public void update(Currency currency) {
        dao.update(currency);
    }
    
    /**
     * Удалить валюту (полное удаление из БД)
     * @param currency валюта для удаления
     */
    public void delete(Currency currency) {
        dao.delete(currency);
    }
    
    /**
     * Удалить все валюты
     */
    public void deleteAll() {
        dao.deleteAll();
    }
    
    /**
     * Получить максимальную позицию среди всех валют
     * @return LiveData с максимальной позицией
     */
    public int getMaxPosition() {
        Integer maxPos = dao.getMaxPosition();
        return maxPos != null ? maxPos : 0;
    }
    
    /**
     * Получить количество активных валют
     * @return LiveData с количеством активных валют
     */
    public int getActiveCount() {
        return dao.countActive();
    }

    /**
     * Получить валюты по подстроке в названии
     * @param searchQuery подстрока для поиска
     * @return LiveData с списком валют
     */
    public LiveData<List<Currency>> searchByTitle(String searchQuery) {
        return dao.searchByTitle(searchQuery);
    }
    
    /**
     * Получить общее количество валют
     * @return LiveData с общим количеством валют
     */
    public int getCount() {
        return dao.count();
    }
} 