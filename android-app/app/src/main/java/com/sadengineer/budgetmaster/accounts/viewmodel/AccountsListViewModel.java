package com.sadengineer.budgetmaster.accounts.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.sadengineer.budgetmaster.accounts.base.ListViewModel;
import com.sadengineer.budgetmaster.accounts.usecase.LoadAccountsUseCase;
import com.sadengineer.budgetmaster.backend.entity.Account;
import com.sadengineer.budgetmaster.backend.filters.AccountTypeFilter;
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;
import com.sadengineer.budgetmaster.backend.service.AccountService;
import com.sadengineer.budgetmaster.backend.service.CurrencyCacheService;
import com.sadengineer.budgetmaster.utils.LogManager;

import java.util.List;

/**
 * ViewModel для отображения списка счетов
 * Наследует общую функциональность от ListViewModel
 * Использует специализированный CurrencyCacheService для работы с валютами
 */
public class AccountsListViewModel extends ListViewModel<Account> {
    
    private final AccountService accountService;
    private final LoadAccountsUseCase loadAccountsUseCase;
    private final CurrencyCacheService currencyCacheService;
    
    public AccountsListViewModel(AccountService accountService, CurrencyCacheService currencyCacheService) {
        this.accountService = accountService;
        this.currencyCacheService = currencyCacheService;
        this.loadAccountsUseCase = new LoadAccountsUseCase(accountService);
    }
    
    @Override
    protected void loadItems() {
        LogManager.d(TAG, "Загрузка всех счетов");
        
        // Загружаем все активные счета
        LoadAccountsUseCase.Params params = LoadAccountsUseCase.Params.forActiveAccounts(AccountTypeFilter.CURRENT);
        LiveData<List<Account>> accountsLiveData = loadAccountsUseCase.execute(params);
        
        // Подписываемся на изменения
        accountsLiveData.observeForever(this::handleDataLoaded);
    }
    
    /**
     * Загружает счета по типу
     */
    public void loadAccountsByType(AccountTypeFilter accountType) {
        LogManager.d(TAG, "Загрузка счетов типа: " + accountType);
        
        LoadAccountsUseCase.Params params = LoadAccountsUseCase.Params.forActiveAccounts(accountType);
        LiveData<List<Account>> accountsLiveData = loadAccountsUseCase.execute(params);
        
        // Подписываемся на изменения
        accountsLiveData.observeForever(this::handleDataLoaded);
    }
    
    /**
     * Возвращает счета текущего типа
     */
    public LiveData<List<Account>> getCurrentAccounts() {
        return loadAccountsUseCase.execute(LoadAccountsUseCase.Params.forActiveAccounts(AccountTypeFilter.CURRENT));
    }
    
    /**
     * Возвращает сберегательные счета
     */
    public LiveData<List<Account>> getSavingsAccounts() {
        return loadAccountsUseCase.execute(LoadAccountsUseCase.Params.forActiveAccounts(AccountTypeFilter.SAVINGS));
    }
    
    /**
     * Возвращает кредитные счета
     */
    public LiveData<List<Account>> getCreditAccounts() {
        return loadAccountsUseCase.execute(LoadAccountsUseCase.Params.forActiveAccounts(AccountTypeFilter.CREDIT));
    }
    
    /**
     * Возвращает короткое имя валюты по ID
     * Делегирует запрос специализированному CurrencyCacheService
     */
    public String getCurrencyShortName(int currencyId) {
        String currencyName = currencyCacheService.getCurrencyShortName(currencyId);
        LogManager.d(TAG, "Получена валюта для ID " + currencyId + ": " + currencyName);
        return currencyName;
    }
    
    @Override
    public void addItem(Account item) {
        LogManager.d(TAG, "Добавление счета: " + item.getTitle());
        
        List<Account> currentItems = items.getValue();
        if (currentItems != null) {
            currentItems.add(item);
            setItems(currentItems);
        }
    }
    
    @Override
    public void updateItem(Account item) {
        LogManager.d(TAG, "Обновление счета: " + item.getTitle());
        
        List<Account> currentItems = items.getValue();
        if (currentItems != null) {
            for (int i = 0; i < currentItems.size(); i++) {
                if (currentItems.get(i).getId() == item.getId()) {
                    currentItems.set(i, item);
                    break;
                }
            }
            setItems(currentItems);
        }
    }
    
    @Override
    public void removeItem(Account item) {
        LogManager.d(TAG, "Удаление счета: " + item.getTitle());
        
        List<Account> currentItems = items.getValue();
        if (currentItems != null) {
            currentItems.removeIf(account -> account.getId() == item.getId());
            setItems(currentItems);
        }
    }
    
    /**
     * Проверяет, загружен ли кэш валют
     * @return LiveData с состоянием загрузки кэша
     */
    public LiveData<Boolean> isCurrencyCacheLoaded() {
        return currencyCacheService.isCacheLoaded();
    }
    
    /**
     * Возвращает количество валют в кэше
     * @return количество валют
     */
    public int getCurrencyCacheSize() {
        return currencyCacheService.getCacheSize();
    }
    
    /**
     * Перезагружает кэш валют
     */
    public void reloadCurrencyCache() {
        LogManager.d(TAG, "Перезагрузка кэша валют");
        currencyCacheService.reloadCache();
    }
}
