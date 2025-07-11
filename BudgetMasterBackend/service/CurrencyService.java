package service;

import model.Currency;
import repository.CurrencyRepository;
import validator.CurrencyValidator;
import validator.BaseEntityValidator;

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
     * Удаляет валюту по id
     * @param id id валюты
     * @return true, если удаление успешно
     */
    public boolean delete(Integer id) {
        CurrencyValidator.validateId(id);
        return currencyRepository.deleteById(id, user);
    }

    /**
     * Удаляет валюту по title
     * @param title название валюты
     * @return true, если удаление успешно
     */
    public boolean delete(String title) {
        CurrencyValidator.validateTitle(title);
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
        CurrencyValidator.validatePosition(newPosition);
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
     * @return валюта с новой позицией. Если валюта не найдена, возвращает null
     */
    public Currency changePosition(int oldPosition, int newPosition) {
        CurrencyValidator.validatePosition(oldPosition);
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
        CurrencyValidator.validateTitle(title);
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
        CurrencyValidator.validateId(id);
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
     * Получает валюту по title.
     * Если валюта с таким title существует, возвращает ее.
     * Если валюта с таким title существует, но удалена, восстанавливает ее (удаляет информацию об удалении валюты).
     * Если валюта с таким title не существует, создает новую.
     * @param title название валюты
     * @return валюта
     */
    public Currency get(String title) {
        CurrencyValidator.validateTitle(title);
        Optional<Currency> currency = currencyRepository.findByTitle(title);
        if (currency.isPresent()) {
            Currency currencyObj = currency.get();
            if (isCurrencyDeleted(currencyObj)) {
                return restore(currencyObj);
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
        BaseEntityValidator.validate(currency);
        return currency.getDeleteTime() != null || currency.getDeletedBy() != null;
    }

    /**
     * Восстанавливает валюту (удаляет информацию об удалении валюты) (для внутреннего использования)
     * @param restoredCurrency валюта
     * @return валюта
     */
    private Currency restore(Currency restoredCurrency) {
        // Устанавливаем время восстановления и пользователя
        restoredCurrency.setUpdateTime(LocalDateTime.now());
        restoredCurrency.setUpdatedBy(user);
        // Удаляем информацию об удалении если она есть
        restoredCurrency.setDeleteTime(null);
        restoredCurrency.setDeletedBy(null);
        // Валидация валюты
        CurrencyValidator.validateForUpdate(restoredCurrency);
        
        return currencyRepository.update(restoredCurrency);
    }
} 