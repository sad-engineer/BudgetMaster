
package com.sadengineer.budgetmaster.backend.service;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.room.Transaction;

import com.sadengineer.budgetmaster.backend.entity.Account;
import com.sadengineer.budgetmaster.backend.entity.EntityFilter;
import com.sadengineer.budgetmaster.backend.repository.AccountRepository;
import com.sadengineer.budgetmaster.backend.constants.ServiceConstants;
import com.sadengineer.budgetmaster.backend.ThreadManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;


/**
 * Service класс для бизнес-логики работы с Account
 */
public class AccountService {
    private static final String TAG = "AccountService";
    
    private final AccountRepository repo;
    private final ExecutorService executorService;
    private final String user;
    private final ServiceConstants constants;
    
    public AccountService(Context context, String user) {
        this.repo = new AccountRepository(context);
        this.executorService = ThreadManager.getExecutor();
        this.user = user;
        this.constants = new ServiceConstants();
    }

    /**
     * Изменить позицию счета
     * @param account счет
     * @param newPosition новая позиция
     */
    public void changePosition(Account account, int newPosition) {
        executorService.execute(() -> {
            changePositionInTransaction(account, newPosition);
        });
    }
    
    /**
     * Транзакция для изменения позиции счета
     * @param account счет
     * @param newPosition новая позиция
     */
    @Transaction
    private void changePositionInTransaction(Account account, int newPosition) {
        int oldPosition = account.getPosition();
        
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
        
        // Устанавливаем новую позицию для текущего счета
        account.setPosition(newPosition);
        repo.update(account);
    }
    
    /**
     * Изменить позицию счета по старой позиции
     * @param oldPosition старая позиция
     * @param newPosition новая позиция
     */
    public void changePosition(int oldPosition, int newPosition) {
        Account account = repo.getByPosition(oldPosition).getValue();
        if (account != null) {
            changePosition(account, newPosition);
        }
    }
    
    /**
     * Изменить позицию счета по названию
     * @param title название счета
     * @param newPosition новая позиция
     */
    public void changePosition(String title, int newPosition) {
        Account account = repo.getByTitle(title).getValue();
        if (account != null) {
            changePosition(account, newPosition);
        }
    }

    /**
     * Создать новый счет
     * @param title название счета
     * @param amount сумма
     * @param type тип счета
     * @param currencyId ID валюты
     * @param closed признак закрытости счета (0 - открыт, 1 - закрыт)
     */
    public void create(String title, Integer currencyId, Integer amount, Integer type, Integer closed) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Название счета не может быть пустым");
        }
        executorService.execute(() -> {
            try {
                // Создаем переменные ВНУТРИ lambda
                int finalCurrencyId = currencyId;
                int finalAmount = amount;
                int finalType = type;
                int finalClosed = closed;

                // Проверяем, если ID валюты не передан, устанавливаем значение по умолчанию
                if (currencyId == null || currencyId <= 0) {
                    Log.d(TAG, "Для счета " + title + " не передано значение ID валюты, устанавливаем значение по умолчанию");
                    finalCurrencyId = constants.DEFAULT_CURRENCY_ID;
                }

                // Проверяем, если сумма не передана, устанавливаем значение по умолчанию
                if (amount == null || amount <= 0) {
                    Log.d(TAG, "Для счета " + title + " не передано значение суммы, устанавливаем значение по умолчанию");
                    finalAmount = constants.DEFAULT_ACCOUNT_BALANCE;
                }

                // Проверяем, если тип счета не передан, устанавливаем значение по умолчанию
                if (type == null || type <= 0) {
                    Log.d(TAG, "Для счета " + title + " не передано значение типа счета, устанавливаем значение по умолчанию");
                    finalType = constants.DEFAULT_ACCOUNT_TYPE;
                }

                // Проверяем, если признак закрытости счета не передан, устанавливаем значение по умолчанию
                if (closed == null) {
                    Log.d(TAG, "Для счета " + title + " не передано значение признака закрытости счета, устанавливаем значение по умолчанию");
                    finalClosed = constants.DEFAULT_ACCOUNT_STATUS_OPEN;
                }               
                
                createAccountInTransaction(title, finalAmount, finalCurrencyId, finalType, finalClosed);
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при создании счета: " + e.getMessage(), e);
            }
        });
    }   

    /**
     * Транзакция для создания нового счета
     * @param title название счета
     * @param amount сумма
     * @param currencyId ID валюты
     * @param type тип счета
     * @param closed признак закрытости счета (0 - открыт, 1 - закрыт)
     */
    @Transaction
    private void createAccountInTransaction(String title, int amount, int currencyId, int type, int closed) {
        Log.d(TAG, "Запрос на создание счета: " + title);
        Account account = new Account();
        account.setTitle(title);
        account.setAmount(amount);
        account.setCurrencyId(currencyId);
        account.setType(type);
        account.setClosed(closed);
        account.setPosition(repo.getMaxPosition() + 1);
        account.setCreateTime(LocalDateTime.now());
        account.setCreatedBy(user);
        repo.insert(account);
        Log.d(TAG, "Счет " + title + " успешно создан");
    }

    /**
     * Создать новый счет со значениями по умолчанию
     * @param title название счета
     */
    public void create(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Название счета не может быть пустым");
        }
        executorService.execute(() -> {
            try {
                String trimmedTitle = title.trim();
                create(trimmedTitle, null, null, null, null);                
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при создании счета '" + title + "': " + e.getMessage(), e);
            }
        });
    }

    /**
     * Удалить счет (полное удаление - удаление строки из БД)
     * @param account счет
     */
    private void delete(Account account) {
        if (account == null) {
            Log.e(TAG, "Счет не найден для удаления. Удаление было отменено");
            return;
        }
        executorService.execute(() -> {
            try {
                deleteAccountInTransaction(account);
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при удалении счета '" + account.getTitle() + "': " + e.getMessage(), e);
            }
        });
    }     
    
    /**
     * Транзакция для удаления валюты
     * @param currency валюта
     */
    @Transaction
    private void deleteAccountInTransaction(Account account) {
        Log.d(TAG, "Запрос на удаление счета: " + account.getTitle());
        int deletedPosition = account.getPosition();
        repo.delete(account);
        Log.d(TAG, "Счет " + account.getTitle() + " успешно удален");
    }

    /**
     * Удалить счет (полное удаление - удаление строки из БД)
     * @param softDelete true - soft delete, false - полное удаление
     * @param account счет
     */
    public void delete(boolean softDelete, Account account) {
        if (softDelete) {
            softDelete(account);
        } else {
            delete(account);
        }
    }
    
    /**
     * Получить все счета
     * @param filter фильтр для выборки счетов
     * @return LiveData со списком всех счетов
     */
    public LiveData<List<Account>> getAll(EntityFilter filter) {
        return repo.getAll(filter);
    }

    /**
     * Получить все счета по типу
     * @param type тип счета
     * @param filter фильтр для выборки счетов
     * @return LiveData со списком всех счетов
     */
    public LiveData<List<Account>> getAllByType(EntityFilter filter, int type) {
        return repo.getAllByType(filter, type);
    }
    
    /**
     * Получить все счета (включая удаленные)
     * @return LiveData со списком всех счетов
     */
    public LiveData<List<Account>> getAll() {
        return repo.getAll();
    }

    /**
     * Получить счет по названию
     * @param title название счета
     * @return LiveData с счетом
     */
    public LiveData<Account> getByTitle(String title) {
        return repo.getByTitle(title);
    }

    /**
     * Восстановить удаленный счет (soft delete)
     * @param deletedAccount удаленный счет
     */
    public void restore(Account deletedAccount) {
        executorService.execute(() -> {
            try {
                restoreAccountInTransaction(deletedAccount);
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при восстановлении счета: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Транзакция для восстановления счета
     * @param deletedAccount удаленный счет
     */
    @Transaction
    private void restoreAccountInTransaction(Account deletedAccount) {
        if (deletedAccount != null) {
            Log.d(TAG, "Запрос на восстановление счета для категории " + deletedAccount.getTitle());
            deletedAccount.setPosition(repo.getMaxPosition() + 1);
            deletedAccount.setDeleteTime(null);
            deletedAccount.setDeletedBy(null);
            deletedAccount.setUpdateTime(LocalDateTime.now());
            deletedAccount.setUpdatedBy(user);
            repo.update(deletedAccount);
            Log.d(TAG, "Счет " + deletedAccount.getTitle() + " успешно восстановлен");
        } else {
            Log.e(TAG, "Счет не найден для восстановления");
        }
    }

    /**
     * Удалить счет (soft delete)
     * @param account счет
     */
    private void softDelete(Account account) {
        if (account == null) {
            Log.e(TAG, "Счет не найден для soft delete. Удаление было отменено");
            return;
        }   
        executorService.execute(() -> {
            try {
                softDeleteAccountInTransaction(account);
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при soft delete счета: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Транзакция для удаления счета (soft delete)
     * @param account счет
     */
    @Transaction
    private void softDeleteAccountInTransaction(Account account) {
        Log.d(TAG, "Запрос на softDelete счета для категории " + account.getTitle());
        Log.d(TAG, "   - До удаления: ID=" + account.getId() + ", позиция=" + account.getPosition() + 
              ", deleteTime=" + account.getDeleteTime() + ", deletedBy=" + account.getDeletedBy());
        
        int deletedPosition = account.getPosition();
        account.setPosition(0);
        account.setDeleteTime(LocalDateTime.now());
        account.setDeletedBy(user);
        
        Log.d(TAG, "   - После установки: позиция=" + account.getPosition() + 
              ", deleteTime=" + account.getDeleteTime() + ", deletedBy=" + account.getDeletedBy());
        
        repo.update(account);
        
        // Пересчитываем позиции после soft delete
        repo.shiftPositionsDown(deletedPosition);
        Log.d(TAG, "Счет " + account.getTitle() + " успешно soft deleted");
    }

    /**
     * Обновить счет
     * @param account счет
     */
    public void update(Account account) {
        if (account == null) {
            Log.e(TAG, "Счет не найден для обновления. Обновление было отменено");
            return;
        }

        executorService.execute(() -> {
            try {
                Log.d(TAG, "Запрос на обновление счета для категории " + account.getTitle());
                account.setUpdateTime(LocalDateTime.now());
                account.setUpdatedBy(user);
                repo.update(account);
                Log.d(TAG, "Запрос на обновление счета для категории " + account.getTitle() + " успешно отправлен");
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при обновлении счета для категории " + account.getTitle() + ": " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Получить количество счетов
     * @param filter фильтр для выборки счетов
     * @return количество счетов
     */
    public int getCount(EntityFilter filter) {
        return repo.getCount(filter);
    }
    
    /**
     * Получить общее количество счетов (включая удаленные)
     * @return общее количество счетов
     */
    public int getCount() {
        return repo.getCount();
    }
    
    /**
     * Закрыть ExecutorService
     * @deprecated Используйте ThreadManager.shutdown() для централизованного управления
     */
    @Deprecated
    public void shutdown() {
        // Не закрываем ExecutorService здесь, так как он общий
        // Используйте ThreadManager.shutdown() при завершении приложения
    }
} 