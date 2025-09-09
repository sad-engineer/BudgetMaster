package com.sadengineer.budgetmaster.backend.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.sadengineer.budgetmaster.backend.dao.CurrencyDao;
import com.sadengineer.budgetmaster.backend.database.BudgetMasterDatabase;
import com.sadengineer.budgetmaster.backend.entity.Currency;
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;

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
     * Получить все валюты по фильтру
     * @param filter фильтр для выборки валют (ACTIVE, DELETED, ALL)
     * @return LiveData со списком всех валют
     */
    public LiveData<List<Currency>> getAll(EntityFilter filter) {
        return dao.getAll(filter);
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
     * Получить валюту по ID синхронно (включая удаленные)
     * @param id ID валюты
     * @return валюта
     */
    public Currency getByIdSync(int id) {
        return dao.getByIdSync(id);
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
     * Получить валюту по короткому имени (включая удаленные)
     * @param shortName короткое имя валюты
     * @return LiveData с валютой
     */
    public LiveData<Currency> getByShortName(String shortName) {
        return dao.getByShortName(shortName);
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
     * @return LiveData с вставленной валютой
     */
    public LiveData<Currency> insert(Currency currency) {
        long id = dao.insert(currency);
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
     * @return максимальная позиция
     */
    public int getMaxPosition() {
        return dao.getMaxPosition();
    }

    /**
     * Сдвинуть позиции валют вверх начиная с указанной позиции
     * @param fromPosition позиция, с которой начинается сдвиг
     */
    public void shiftPositionsUp(int fromPosition) {
        dao.shiftPositionsUp(fromPosition);
    }
    
    /**
     * Сдвинуть позиции валют вниз начиная с указанной позиции
     * @param fromPosition позиция, с которой начинается сдвиг
     */
    public void shiftPositionsDown(int fromPosition) {
        dao.shiftPositionsDown(fromPosition);
    }
    
    /**
     * Получить количество валют
     * @param filter фильтр для выборки валют
     * @return количество валют
     */
    public int getCount(EntityFilter filter) {
        return dao.count(filter);
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
     * Получить валюты по подстроке в названии или коротком имени
     * @param searchQuery подстрока для поиска
     * @return LiveData с списком валют
     */
    public LiveData<List<Currency>> searchByTitleOrShortName(String searchQuery) {
        return dao.searchByTitleOrShortName(searchQuery);
    }
    
    /**
     * Проверить существование валюты с указанным названием
     * @param title название валюты
     * @return true если валюта существует, false если нет
     */
    public boolean existsByTitle(String title) {
        return dao.existsByTitle(title);
    }
    
    /**
     * Проверить существование валюты с указанным коротким именем
     * @param shortName короткое имя валюты
     * @return true если валюта существует, false если нет
     */
    public boolean existsByShortName(String shortName) {
        return dao.existsByShortName(shortName);
    }

    /**
     * Проверить существование валюты с указанным названием, исключая валюту по ID
     * @param title название валюты
     * @param excludeId ID валюты, которую нужно исключить из проверки
     * @return true если валюта существует, false если нет
     */
    public boolean existsByTitleExcludingId(String title, int excludeId) {
        return dao.existsByTitleExcludingId(title, excludeId);
    }

    /**
     * Проверить существование валюты с указанным коротким именем, исключая валюту по ID
     * @param shortName короткое имя валюты
     * @param excludeId ID валюты, которую нужно исключить из проверки
     * @return true если валюта существует, false если нет
     */
    public boolean existsByShortNameExcludingId(String shortName, int excludeId) {
        return dao.existsByShortNameExcludingId(shortName, excludeId);
    }

    /**
     * Получить обменный курс валюты по ID
     * @param id ID валюты
     * @return обменный курс валюты
     */
    public double getExchangeRateById(int id) {
        return dao.getExchangeRateById(id);
    }

    /**
     * Получить список доступных ID валют по фильтру
     * @param filter фильтр (ACTIVE, DELETED, ALL)
     * @return список доступных ID валют
     */
    public LiveData<List<Integer>> getAvailableIds(EntityFilter filter) {
        return dao.getAvailableIds(filter);
    }

    /**
     * Получить список доступных ID валют по фильтру (синхронно)
     * @param filter фильтр (ACTIVE, DELETED, ALL)
     * @return список доступных ID валют
     */
    public List<Integer> getAvailableIdsSync(EntityFilter filter) {
        return dao.getAvailableIdsSync(filter);
    }
} 