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

    // Изменить позицию валюты (сложная логика)
    public void changePosition(Currency currency, int newPosition) {
        executorService.execute(() -> {
            int oldPosition = currency.getPosition();
            
            // Если позиция не изменилась, ничего не делаем
            if (oldPosition == newPosition) {
                return;
            }
            
            // Получаем все валюты для переупорядочивания
            List<Currency> allCurrencies = repo.getAll().getValue();
            if (allCurrencies == null) return;
            
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
    
    // Изменить позицию валюты по старой позиции
    public void changePosition(int oldPosition, int newPosition) {
        executorService.execute(() -> {
            Currency currency = repo.getByPosition(oldPosition).getValue();
            if (currency != null) {
                changePosition(currency, newPosition);
            }
        });
    }
    
    // Изменить позицию валюты по названию
    public void changePosition(String title, int newPosition) {
        executorService.execute(() -> {
            Currency currency = repo.getByTitle(title).getValue();
            if (currency != null) {
                changePosition(currency, newPosition);
            }
        });
    }

    // Создать новую валюту
    public CompletableFuture<Long> create(String title) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // создаем новую валюту
                Currency currency = new Currency();
                // получаем максимальную позицию и увеличиваем на 1
                int maxPos = repo.getMaxPosition();
                int position = maxPos + 1;
                // устанавливаем значения
                currency.setTitle(title);
                currency.setPosition(position);
                currency.setCreateTime(LocalDateTime.now());
                currency.setCreatedBy(user);

                // сохраняем валюту и возвращаем ID
                return repo.insert(currency);
            } catch (Exception e) {
                throw new RuntimeException("Ошибка создания валюты: " + e.getMessage(), e);
            }
        }, executorService);
    }

    // Удалить валюту (полное удаление - удаление строки из БД)
    public void delete(Currency currency) {
        executorService.execute(() -> {
            if (currency != null) {
                repo.delete(currency);
            }
        });
    }     
    
    // Получить все валюты
    public LiveData<List<Currency>> getAll() {
        return repo.getAll();
    }

    // Получить валюту по названию
    public LiveData<Currency> getByTitle(String title) {
        return repo.getByTitle(title);
    }

    // Восстановить удаленную валюту (soft delete)
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

    // Удалить валюту (soft delete)
    public void softDelete(Currency currency) {
        executorService.execute(() -> {
            if (currency != null) {
                currency.setDeleteTime(LocalDateTime.now());
                currency.setDeletedBy(user);
                repo.update(currency);
            }
        });
    }
    
    // Обновить валюту
    public void update(Currency currency) {
        executorService.execute(() -> {
            if (currency != null) {
                currency.setUpdateTime(LocalDateTime.now());
                currency.setUpdatedBy(user);
                repo.update(currency);
            }
        });
    }
    
    // Получить валюту по умолчанию 
    public LiveData<Currency> getDefaultCurrency() {
        return repo.getById(defaultCurrencyID);
    }
} 