// -*- coding: utf-8 -*-
package com.sadengineer.budgetmaster.backend.service;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.sadengineer.budgetmaster.backend.entity.Currency;
import com.sadengineer.budgetmaster.backend.repository.CurrencyRepository;
import com.sadengineer.budgetmaster.backend.constants.ModelConstants;


import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CompletableFuture;

/**
 * Service класс для бизнес-логики работы с Currency
 */
public class CurrencyService {
    
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
     * Изменить позицию валюты (сложная логика)
     * @param currency валюта
     * @param newPosition новая позиция
     */
    public void changePosition(Currency currency, int newPosition) {
        executorService.execute(() -> {
            int oldPosition = currency.getPosition();
            
            // Если позиция не изменилась, ничего не делаем
            if (oldPosition == newPosition) {
                return;
            }
            
            // Получаем все валюты для переупорядочивания
            List<Currency> allCurrencies = repo.getAll().getValue();
            if (allCurrencies == null) {
                throw new RuntimeException("Не удалось получить список валют");
            }
            
            // Проверяем, что новая позиция валидна
            int maxPosition = allCurrencies.size();
            if (newPosition < 1 || newPosition > maxPosition) {
                throw new IllegalArgumentException("Позиция вне диапазона: " + maxPosition);
            }
            
            // Переупорядочиваем позиции
            if (oldPosition < newPosition) {
                // Двигаем валюту вниз: сдвигаем валюты между старой и новой позицией вверх
                for (Currency c : allCurrencies) {
                    if (c.getId() != currency.getId() && 
                        c.getPosition() > oldPosition && 
                        c.getPosition() <= newPosition) {
                        c.setPosition(c.getPosition() - 1);
                        repo.update(c);
                    }
                }
            } else {
                // Двигаем валюту вверх: сдвигаем валюты между новой и старой позицией вниз
                for (Currency c : allCurrencies) {
                    if (c.getId() != currency.getId() && 
                        c.getPosition() >= newPosition && 
                        c.getPosition() < oldPosition) {
                        c.setPosition(c.getPosition() + 1);
                        repo.update(c);
                    }
                }
            }
            
            // Устанавливаем новую позицию для текущей валюты
            currency.setPosition(newPosition);
            repo.update(currency);
        });
    }
    
    /**
     * Изменить позицию валюты по старой позиции
     * @param oldPosition старая позиция
     * @param newPosition новая позиция
     */
    public void changePosition(int oldPosition, int newPosition) {
        executorService.execute(() -> {
            Currency currency = repo.getByPosition(oldPosition).getValue();
            if (currency != null) {
                changePosition(currency, newPosition);
            }
        });
    }
    
    /**
     * Изменить позицию валюты по названию
     * @param title название валюты
     * @param newPosition новая позиция
     */
    public void changePosition(String title, int newPosition) {
        executorService.execute(() -> {
            Currency currency = repo.getByTitle(title).getValue();
            if (currency != null) {
                changePosition(currency, newPosition);
            }
        });
    }

    /**
     * Создать новую валюту
     * @param title название валюты
     * @return CompletableFuture с созданной валютой
     */
    public CompletableFuture<Currency> create(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Название валюты не может быть пустым");
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                return createCurrencySync(title);
            } catch (Exception e) {
                throw new RuntimeException("Ошибка создания валюты: " + e.getMessage(), e);
            }
        }, executorService);
    }

    /**
     * Приватный метод для создания валюты
     * @param title название валюты
     * @return валюта
     */
    private Currency createCurrencySync(String title) {
        Currency currency = new Currency();
        currency.setTitle(title);
        currency.setPosition(repo.getMaxPosition() + 1);
        currency.setCreateTime(LocalDateTime.now());
        currency.setCreatedBy(user);
        
        LiveData<Currency> insertedCurrency = repo.insert(currency);
        Currency result = insertedCurrency.getValue();
        if (result == null) {
            throw new RuntimeException("Не удалось создать валюту");
        }
        return result;
    }

    /**
     * Удалить валюту (полное удаление - удаление строки из БД)
     * @param currency валюта
     */
    public void delete(Currency currency) {
        executorService.execute(() -> {
            if (currency != null) {
                repo.delete(currency);
            }
        });
    }     
    
    /**
     * Получить все валюты
     * @return LiveData с списком всех валют
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
     * Получить валюту по названию или создать новую
     * @param title название валюты
     * @return CompletableFuture с валютой
     */
    public CompletableFuture<Currency> getByTitleOrCreate(String title) {
        return CompletableFuture.supplyAsync(() -> {
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("Название валюты не может быть пустым");
            }
            
            // Поиск валют по подстроке в названии (включая удаленные)
            List<Currency> currencies = repo.searchByTitle(title).getValue();
            if (currencies != null) {
                // Поиск по точному совпадению
                for (Currency currency : currencies) {
                    if (currency.getTitle().equals(title)) {
                        return currency;
                    }
                }
                
                // Поиск по подстроке (без учета регистра)
                for (Currency currency : currencies) {
                    if (currency.getTitle().toLowerCase().contains(title.toLowerCase())) {
                        return currency;
                    }
                }
            }
            
            // Создание новой валюты
            return createCurrencySync(title);
        }, executorService);
    }

    /**
     * Восстановить удаленную валюту (soft delete)
     * @param deletedCurrency удаленная валюта
     */
    public void restore(Currency deletedCurrency) {
        executorService.execute(() -> {
            if (deletedCurrency != null) {
                // Очищаем поля удаления
                deletedCurrency.setDeleteTime(null);
                deletedCurrency.setDeletedBy(null);
                deletedCurrency.setUpdateTime(LocalDateTime.now());
                deletedCurrency.setUpdatedBy(user);
                // Обновляем валюту в базе
                repo.update(deletedCurrency);
            }
        });
    }

    /**
     * Удалить валюту (soft delete)
     * @param currency валюта
     */
    public void softDelete(Currency currency) {
        executorService.execute(() -> {
            if (currency != null) {
                currency.setDeleteTime(LocalDateTime.now());
                currency.setDeletedBy(user);
                repo.update(currency);
            }
        });
    }
    
    /**
     * Обновить валюту
     * @param currency валюта
     */
    public void update(Currency currency) {
        executorService.execute(() -> {
            if (currency != null) {
                currency.setUpdateTime(LocalDateTime.now());
                currency.setUpdatedBy(user);
                repo.update(currency);
            }
        });
    }
    
    /**
     * Получить валюту по умолчанию 
     * @return LiveData с валютой
     */
    public LiveData<Currency> getDefaultCurrency() {
        return repo.getById(defaultCurrencyID);
    }
} 