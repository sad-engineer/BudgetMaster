package com.sadengineer.budgetmaster.backend.service;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sadengineer.budgetmaster.backend.entity.Currency;
import com.sadengineer.budgetmaster.backend.repository.CurrencyRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service класс для бизнес-логики работы с Currency
 */
public class CurrencyService {
    
    private final CurrencyRepository currencyRepository;
    private final ExecutorService executorService;
    private final String user;
    
    public CurrencyService(Context context, String user) {
        this.currencyRepository = new CurrencyRepository(context);
        this.executorService = Executors.newFixedThreadPool(4);
        this.user = user;
    }
    
    // Получить все активные валюты
    public LiveData<List<Currency>> getAllActiveCurrencies() {
        return currencyRepository.getAllActiveCurrencies();
    }
    
    // Получить валюту по ID
    public LiveData<Currency> getCurrencyById(int id) {
        return currencyRepository.getCurrencyById(id);
    }
    
    // Получить валюту по названию
    public LiveData<Currency> getCurrencyByTitle(String title) {
        return currencyRepository.getCurrencyByTitle(title);
    }
    
    // Получить валюту по коду
    public LiveData<Currency> getCurrencyByCode(String code) {
        return currencyRepository.getCurrencyByCode(code);
    }
    
    // Получить валюту по умолчанию
    public LiveData<Currency> getDefaultCurrency() {
        return currencyRepository.getDefaultCurrency();
    }
    
    // Создать новую валюту
    public void createCurrency(String title, String code, String symbol) {
        Currency currency = new Currency();
        currency.setTitle(title);
        currency.setCode(code);
        currency.setSymbol(symbol);
        currency.setDefault(false);
        currency.setPosition(1); // TODO: Получить следующую позицию
        
        currencyRepository.insertCurrency(currency, user);
    }
    
    // Обновить валюту
    public void updateCurrency(Currency currency) {
        currencyRepository.updateCurrency(currency, user);
    }
    
    // Удалить валюту (soft delete)
    public void deleteCurrency(int currencyId) {
        currencyRepository.deleteCurrency(currencyId, user);
    }
    
    // Удалить валюту по названию
    public void deleteCurrency(String title) {
        currencyRepository.deleteCurrencyByTitle(title, user);
    }
    
    // Восстановить удаленную валюту
    public void restoreCurrency(int currencyId) {
        executorService.execute(() -> {
            // Получаем удаленную валюту
            Currency deletedCurrency = currencyRepository.getCurrencyById(currencyId).getValue();
            if (deletedCurrency == null || !deletedCurrency.isDeleted()) {
                return; // Валюта не найдена или уже активна
            }
            
            // Очищаем поля удаления
            deletedCurrency.setDeleteTime(null);
            deletedCurrency.setDeletedBy(null);
            deletedCurrency.setUpdateTime(LocalDateTime.now());
            deletedCurrency.setUpdatedBy(user);
            
            // Обновляем валюту в базе
            currencyRepository.updateCurrency(deletedCurrency, user);
        });
    }
    
    // Изменить позицию валюты (сложная логика)
    public void changePosition(Currency currency, int newPosition) {
        executorService.execute(() -> {
            int oldPosition = currency.getPosition();
            
            // Если позиция не изменилась, ничего не делаем
            if (oldPosition == newPosition) {
                return;
            }
            
            // Получаем все активные валюты для переупорядочивания
            List<Currency> allCurrencies = currencyRepository.getAllActiveCurrencies().getValue();
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
                        currencyRepository.updateCurrency(c, user);
                    }
                }
            } else {
                // Двигаем валюту вверх: сдвигаем валюты между новой и старой позицией вниз
                for (Currency c : allCurrencies) {
                    if (c.getId() != currency.getId() && 
                        c.getPosition() >= newPosition && 
                        c.getPosition() < oldPosition) {
                        c.setPosition(c.getPosition() + 1);
                        currencyRepository.updateCurrency(c, user);
                    }
                }
            }
            
            // Устанавливаем новую позицию для текущей валюты
            currency.setPosition(newPosition);
            currencyRepository.updateCurrency(currency, user);
        });
    }
    
    // Изменить позицию валюты по старой позиции
    public void changePosition(int oldPosition, int newPosition) {
        executorService.execute(() -> {
            Currency currency = currencyRepository.getCurrencyByPosition(oldPosition).getValue();
            if (currency != null) {
                changePosition(currency, newPosition);
            }
        });
    }
    
    // Изменить позицию валюты по названию
    public void changePosition(String title, int newPosition) {
        executorService.execute(() -> {
            Currency currency = currencyRepository.getCurrencyByTitle(title).getValue();
            if (currency != null) {
                changePosition(currency, newPosition);
            }
        });
    }
    
    // Получить или создать валюту
    public LiveData<Currency> getOrCreateCurrency(String title) {
        MutableLiveData<Currency> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            // Поиск по названию
            Currency existingCurrency = currencyRepository.getCurrencyByTitle(title).getValue();
            if (existingCurrency != null) {
                liveData.postValue(existingCurrency);
                return;
            }
            
            // Если не найден - создаем новый
            Currency newCurrency = new Currency();
            newCurrency.setTitle(title);
            newCurrency.setCode(title.toUpperCase()); // TODO: Получить код валюты
            newCurrency.setSymbol(""); // TODO: Получить символ валюты
            newCurrency.setDefault(false);
            newCurrency.setPosition(1); // TODO: Получить следующую позицию
            
            currencyRepository.insertCurrency(newCurrency, user);
            liveData.postValue(newCurrency);
        });
        return liveData;
    }
    
    // Получить или создать валюту с полными параметрами
    public LiveData<Currency> getOrCreateCurrency(String title, String code, String symbol) {
        MutableLiveData<Currency> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            // Поиск по названию
            Currency existingCurrency = currencyRepository.getCurrencyByTitle(title).getValue();
            if (existingCurrency != null) {
                // Проверяем, совпадают ли параметры
                if (existingCurrency.getCode().equals(code) && 
                    existingCurrency.getSymbol().equals(symbol)) {
                    liveData.postValue(existingCurrency);
                    return;
                }
                
                // Если параметры не совпадают, обновляем валюту
                existingCurrency.setCode(code);
                existingCurrency.setSymbol(symbol);
                currencyRepository.updateCurrency(existingCurrency, user);
                liveData.postValue(existingCurrency);
                return;
            }
            
            // Если не найден - создаем новый
            Currency newCurrency = new Currency();
            newCurrency.setTitle(title);
            newCurrency.setCode(code);
            newCurrency.setSymbol(symbol);
            newCurrency.setDefault(false);
            newCurrency.setPosition(1); // TODO: Получить следующую позицию
            
            currencyRepository.insertCurrency(newCurrency, user);
            liveData.postValue(newCurrency);
        });
        return liveData;
    }
    
    // Установить валюту по умолчанию
    public void setDefaultCurrency(int currencyId) {
        executorService.execute(() -> {
            // Сначала сбрасываем все валюты как не по умолчанию
            List<Currency> allCurrencies = currencyRepository.getAllActiveCurrencies().getValue();
            if (allCurrencies != null) {
                for (Currency currency : allCurrencies) {
                    if (currency.isDefault()) {
                        currency.setDefault(false);
                        currencyRepository.updateCurrency(currency, user);
                    }
                }
            }
            
            // Устанавливаем выбранную валюту как валюту по умолчанию
            Currency defaultCurrency = currencyRepository.getCurrencyById(currencyId).getValue();
            if (defaultCurrency != null) {
                defaultCurrency.setDefault(true);
                currencyRepository.updateCurrency(defaultCurrency, user);
            }
        });
    }
    
    // Получить количество активных валют
    public LiveData<Integer> getActiveCurrenciesCount() {
        return currencyRepository.getActiveCurrenciesCount();
    }
    
    // Валидация валюты
    public boolean validateCurrency(Currency currency) {
        if (currency.getTitle() == null || currency.getTitle().trim().isEmpty()) {
            return false;
        }
        if (currency.getCode() == null || currency.getCode().trim().isEmpty()) {
            return false;
        }
        return true;
    }
    
    // Проверить, является ли валюта валютой по умолчанию
    public boolean isDefaultCurrency(Currency currency) {
        return currency != null && currency.isDefault();
    }
    
    // Получить валюту по умолчанию или создать рубль
    public LiveData<Currency> getDefaultCurrencyOrCreate() {
        MutableLiveData<Currency> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            Currency defaultCurrency = currencyRepository.getDefaultCurrency().getValue();
            if (defaultCurrency != null) {
                liveData.postValue(defaultCurrency);
                return;
            }
            
            // Если валюты по умолчанию нет, создаем рубль
            Currency ruble = new Currency();
            ruble.setTitle("Рубль");
            ruble.setCode("RUB");
            ruble.setSymbol("₽");
            ruble.setDefault(true);
            ruble.setPosition(1);
            
            currencyRepository.insertCurrency(ruble, user);
            liveData.postValue(ruble);
        });
        return liveData;
    }
} 