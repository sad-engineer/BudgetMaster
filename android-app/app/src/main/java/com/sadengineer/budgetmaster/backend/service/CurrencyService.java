package com.sadengineer.budgetmaster.backend.service;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.room.Transaction;

import com.sadengineer.budgetmaster.backend.entity.Currency;
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;
import com.sadengineer.budgetmaster.backend.repository.CurrencyRepository;
import com.sadengineer.budgetmaster.backend.constants.ModelConstants;
import com.sadengineer.budgetmaster.backend.validator.CurrencyValidator;
import com.sadengineer.budgetmaster.backend.constants.ServiceConstants;
import com.sadengineer.budgetmaster.backend.ThreadManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;


/**
 * Service класс для бизнес-логики работы с Currency
 */
public class CurrencyService {
    private static final String TAG = "CurrencyService";

    private final CurrencyRepository repo;
    private final ExecutorService executorService;
    private final String user;
    private final int defaultCurrencyID;
    private final CurrencyValidator validator;
    private final ServiceConstants constants;
    
    public CurrencyService(Context context, String user) {
        this.repo = new CurrencyRepository(context);
        this.executorService = ThreadManager.getExecutor();
        this.user = user;
        this.defaultCurrencyID = ModelConstants.DEFAULT_CURRENCY_ID;
        this.validator = new CurrencyValidator();
        this.constants = new ServiceConstants();
    }

    /**
     * Изменить позицию валюты
     * @param currency валюта
     * @param newPosition новая позиция
     */
    public void changePosition(Currency currency, int newPosition) {
        executorService.execute(() -> {
            changePositionInTransaction(currency, newPosition);
        });
    }
    
    /**
     * Транзакция для изменения позиции валюты
     * @param currency валюта
     * @param newPosition новая позиция
     */
    @Transaction
    private void changePositionInTransaction(Currency currency, int newPosition) {
        int oldPosition = currency.getPosition();
        
        // Если позиция не изменилась, ничего не делаем
        if (oldPosition == newPosition) {
            return;
        }
        
        // Используем методы сдвига позиций из Repository
        if (oldPosition < newPosition) {
            repo.shiftPositionsDown(oldPosition);
            repo.shiftPositionsUp(newPosition + 1);
        } else {
            repo.shiftPositionsUp(newPosition);
            repo.shiftPositionsDown(oldPosition);
        }
        
        // Устанавливаем новую позицию для текущей валюты
        currency.setPosition(newPosition);
        repo.update(currency);
    }
    
    /**
     * Изменить позицию валюты по старой позиции
     * @param oldPosition старая позиция
     * @param newPosition новая позиция
     */
    public void changePosition(int oldPosition, int newPosition) {
        Currency currency = repo.getByPosition(oldPosition).getValue();
        if (currency != null) {
            changePosition(currency, newPosition);
        }
    }  

    /**
     * Изменить позицию валюты по названию
     * @param title название валюты
     * @param newPosition новая позиция
     */
    public void changePosition(String title, int newPosition) {
        Currency currency = repo.getByTitle(title).getValue();
        if (currency != null) {
            changePosition(currency, newPosition);
        }
    }
    
    /**
     * Изменить позицию валюты по короткому имени
     * @param shortName короткое имя валюты
     * @param newPosition новая позиция
     */
    public void changePositionByShortName(String shortName, int newPosition) {
        Currency currency = repo.getByShortName(shortName).getValue();
        if (currency != null) {
            changePosition(currency, newPosition);
        }
    }

    /**
     * Создать новую валюту без короткого имени
     * Не проверяет уникальность 
     * @param title название валюты
     */
    public void create(String title) {
        String trimmedTitle = title.trim();
        validator.validateTitle(trimmedTitle);
        executorService.execute(() -> {
            try {
                createCurrencyInTransaction(trimmedTitle, null, 1.0);                
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при создании валюты '" + title + "': " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Создать новую валюту с коротким именем
     * Проверяет уникальность названия и короткого имени
     * @param title название валюты
     * @param shortName короткое имя валюты
     */
    public void create(String title, String shortName) {
        String trimmedTitle = title.trim();
        String trimmedShortName = shortName != null ? shortName.trim() : null;
        validator.validateTitle(trimmedTitle);
        validator.validateShortName(trimmedShortName);
        executorService.execute(() -> {
            // Проверяем уникальность в фоновом потоке
            validator.validateTitleUnique(trimmedTitle, repo::existsByTitle);
            validator.validateShortNameUnique(trimmedShortName, repo::existsByShortName);
            createCurrencyInTransaction(trimmedTitle, trimmedShortName, 1.0);                
        });
    }
    
    /**
     * Создать новую валюту с курсом
     * Проверяет уникальность названия и короткого имени
     * @param title название валюты
     * @param shortName короткое имя валюты
     * @param exchangeRate обменный курс валюты
     */
    public void create(String title, String shortName, double exchangeRate) {
        String trimmedTitle = title.trim();
        String trimmedShortName = shortName != null ? shortName.trim() : null;
        validator.validateTitle(trimmedTitle);
        validator.validateShortName(trimmedShortName);
        executorService.execute(() -> {
            // Проверяем уникальность в фоновом потоке
            validator.validateTitleUnique(trimmedTitle, repo::existsByTitle);
            validator.validateShortNameUnique(trimmedShortName, repo::existsByShortName);
            createCurrencyInTransaction(trimmedTitle, trimmedShortName, exchangeRate);                
        });
    }
    
    /**
     * Транзакция для создания новой валюты
     * @param title название валюты
     * @param shortName короткое имя валюты
     */
    @Transaction
    private void createCurrencyInTransaction(String title, String shortName, double exchangeRate) {
        Log.d(TAG, String.format(constants.MSG_CREATE_CURRENCY_REQUEST, title + (shortName != null ? " (" + shortName + ")" : "")));
        Currency currency = new Currency();
        currency.setTitle(title);
        currency.setShortName(shortName);
        currency.setPosition(repo.getMaxPosition() + 1);
        currency.setExchangeRate(exchangeRate);
        currency.setCreateTime(LocalDateTime.now());
        currency.setCreatedBy(user);
        try {
            repo.insert(currency);
            Log.d(TAG, String.format(constants.MSG_CURRENCY_CREATED, currency.getTitle()));
        } catch (Exception e) {
            Log.e(TAG, String.format(constants.MSG_CREATE_CURRENCY_ERROR, title) + e.getMessage(), e);
        }
    }

    /**
     * Создать новую валюту без проверок значений
     * @param title название валюты
     * @param shortName короткое имя валюты
     * @param exchangeRate обменный курс валюты
     */
    public void createWithoutValidation(String title, String shortName, double exchangeRate) {
        executorService.execute(() -> {
            createCurrencyInTransaction(title, shortName, exchangeRate);
        });
    }

    /**
     * Удалить валюту (полное удаление - удаление строки из БД)
     * @param softDelete true - soft delete, false - полное удаление
     * @param currency валюта
     */
    public void delete(Currency currency, boolean softDelete) {
        if (currency == null) {
            Log.e(TAG, constants.MSG_DELETE_CURRENCY_NOT_FOUND);
            return;
        }
        if (softDelete) {
            softDelete(currency);
        } else {
            delete(currency);
        }
    }

    /**
     * Удалить валюту (полное удаление - удаление строки из БД)
     * @param currency валюта
     */
    private void delete(Currency currency) {
        executorService.execute(() -> {
            deleteCurrencyInTransaction(currency);
        });
    }     
    
    /**
     * Транзакция для удаления валюты
     * @param currency валюта
     */
    @Transaction
    private void deleteCurrencyInTransaction(Currency currency) {
        Log.d(TAG, constants.MSG_DELETE_CURRENCY_REQUEST + currency.getTitle());
        try {
            repo.delete(currency);
            Log.d(TAG, String.format(constants.MSG_CURRENCY_DELETED, currency.getTitle()));
        } catch (Exception e) {
            Log.e(TAG, String.format(constants.MSG_DELETE_CURRENCY_ERROR, currency.getTitle()) + e.getMessage(), e);
        }
    }
   
    /**
     * Получить все валюты
     * @param filter фильтр для выборки валют
     * @return LiveData со списком всех валют
     */
    public LiveData<List<Currency>> getAll(EntityFilter filter) {
        return repo.getAll(filter);
    }
    
    /**
     * Получить все валюты (включая удаленные)
     * @return LiveData со списком всех валют
     */
    public LiveData<List<Currency>> getAll() {
        return repo.getAll(EntityFilter.ALL);
    }
    
    /**
     * Получить валюту по ID
     * @param id ID валюты
     * @return LiveData с валютой
     */
    public LiveData<Currency> getById(int id) {
        return repo.getById(id);
    }
    
    /**
     * Получить валюту по ID синхронно
     * @param id ID валюты
     * @return валюта
     */
    public Currency getByIdSync(int id) {
        return repo.getByIdSync(id);
    }

    /**
     * Получить валюту по названию
     * @param title название валюты
     * @return LiveData с валютой
     */
    public LiveData<Currency> getByTitle(String title) {
        return repo.getByTitle(title);
    }
    
    /**
     * Получить валюту по короткому имени
     * @param shortName короткое имя валюты
     * @return LiveData с валютой
     */
    public LiveData<Currency> getByShortName(String shortName) {
        return repo.getByShortName(shortName);
    }
    
    /**
     * Проверить существование валюты с указанным названием
     * @param title название валюты
     * @return true если валюта существует, false если нет
     */
    public boolean existsByTitle(String title) {
        return repo.existsByTitle(title);
    }
    
    /**
     * Проверить существование валюты с указанным коротким именем
     * @param shortName короткое имя валюты
     * @return true если валюта существует, false если нет
     */
    public boolean existsByShortName(String shortName) {
        return repo.existsByShortName(shortName);
    }    

    /**
     * Восстановить удаленную валюту (soft delete)
     * @param deletedCurrency удаленная валюта
     */
    public void restore(Currency deletedCurrency) {
        if (deletedCurrency == null) {
            Log.e(TAG, constants.MSG_RESTORE_CURRENCY_NOT_FOUND);
            return;
        }
        executorService.execute(() -> {
            restoreCurrencyInTransaction(deletedCurrency);
        });
    }
    
    /**
     * Транзакция для восстановления валюты
     * @param deletedCurrency удаленная валюта
     */
    @Transaction
    private void restoreCurrencyInTransaction(Currency deletedCurrency) {
        Log.d(TAG, constants.MSG_RESTORE_CURRENCY_REQUEST + deletedCurrency.getTitle());
        deletedCurrency.setPosition(repo.getMaxPosition() + 1);
        deletedCurrency.setDeleteTime(null);
        deletedCurrency.setDeletedBy(null);
        deletedCurrency.setUpdateTime(LocalDateTime.now());
        deletedCurrency.setUpdatedBy(user);
        try {
            repo.update(deletedCurrency);
            Log.d(TAG, String.format(constants.MSG_CURRENCY_RESTORED, deletedCurrency.getTitle()));
        } catch (Exception e) {
            Log.e(TAG, String.format(constants.MSG_RESTORE_CURRENCY_ERROR, deletedCurrency.getTitle()) + e.getMessage(), e);
        }
    }

    /**
     * Удалить валюту (soft delete)
     * @param currency валюта
     */
    private void softDelete(Currency currency) {
        if (currency == null) {
            Log.e(TAG, constants.MSG_SOFT_DELETE_CURRENCY_NOT_FOUND);
            return;
        }   

        executorService.execute(() -> {
            softDeleteCurrencyInTransaction(currency);
        });
    }
    
    /**
     * Транзакция для удаления валюты (soft delete)
     * @param currency валюта
     */
    @Transaction
    private void softDeleteCurrencyInTransaction(Currency currency) {
        Log.d(TAG, constants.MSG_SOFT_DELETE_CURRENCY_REQUEST + currency.getTitle());
        int deletedPosition = currency.getPosition();
        currency.setPosition(0);
        currency.setDeleteTime(LocalDateTime.now());
        currency.setDeletedBy(user);
        try {
            repo.update(currency);
            // Пересчитываем позиции после soft delete
            repo.shiftPositionsDown(deletedPosition);
            Log.d(TAG, String.format(constants.MSG_CURRENCY_SOFT_DELETED, currency.getTitle()));
        } catch (Exception e) {
            Log.e(TAG, String.format(constants.MSG_SOFT_DELETE_CURRENCY_ERROR, currency.getTitle()) + e.getMessage(), e);
        }
    }
    
    
    /**
     * Обновить валюту
     * @param currency валюта
     */
    public void update(Currency currency) {
        if (currency == null) {
            Log.e(TAG, constants.MSG_UPDATE_CURRENCY_NOT_FOUND);
            return;
        }

        executorService.execute(() -> {
            try {
                validator.validateTitleUniqueExcludingId(currency.getTitle(), currency.getId(), repo::existsByTitleExcludingId);
                validator.validateShortNameUniqueExcludingId(currency.getShortName(), currency.getId(), repo::existsByShortNameExcludingId);
                Log.d(TAG, String.format(constants.MSG_UPDATE_CURRENCY_REQUEST, currency.getTitle()));
                currency.setUpdateTime(LocalDateTime.now());
                currency.setUpdatedBy(user);
                repo.update(currency);
                Log.d(TAG, String.format(constants.MSG_CURRENCY_UPDATED, currency.getTitle()));
            } catch (Exception e) {
                Log.e(TAG, String.format(constants.MSG_UPDATE_CURRENCY_ERROR, currency.getTitle()) + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Получить количество валют
     * @param filter фильтр для выборки валют
     * @return количество валют
     */
    public int getCount(EntityFilter filter) {
        return repo.getCount(filter);
    }
    
    /**
     * Получить общее количество валют (включая удаленные)
     * @return общее количество валют
     */
    public int getCount() {
        return repo.getCount(EntityFilter.ALL);
    }  
    
    /**
     * Получить обменный курс валюты по ID
     * @param id ID валюты
     * @return обменный курс валюты
     */
    public double getExchangeRateById(int id) {
        return repo.getExchangeRateById(id);
    }

    /**
     * Получить список доступных ID валют по фильтру
     * @param filter фильтр (ACTIVE, DELETED, ALL)
     * @return список доступных ID валют
     */
    public LiveData<List<Integer>> getAvalibleIds(EntityFilter filter) {
        return repo.getAvalibleIds(filter);
    }
} 