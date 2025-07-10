package service;

import model.Currency;
import repository.CurrencyRepository;


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
        this.currencyRepository = new CurrencyRepository("budget_master.db");
        this.user = user;
    }
    
    /**
     * Удаляет валюту по title
     * @param title название валюты
     * @return true, если удаление успешно
     */
    public boolean delete(String title) {
        return currencyRepository.deleteByTitle(title, user);
    }

    /**
     * Удаляет валюту по id
     * @param id id валюты
     * @return true, если удаление успешно
     */
    public boolean delete(int id) {
        return currencyRepository.deleteById(id, user);
    }

    /**
     * Изменяет порядок валюты с переупорядочиванием других валют
     * @param currency валюта для изменения позиции
     * @param newPosition новая позиция
     * @return валюта с новой позицией
     */
    public Currency changePosition(Currency currency, int newPosition) {
        int oldPosition = currency.getPosition();
        
        // Если позиция не изменилась, ничего не делаем
        if (oldPosition == newPosition) {
            return currency;
        }
        
        // Получаем все валюты для переупорядочивания
        List<Currency> allCurrencies = getAll();
        
        // Проверяем, что новая позиция валидна
        if (newPosition < 1 || newPosition > allCurrencies.size()) {
            throw new IllegalArgumentException("Новая позиция должна быть от 1 до " + allCurrencies.size());
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
     * @return валюта с новой позицией
     */
    public Currency changePosition(int oldPosition, int newPosition) {
        Optional<Currency> currencyOpt = getById(oldPosition);
        if (currencyOpt.isPresent()) {
            return changePosition(currencyOpt.get(), newPosition);
        }
        return null;
    }

    /**
     * Создает новую валюту
     * @param title название валюты
     * @return валюта
     */
    public Currency create(String title) {
        Currency newCurrency = new Currency();
        int nextPosition = currencyRepository.getMaxPosition() + 1;
        newCurrency.setTitle(title);
        newCurrency.setPosition(nextPosition);
        newCurrency.setCreateTime(LocalDateTime.now());
        newCurrency.setCreatedBy(user);
        newCurrency.setUpdateTime(LocalDateTime.now());
        newCurrency.setUpdatedBy(user);
        
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
     * Получает валюту по ID
     * @param id ID валюты
     * @return валюта
     */
    public Optional<Currency> getById(int id) { 
        return currencyRepository.findById(id);
    }

    /**
     * Получает валюту по title.
     * Если валюта не существует, создает новую.
     * Если валюта существует и удалена, восстанавливает ее.
     * Если валюта существует и активна, возвращает ее.
     * @param title название валюты
     * @return валюта
     */
    public Currency get(String title) {
        Optional<Currency> currency = currencyRepository.findByTitle(title);
        if (currency.isPresent()) {
            if (isCurrencyDeleted(currency.get())) {
                return restore(currency.get());
            }
            return currency.get();
        }
        return create(title);
    }

    /**
     * Проверка валюты на удаление
     * @param currency класс валюты
     * @return true, если валюта удалена
     */
    public boolean isCurrencyDeleted(Currency currency) {
        return currency.getDeleteTime() != null;
    }

    /**
     * Восстанавливает валюту
     * @param restoredCurrency валюта
     * @return валюта
     */
    public Currency restore(Currency restoredCurrency) {
        restoredCurrency.setDeleteTime(null);
        restoredCurrency.setDeletedBy(null);
        restoredCurrency.setUpdateTime(LocalDateTime.now());
        restoredCurrency.setUpdatedBy(user);
        return currencyRepository.update(restoredCurrency);
    }

    /**
     * Восстанавливает валюту по id
     * @param id id валюты
     * @return валюта или null, если валюта не найдена
     */
    public Currency restore(int id) {
        Optional<Currency> currencyOpt = getById(id);
        if (currencyOpt.isPresent()) {
            return restore(currencyOpt.get());
        }
        return null;
    }

    /**
     * Устанавливает нового пользователя для операций
     * @param newUser новый пользователь
     */
    public void setUser(String newUser) {
        // Обратите внимание: поле user final, поэтому нужно создать новый экземпляр сервиса
        // или использовать другой подход для смены пользователя
        throw new UnsupportedOperationException("Для смены пользователя создайте новый экземпляр CurrencyService");
    }
} 