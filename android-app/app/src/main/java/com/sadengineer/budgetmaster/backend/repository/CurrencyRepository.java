
package com.sadengineer.budgetmaster.backend.repository;

import android.content.Context;
import android.util.Log;

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
    private static final String TAG = "CurrencyRepository";
    
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
        try {
            if (currency == null) {
                throw new IllegalArgumentException("Валюта не может быть null");
            }
            
            if (currency.getTitle() == null || currency.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("Название валюты не может быть пустым");
            }
            
            // Проверяем, что база данных доступна
            if (dao == null) {
                throw new RuntimeException("DAO не инициализирован");
            }
            
            // Проверяем, что валюта с таким названием уже не существует
            Currency existingCurrency = getByTitle(currency.getTitle().trim()).getValue();
            if (existingCurrency != null) {
                throw new IllegalArgumentException("Валюта с названием '" + currency.getTitle().trim() + "' уже существует");
            }
            
            long id = dao.insert(currency);
            if (id == -1) {
                // Если вставка не удалась, попробуем получить существующую валюту
                Log.w(TAG, "Попытка вставить валюту с названием '" + currency.getTitle().trim() + "'");

                Currency existing = getByTitle(currency.getTitle().trim()).getValue();
                if (existing != null) {
                    Log.w(TAG, "Валюта с названием '" + currency.getTitle().trim() + "' уже существует (ID: " + existing.getId() + ")");
                    return getByTitle(currency.getTitle().trim());
                } else {
                    throw new RuntimeException("Не удалось вставить валюту в базу данных");
                }
            }
            
            return dao.getById((int)id);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при вставке валюты: " + e.getMessage(), e);
            throw e;
        }
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
     * @return максимальная позиция
     */
    public int getMaxPosition() {
        Integer maxPos = dao.getMaxPosition();
        return maxPos != null ? maxPos : 0;
    }
    
    /**
     * Получить количество активных валют
     * @return количество активных валют
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
     * @return общее количество валют
     */
    public int getCount() {
        return dao.count();
    }
} 