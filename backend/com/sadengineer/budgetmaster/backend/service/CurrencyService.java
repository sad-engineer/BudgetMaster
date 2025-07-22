// -*- coding: utf-8 -*-
package com.sadengineer.budgetmaster.backend.service;

import com.sadengineer.budgetmaster.backend.model.Currency;
import com.sadengineer.budgetmaster.backend.repository.CurrencyRepository;
import com.sadengineer.budgetmaster.backend.validator.CurrencyValidator;
import com.sadengineer.budgetmaster.backend.validator.BaseEntityValidator;
import com.sadengineer.budgetmaster.backend.validator.CommonValidator;
import com.sadengineer.budgetmaster.backend.constants.ServiceConstants;
import com.sadengineer.budgetmaster.backend.constants.ModelConstants;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с валютами
 */
public class CurrencyService {
    /**
     * Репозиторий для работы с валютами
     */
    private final CurrencyRepository currencyRepository;

    /**
     * Пользователь, выполняющий операции
     */
    private final String user;

    /**
     * Конструктор для сервиса
     * @param currencyRepository репозиторий для работы с валютами
     * @param user пользователь, выполняющий операции
     */
    public CurrencyService(CurrencyRepository currencyRepository, String user) {
        this.currencyRepository = currencyRepository;
        this.user = user;
    }

    /**
     * Конструктор для сервиса с автоматическим созданием репозитория
     * @param user пользователь, выполняющий операции
     */
    public CurrencyService(String user) {
        this.currencyRepository = new CurrencyRepository(ServiceConstants.DEFAULT_DATABASE_NAME);
        this.user = user;
    }
    
    /**
     * Удаляет валюту по id
     * @param id id валюты
     * @return true, если удаление успешно
     */
    public boolean delete(Integer id) {
        BaseEntityValidator.validatePositiveId(id, "ID валюты");
        return currencyRepository.deleteById(id, user);
    }

    /**
     * Удаляет валюту по title
     * @param title название валюты
     * @return true, если удаление успешно
     */
    public boolean delete(String title) {
        CommonValidator.validateCurrencyTitle(title);
        return currencyRepository.deleteByTitle(title, user);
    }

    /**
     * Изменяет порядок валюты с переупорядочиванием других валют
     * @param currency валюта для изменения позиции
     * @param newPosition новая позиция
     * @return валюта с новой позицией
     */
    public Currency changePosition(Currency currency, int newPosition) {
        BaseEntityValidator.validate(currency); 
        CommonValidator.validatePositivePosition(newPosition);
        int oldPosition = currency.getPosition();
        
        // Если позиция не изменилась, ничего не делаем
        if (oldPosition == newPosition) {
            return currency;
        }
        
        // Получаем все валюты для переупорядочивания
        List<Currency> allCurrencies = getAll();
        
        // Проверяем, что новая позиция валидна
        if (newPosition < 1 || newPosition > allCurrencies.size()) {
            throw new IllegalArgumentException(ServiceConstants.ERROR_POSITION_OUT_OF_RANGE + allCurrencies.size());
        }
        
        // Переупорядочиваем позиции
        if (oldPosition < newPosition) {
            // Двигаем валюту вниз: сдвигаем валюты между старой и новой позицией вверх
            for (Currency c : allCurrencies) {
                if (c.getId() != currency.getId() && 
                    c.getPosition() > oldPosition && 
                    c.getPosition() <= newPosition) {
                    c.setPosition(c.getPosition() - 1);
                    c.setUpdateTime(LocalDateTime.now());
                    c.setUpdatedBy(user);
                    currencyRepository.update(c);
                }
            }
        } else {
            // Двигаем валюту вверх: сдвигаем валюты между новой и старой позицией вниз
            for (Currency c : allCurrencies) {
                if (c.getId() != currency.getId() && 
                    c.getPosition() >= newPosition && 
                    c.getPosition() < oldPosition) {
                    c.setPosition(c.getPosition() + 1);
                    c.setUpdateTime(LocalDateTime.now());
                    c.setUpdatedBy(user);
                    currencyRepository.update(c);
                }
            }
        }
        
        // Устанавливаем новую позицию для целевой валюты
        currency.setPosition(newPosition);
        currency.setUpdateTime(LocalDateTime.now());    
        currency.setUpdatedBy(user);
        return currencyRepository.update(currency);
    }

    /**
     * Изменяет порядок валюты с переупорядочиванием других валют
     * @param oldPosition старая позиция
     * @param newPosition новая позиция
     * @return валюта с новой позицией. Если валюта не найдена, возвращает null
     */
    public Currency changePosition(int oldPosition, int newPosition) {
        CommonValidator.validatePositivePosition(oldPosition);
        Optional<Currency> currency = currencyRepository.findByPosition(oldPosition);
        if (currency.isPresent()) {
            return changePosition(currency.get(), newPosition);
        }
        return null;
    }

    /**
     * Изменяет порядок валюты с переупорядочиванием других валют
     * @param title название валюты
     * @param newPosition новая позиция
     * @return валюта с новой позицией. Если валюта не найдена, возвращает null
     */
    public Currency changePosition(String title, int newPosition) {
        CommonValidator.validateCurrencyTitle(title);
        Optional<Currency> currencyOpt = currencyRepository.findByTitle(title);
        if (currencyOpt.isPresent()) {
            return changePosition(currencyOpt.get(), newPosition);
        }
        return null;
    }


    /**
     * Создает новую валюту без валидации названия валюты (для внутреннего использования)
     * @param title название валюты
     * @return валюта
     */
    private Currency create(String title) {
        Currency newCurrency = new Currency();
        int nextPosition = currencyRepository.getMaxPosition() + 1;
        newCurrency.setTitle(title);
        newCurrency.setPosition(nextPosition);
        newCurrency.setCreateTime(LocalDateTime.now());
        newCurrency.setCreatedBy(user);

        // Валидация валюты
        CurrencyValidator.validateForCreate(newCurrency);

        return currencyRepository.save(newCurrency);
    }

    /**
     * Получает все валюты
     * @return список валют
     */
    public List<Currency> getAll() {
        return currencyRepository.findAll();
    }

    /**
     * Получает валюту по ID. 
     * Если валюта с таким ID существует, возвращает ее.
     * Если валюта с таким ID существует, но удалена, восстанавливает ее (удаляет информацию об удалении валюты).
     * Если валюта с таким ID не существует, вернет null.
     * @param id ID валюты
     * @return валюта
     */
    public Currency get(Integer id) { 
        BaseEntityValidator.validatePositiveId(id, "ID валюты");
        Optional<Currency> currency = currencyRepository.findById(id);
        if (currency.isPresent()) {
            Currency currencyObj = currency.get();
            if (isCurrencyDeleted(currencyObj)) {
                return restore(currencyObj);
            }
            return currencyObj;
        }
        return null;
    }

    /**
     * Получает валюту по названию. 
     * Если валюта с таким названием существует, возвращает ее.
     * Если валюта с таким названием существует, но удалена, восстанавливает ее (удаляет информацию об удалении валюты).
     * Если валюта с таким названием не существует, создает новую.
     * @param title название валюты
     * @return валюта
     */
    public Currency get(String title) {
        CommonValidator.validateCurrencyTitle(title);
        Optional<Currency> currency = currencyRepository.findByTitle(title);
        if (currency.isPresent()) {
            Currency currencyObj = currency.get();
            if (isCurrencyDeleted(currencyObj)) {
                return restore(currencyObj);
            }
            return currencyObj;
        }
        return create(title);
    }

    /**
     * Проверяет, удалена ли валюта
     * @param currency валюта для проверки
     * @return true, если валюта удалена
     */
    public boolean isCurrencyDeleted(Currency currency) {
        return currency.getDeletedBy() != null;
    }

    /**
     * Восстанавливает удаленную валюту
     * @param restoredCurrency валюта для восстановления
     * @return восстановленная валюта
     */
    private Currency restore(Currency restoredCurrency) {
        restoredCurrency.setDeletedBy(null);
        restoredCurrency.setDeleteTime(null);
        restoredCurrency.setUpdateTime(LocalDateTime.now());
        restoredCurrency.setUpdatedBy(user);
        return currencyRepository.update(restoredCurrency);
    }

    /**
     * Создает новую валюту
     * @param title название валюты
     * @return валюта
     */
    public Currency createCurrency(String title) {
        CommonValidator.validateCurrencyTitle(title);
        
        // Проверяем, существует ли валюта с таким названием
        Optional<Currency> existingCurrency = currencyRepository.findByTitle(title);
        if (existingCurrency.isPresent()) {
            Currency currency = existingCurrency.get();
            if (isCurrencyDeleted(currency)) {
                return restore(currency);
            } else {
                throw new IllegalArgumentException(ServiceConstants.ERROR_CURRENCY_ALREADY_EXISTS + title + "' уже существует");
            }
        }
        
        return create(title);
    }

    /**
     * Обновляет валюту
     * @param id ID валюты
     * @param title новое название валюты
     * @return обновленная валюта
     */
    public Currency update(Integer id, String title) {
        BaseEntityValidator.validatePositiveId(id, "ID валюты");
        CommonValidator.validateCurrencyTitle(title);
        
        Optional<Currency> currencyOpt = currencyRepository.findById(id);
        if (currencyOpt.isPresent()) {
            Currency currency = currencyOpt.get();
            
            // Проверяем, не удалена ли валюта
            if (isCurrencyDeleted(currency)) {
                throw new IllegalArgumentException(ServiceConstants.ERROR_CANNOT_UPDATE_DELETED_CURRENCY);
            }
            
            // Проверяем, не существует ли другая валюта с таким названием
            Optional<Currency> existingCurrency = currencyRepository.findByTitle(title);
            if (existingCurrency.isPresent() && existingCurrency.get().getId() != id) {
                throw new IllegalArgumentException(ServiceConstants.ERROR_CURRENCY_ALREADY_EXISTS + title + "' уже существует");
            }
            
            currency.setTitle(title);
            currency.setUpdateTime(LocalDateTime.now());
            currency.setUpdatedBy(user);
            
            CurrencyValidator.validateForUpdate(currency);
            
            return currencyRepository.update(currency);
        }
        
        throw new IllegalArgumentException(ServiceConstants.ERROR_CURRENCY_NOT_FOUND + id + " не найдена");
    }
} 