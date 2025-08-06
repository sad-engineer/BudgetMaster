
package com.sadengineer.budgetmaster.backend.service;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sadengineer.budgetmaster.backend.entity.Account;
import com.sadengineer.budgetmaster.backend.repository.AccountRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service класс для бизнес-логики работы с Account
 */
public class AccountService {
    private static final String TAG = "AccountService";
    
    private final AccountRepository repo;
    private final ExecutorService executorService;
    private final String user;
    
    public AccountService(Context context, String user) {
        this.repo = new AccountRepository(context);
        this.executorService = Executors.newFixedThreadPool(4);
        this.user = user;
    }

    /**
     * Изменить позицию счета (сложная логика)
     * @param account счет
     * @param newPosition новая позиция
     */
    public void changePosition(Account account, int newPosition) {
        executorService.execute(() -> {
            int oldPosition = account.getPosition();
            
            // Если позиция не изменилась, ничего не делаем
            if (oldPosition == newPosition) {
                return;
            }
            
            // Получаем все счета для переупорядочивания
            List<Account> allAccounts = repo.getAll().getValue();
            if (allAccounts == null) {
                throw new RuntimeException("Не удалось получить список счетов");
            }
            
            // Проверяем, что новая позиция валидна
            int maxPosition = allAccounts.size();
            if (newPosition < 1 || newPosition > maxPosition) {
                throw new IllegalArgumentException("Позиция вне диапазона: " + maxPosition);
            }
            
            // Переупорядочиваем позиции
            if (oldPosition < newPosition) {
                // Двигаем счет вниз: сдвигаем счета между старой и новой позицией вверх
                for (Account a : allAccounts) {
                    if (a.getId() != account.getId() && 
                        a.getPosition() > oldPosition && 
                        a.getPosition() <= newPosition) {
                        a.setPosition(a.getPosition() - 1);
                        repo.update(a);
                    }
                }
            } else {
                // Двигаем счет вверх: сдвигаем счета между новой и старой позицией вниз
                for (Account a : allAccounts) {
                    if (a.getId() != account.getId() && 
                        a.getPosition() >= newPosition && 
                        a.getPosition() < oldPosition) {
                        a.setPosition(a.getPosition() + 1);
                        repo.update(a);
                    }
                }
            }
            
            // Устанавливаем новую позицию для текущего счета
            account.setPosition(newPosition);
            repo.update(account);
        });
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
     */
    public void create(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Название счета не может быть пустым");
        }
        
        executorService.execute(() -> {
            try {
                Log.d(TAG, "🔄 Запрос на создание счета: " + title);
                
                String trimmedTitle = title.trim();
                Account account = new Account();
                account.setTitle(trimmedTitle);
                account.setPosition(repo.getMaxPosition() + 1);
                account.setCreateTime(LocalDateTime.now());
                account.setCreatedBy(user);
                
                // Вставляем счет в базу данных
                repo.insert(account);
                
                Log.d(TAG, "✅ Запрос на создание счета успешно отправлен: " + account.getTitle());
                
            } catch (Exception e) {
                Log.e(TAG, "❌ Ошибка при создании счета '" + title + "': " + e.getMessage(), e);
            }
        });
    }

    /**
     * Создать новый счет
     * @param title название счета
     * @param currencyId ID валюты
     * @param amount сумма
     * @param type тип счета
     */
    public void create(String title, int currencyId, int amount, int type) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Название счета не может быть пустым");
        }
        
        executorService.execute(() -> {
            try {
                Log.d(TAG, "🔄 Запрос на создание счета: " + title);
                
                String trimmedTitle = title.trim();
                Account account = new Account();
                account.setTitle(trimmedTitle);
                account.setCurrencyId(currencyId);
                account.setAmount(amount);
                account.setType(type);
                account.setPosition(repo.getMaxPosition() + 1);
                account.setCreateTime(LocalDateTime.now());
                account.setCreatedBy(user); 
                
                // Вставляем счет в базу данных
                repo.insert(account);
                
                Log.d(TAG, "✅ Запрос на создание счета успешно отправлен: " + account.getTitle());
                
            } catch (Exception e) {
                Log.e(TAG, "❌ Ошибка при создании счета '" + title + "': " + e.getMessage(), e);
            }
        });
    }

     /**
     * Удалить счет (полное удаление - удаление строки из БД)
     * @param account счет
     */
    public void delete(Account account) {
        executorService.execute(() -> {
            try {
                Log.d(TAG, "🔄 Запрос на удаление счета: " + account.getTitle());

                if (account != null) {
                    repo.delete(account);
                }

                Log.d(TAG, "✅ Запрос на удаление счета успешно отправлен: " + account.getTitle());
            } catch (Exception e) {
                Log.e(TAG, "❌ Ошибка при удалении счета '" + account.getTitle() + "': " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Получить все счета
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
                Log.d(TAG, "🔄 Запрос на восстановление счета: " + deletedAccount.getTitle());

                if (deletedAccount != null) {
                    int position = deletedAccount.getPosition();
                    deletedAccount.setPosition(position);
                    deletedAccount.setDeleteTime(null);
                    deletedAccount.setDeletedBy(null);
                    deletedAccount.setUpdateTime(LocalDateTime.now());
                    deletedAccount.setUpdatedBy(user);
                    repo.update(deletedAccount);
                }

                Log.d(TAG, "✅ Запрос на восстановление счета успешно отправлен: " + deletedAccount.getTitle());
            } catch (Exception e) {
                Log.e(TAG, "❌ Ошибка при восстановлении счета '" + deletedAccount.getTitle() + "': " + e.getMessage(), e);
            }
        });
    }

    /**
     * Удалить счет (soft delete)
     * @param account счет
     */
    public void softDelete(Account account) {
        executorService.execute(() -> {
            try {
                Log.d(TAG, "🔄 Запрос на softDelete счета: " + account.getTitle());
                    
                if (account != null) {
                    account.setPosition(0);
                    account.setDeleteTime(LocalDateTime.now());
                    account.setDeletedBy(user);
                    repo.update(account);
                }

                Log.d(TAG, "✅ Запрос на softDelete счета успешно отправлен: " + account.getTitle());
            } catch (Exception e) {
                Log.e(TAG, "❌ Ошибка при softDelete счета '" + account.getTitle() + "': " + e.getMessage(), e);
            }
        });
    }

    /**
     * Обновить счет
     * @param account счет
     */
    public void update(Account account) {
        executorService.execute(() -> {
            try {
                Log.d(TAG, "🔄 Запрос на обновление счета: " + account.getTitle());

                if (account != null) {
                    account.setUpdateTime(LocalDateTime.now());
                    account.setUpdatedBy(user);
                    repo.update(account);
                }

                Log.d(TAG, "✅ Запрос на обновление счета успешно отправлен: " + account.getTitle());
            } catch (Exception e) {
                Log.e(TAG, "❌ Ошибка при обновлении счета '" + account.getTitle() + "': " + e.getMessage(), e);
            }
        });
    }
} 