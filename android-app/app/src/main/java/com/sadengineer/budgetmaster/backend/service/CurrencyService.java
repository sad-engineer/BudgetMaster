package com.sadengineer.budgetmaster.backend.service;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.room.Transaction;

import com.sadengineer.budgetmaster.backend.entity.Currency;
import com.sadengineer.budgetmaster.backend.entity.EntityFilter;
import com.sadengineer.budgetmaster.backend.repository.CurrencyRepository;
import com.sadengineer.budgetmaster.backend.constants.ModelConstants;
import com.sadengineer.budgetmaster.backend.validator.CurrencyValidator;

import java.time.LocalDateTime;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service класс для бизнес-логики работы с Currency
 */
public class CurrencyService {
    private static final String TAG = "CurrencyService";

    private final CurrencyRepository repo;
    private final ExecutorService executorService;
    private final String user;
    private final int defaultCurrencyID;
    
    public CurrencyService(Context context, String user) {
        this.repo = new CurrencyRepository(context);
        this.executorService = Executors.newFixedThreadPool(4);
        this.user = user;
        this.defaultCurrencyID = ModelConstants.DEFAULT_CURRENCY_ID;
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
        CurrencyValidator.validateTitle(trimmedTitle);
        executorService.execute(() -> {
            try {
                createCurrencyInTransaction(trimmedTitle, null);                
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
        CurrencyValidator.validateTitle(trimmedTitle);
        CurrencyValidator.validateShortName(trimmedShortName);
        executorService.execute(() -> {
            try {
                // Проверяем уникальность в фоновом потоке
                if (repo.existsByTitle(trimmedTitle)) {
                    Log.e(TAG, "Валюта с названием '" + trimmedTitle + "' уже существует");
                    return;
                }
                
                if (trimmedShortName != null && repo.existsByShortName(trimmedShortName)) {
                    Log.e(TAG, "Валюта с коротким именем '" + trimmedShortName + "' уже существует");
                    return;
                }
                
                createCurrencyInTransaction(trimmedTitle, trimmedShortName);                
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при создании валюты '" + title + "': " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Транзакция для создания новой валюты
     * @param title название валюты
     * @param shortName короткое имя валюты
     */
    @Transaction
    private void createCurrencyInTransaction(String title, String shortName) {
        Log.d(TAG, "🔄 Запрос на создание валюты: " + title + (shortName != null ? " (" + shortName + ")" : ""));
        Currency currency = new Currency();
        currency.setTitle(title);
        currency.setShortName(shortName);
        currency.setPosition(repo.getMaxPosition() + 1);
        currency.setCreateTime(LocalDateTime.now());
        currency.setCreatedBy(user);
        repo.insert(currency);
        Log.d(TAG, "✅ Валюта " + currency.getTitle() + " успешно создана");
    }

    /**
     * Удалить валюту (полное удаление - удаление строки из БД)
     * @param softDelete true - soft delete, false - полное удаление
     * @param currency валюта
     */
    public void delete(boolean softDelete, Currency currency) {
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
        if (currency == null) {
            Log.e(TAG, "Валюта не передана для удаления. Удаление было отменено");
            return;
        }
        executorService.execute(() -> {
            try {
                deleteCurrencyInTransaction(currency);
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при удалении валюты '" + currency.getTitle() + "': " + e.getMessage(), e);
            }
        });
    }     
    
    /**
     * Транзакция для удаления валюты
     * @param currency валюта
     */
    @Transaction
    private void deleteCurrencyInTransaction(Currency currency) {
        Log.d(TAG, "🔄 Запрос на удаление валюты: " + currency.getTitle());
        int deletedPosition = currency.getPosition();
        repo.delete(currency);
        Log.d(TAG, "✅ Валюта " + currency.getTitle() + " успешно удалена");
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
        return repo.getAll();
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
        executorService.execute(() -> {
            try {
                restoreCurrencyInTransaction(deletedCurrency);
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при восстановлении валюты: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Транзакция для восстановления валюты
     * @param deletedCurrency удаленная валюта
     */
    @Transaction
    private void restoreCurrencyInTransaction(Currency deletedCurrency) {
        if (deletedCurrency != null) {
            Log.d(TAG, "Запрос на восстановление валюты " + deletedCurrency.getTitle());
            deletedCurrency.setPosition(repo.getMaxPosition() + 1);
            deletedCurrency.setDeleteTime(null);
            deletedCurrency.setDeletedBy(null);
            deletedCurrency.setUpdateTime(LocalDateTime.now());
            deletedCurrency.setUpdatedBy(user);
            repo.update(deletedCurrency);
            Log.d(TAG, "Валюта " + deletedCurrency.getTitle() + " успешно восстановлена");
        } else {
            Log.e(TAG, "Валюта не найдена для восстановления");
        }
    }

    /**
     * Удалить валюту (soft delete)
     * @param currency валюта
     */
    private void softDelete(Currency currency) {
        if (currency == null) {
            Log.e(TAG, "Валюта не найдена для soft delete. Удаление было отменено");
            return;
        }   

        executorService.execute(() -> {
            try {
                softDeleteCurrencyInTransaction(currency);
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при soft delete валюты: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Транзакция для удаления валюты (soft delete)
     * @param currency валюта
     */
    @Transaction
    private void softDeleteCurrencyInTransaction(Currency currency) {
        Log.d(TAG, "Запрос на softDelete валюты " + currency.getTitle());
        int deletedPosition = currency.getPosition();
        currency.setPosition(0);
        currency.setDeleteTime(LocalDateTime.now());
        currency.setDeletedBy(user);
        repo.update(currency);
        // Пересчитываем позиции после soft delete
        repo.shiftPositionsDown(deletedPosition);
        Log.d(TAG, "✅ Валюта " + currency.getTitle() + " успешно soft deleted");
    }
    
    
    /**
     * Обновить валюту
     * @param currency валюта
     */
    public void update(Currency currency) {
        if (currency == null) {
            Log.e(TAG, "Валюта не найдена для обновления. Обновление было отменено");
            return;
        }

        executorService.execute(() -> {
            try {
                // Проверяем уникальность (исключая текущую валюту)
                if (repo.existsByTitleExcludingId(currency.getTitle(), currency.getId())) {
                    Log.e(TAG, "Валюта с названием '" + currency.getTitle() + "' уже существует");
                    return;
                }
                
                if (currency.getShortName() != null && repo.existsByShortNameExcludingId(currency.getShortName(), currency.getId())) {
                    Log.e(TAG, "Валюта с коротким именем '" + currency.getShortName() + "' уже существует");
                    return;
                }
                
                Log.d(TAG, "Запрос на обновление валюты " + currency.getTitle());
                currency.setUpdateTime(LocalDateTime.now());
                currency.setUpdatedBy(user);
                repo.update(currency);
                Log.d(TAG, "Запрос на обновление валюты для категории " + currency.getTitle() + " успешно отправлен");
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при обновлении валюты для категории " + currency.getTitle() + ": " + e.getMessage(), e);
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
        return repo.getCount();
    }
    
    /**
     * Закрыть ExecutorService
     */
    public void shutdown() {
        executorService.shutdown();
    }
} 