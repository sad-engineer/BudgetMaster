package com.sadengineer.budgetmaster.accounts;

import android.app.Application;
 
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sadengineer.budgetmaster.backend.entity.Account;
import com.sadengineer.budgetmaster.backend.entity.Currency;
import com.sadengineer.budgetmaster.backend.service.AccountService;
import com.sadengineer.budgetmaster.backend.service.CurrencyService;
import com.sadengineer.budgetmaster.base.SelectionListViewModel;
import com.sadengineer.budgetmaster.calculators.AccountCalculatorViewModel;
import com.sadengineer.budgetmaster.backend.filters.AccountTypeFilter;
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;    
import com.sadengineer.budgetmaster.utils.LogManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Shared ViewModel для экрана счетов. Держит общий режим выбора и
 * выполняет мягкое удаление в фоновой очереди.
 */
public class AccountsSharedViewModel extends AndroidViewModel implements SelectionListViewModel {

    private static final String TAG = "AccountsSharedViewModel";

    /** Имя пользователя по умолчанию */
    /** TODO: передлать на получение имени пользователя из SharedPreferences */
    private String userName = "default_user";

    private AccountService mAccountService;
    private CurrencyService mCurrencyService;
    // LiveData для управления режимом выбора и мягким удалением
    private final MutableLiveData<Boolean> mSelectionMode = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> mDeleting = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> mSoftDeletionDone = new MutableLiveData<>();
    private final MutableLiveData<List<Account>> mSelectedAccounts = new MutableLiveData<>();
    
    // LiveData для редактирования счетов
    private final MutableLiveData<Account> mCurrentAccount = new MutableLiveData<>();
    private final MutableLiveData<List<Currency>> mCurrencies = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mSaving = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> mAccountSaved = new MutableLiveData<>();
    
    // Калькулятор для разных типов счетов
    private AccountCalculatorViewModel mCurrentCalculator;
    private AccountCalculatorViewModel mSavingsCalculator;
    private AccountCalculatorViewModel mCreditCalculator;

    // Кэш валют
    private final Map<Integer, String> mCurrencyCache = new HashMap<>();
    private final MutableLiveData<Boolean> mIsCurrencyCacheLoaded = new MutableLiveData<>(false);

    private final ExecutorService mIoExecutor = Executors.newSingleThreadExecutor();

    /**
     * Конструктор
     * @param application
     */
    public AccountsSharedViewModel(@NonNull Application application) {
        super(application);
        // Сервисы для работы с данными
        mAccountService = new AccountService(application.getApplicationContext(), userName);
        mCurrencyService = new CurrencyService(application.getApplicationContext(), userName);
        
        // Загружаем кэш валют при инициализации
        loadCurrencyCache();
    }

    /**
     * Возвращает режим выбора
     * @return режим выбора
     */
    @Override
    public LiveData<Boolean> getSelectionMode() {
        return mSelectionMode;
    }

    /**
     * Возвращает состояние удаления
     * @return состояние удаления
     */
    @Override
    public LiveData<Boolean> getDeleting() {
        return mDeleting;
    }

    /**
     * Возвращает количество удаленных счетов
     * @return количество удаленных счетов
     */
    @Override
    public LiveData<Integer> getSoftDeletionDone() {
        return mSoftDeletionDone;
    }

    /**
     * Возвращает выбранные счета
     * @return выбранные счета
     */
     public LiveData<List<Account>> getSelectedAccounts() {
         return mSelectedAccounts;
     }

    /**
     * Включает режим выбора
     */
    @Override
    public void enableSelectionMode() {
        mSelectionMode.setValue(true);
    }

    /**
     * Отменяет режим выбора
     */
    @Override
    public void cancelSelectionMode() {
        mSelectionMode.setValue(false);
        mSelectedAccounts.setValue(null);
    }

    /**
     * Делает softDelete для каждого счета из переданного списка в фоне.
     */
    public void deleteAccountsSoft(List<Account> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            mSoftDeletionDone.setValue(0);
            mSelectionMode.setValue(false);
            return;
        }

        // Фильтруем только неудаленные счета
        List<Account> accountsToDelete = new ArrayList<>();
        for (Account account : accounts) {
            if (!account.isDeleted()) {
                accountsToDelete.add(account);
            } else {
                LogManager.w(TAG, "Пропуск счёта: ID=" + account.getId() + ", уже удалён");
            }
        }

        if (accountsToDelete.isEmpty()) {
            LogManager.w(TAG, "Нет счетов для удаления - все уже удалены");
            mSoftDeletionDone.setValue(0);
            mSelectionMode.setValue(false);
            return;
        }

        LogManager.d(TAG, "Начато удаление счетов. Количество: " + accountsToDelete.size());
        // Устанавливаем состояние удаления
        mDeleting.setValue(true);
        mIoExecutor.execute(() -> {
            int deletedCount = 0;
            for (Account account : accountsToDelete) {
                try {
                    LogManager.d(TAG, "Удаление счёта: ID=" + account.getId());
                    mAccountService.delete(account, true);
                    deletedCount++;
                    LogManager.d(TAG, "Счет ID: " + account.getId() + " успешно удален");
                } catch (Exception e) {
                    LogManager.e(TAG, "Ошибка удаления счёта: ID=" + account.getId() + ", причина: " + e.getMessage());
                }
            }
            mDeleting.postValue(false);
            mSoftDeletionDone.postValue(deletedCount);
            mSelectionMode.postValue(false);
            mSelectedAccounts.postValue(null);
            LogManager.d(TAG, "Удаление завершено. Удалено счетов: " + deletedCount);
        });
    }

     /**
      * Обновляет текущий набор выбранных счетов.
      */
     public void setSelectedAccounts(List<Account> accounts) {
        LogManager.d(TAG, "Выбранных счетов: " + (accounts != null ? accounts.size() : 0));
        mSelectedAccounts.setValue(accounts);
     }

     /**
      * Делает softDelete для текущего набора выбранных счетов из ViewModel.
      */
    @Override
    public void deleteSelectedItemsSoft() {
         List<Account> accounts = mSelectedAccounts.getValue();
         deleteAccountsSoft(accounts);
     }
     
     /**
      * Возвращает сервис для работы с данными
      */
    public AccountService getService() {
        return mAccountService;
    }
    
    /**
     * Загружает счета по типу
     */
    public LiveData<List<Account>> loadAccountsByType(AccountTypeFilter accountType) {
        return mAccountService.getAllByType(accountType.getIndex(), EntityFilter.ALL);
    }
    
    /**
     * Возвращает калькулятор для указанного типа счетов
     */
    public AccountCalculatorViewModel getCalculator(AccountTypeFilter accountType) {
        switch (accountType) {
            case CURRENT:
                if (mCurrentCalculator == null) {
                    mCurrentCalculator = new AccountCalculatorViewModel(getApplication(), AccountTypeFilter.CURRENT);
                    mCurrentCalculator.initialize();
                }
                return mCurrentCalculator;
            case SAVINGS:
                if (mSavingsCalculator == null) {
                    mSavingsCalculator = new AccountCalculatorViewModel(getApplication(), AccountTypeFilter.SAVINGS);
                    mSavingsCalculator.initialize();
                }
                return mSavingsCalculator;
            case CREDIT:
                if (mCreditCalculator == null) {
                    mCreditCalculator = new AccountCalculatorViewModel(getApplication(), AccountTypeFilter.CREDIT);
                    mCreditCalculator.initialize();
                }
                return mCreditCalculator;
            default:
                return null;
        }
    }
    
    // ========== МЕТОДЫ ДЛЯ РЕДАКТИРОВАНИЯ СЧЕТОВ ==========
    
    /**
     * Возвращает текущий редактируемый счет
     */
    public LiveData<Account> getCurrentAccount() {
        return mCurrentAccount;
    }
    
    /**
     * Возвращает список валют
     */
    public LiveData<List<Currency>> getCurrencies() {
        return mCurrencies;
    }
    
    /**
     * Возвращает состояние сохранения
     */
    public LiveData<Boolean> getSaving() {
        return mSaving;
    }
    
    /**
     * Возвращает результат сохранения
     */
    public LiveData<Boolean> getAccountSaved() {
        return mAccountSaved;
    }
    
    /**
     * Загружает счет по ID для редактирования
     */
    public void loadAccountForEdit(int accountId) {
        if (accountId > 0) {
            mAccountService.getById(accountId).observeForever(account -> {
                if (account != null) {
                    mCurrentAccount.setValue(account);
                }
            });
        } else {
            // Создание нового счета
            mCurrentAccount.setValue(null);
        }
    }
    
    /**
     * Загружает список валют
     */
    public void loadCurrencies() {
        mCurrencyService.getAll().observeForever(currencyList -> {
            if (currencyList != null) {
                mCurrencies.setValue(currencyList);
            }
        });
    }
    
    /**
     * Сохраняет счет (создание или обновление)
     */
    public void saveAccount(Account account) {
        if (account == null) {
            mAccountSaved.setValue(false);
            return;
        }
        
        mSaving.setValue(true);
        mIoExecutor.execute(() -> {
            try {
                if (account.getId() > 0) {
                    // Обновление существующего счета
                    mAccountService.update(account);
                    LogManager.d(TAG, "Счет обновлен: ID=" + account.getId());
                } else {
                    // Создание нового счета
                    LogManager.d(TAG, "Создание нового счета: " + account.getTitle());
                    mAccountService.createWithoutValidation(
                        account.getTitle(),
                        account.getCurrencyId(),
                        account.getAmount(),
                        account.getType(),
                        account.getClosed()
                    );
                    LogManager.d(TAG, "Счет создан: " + account.getTitle());
                }
                mAccountSaved.postValue(true);
            } catch (Exception e) {
                LogManager.e(TAG, "Ошибка сохранения счета: " + e.getMessage());
                mAccountSaved.postValue(false);
            } finally {
                mSaving.postValue(false);
            }
        });
    }
    
    /**
     * Очищает данные редактирования
     */
    public void clearEditData() {
        mCurrentAccount.setValue(null);
        mAccountSaved.setValue(false);
    }
    
    /**
     * Загружает кэш валют
     */
    private void loadCurrencyCache() {
        try {
            // observeForever должен вызываться в главном потоке
            mCurrencyService.getAll(EntityFilter.ALL).observeForever(currencies -> {
                try {
                    if (currencies != null) {
                        mCurrencyCache.clear();
                        for (Currency currency : currencies) {
                            if (currency.getShortName() != null && !currency.getShortName().isEmpty()) {
                                mCurrencyCache.put(currency.getId(), currency.getShortName());
                            }
                        }
                        mIsCurrencyCacheLoaded.postValue(true);
                        LogManager.d(TAG, "Кэш валют загружен: " + mCurrencyCache.size() + " валют");
                    } else {
                        LogManager.w(TAG, "Список валют пуст");
                        mIsCurrencyCacheLoaded.postValue(false);
                    }
                } catch (Exception e) {
                    LogManager.e(TAG, "Ошибка обработки валют: " + e.getMessage(), e);
                    mIsCurrencyCacheLoaded.postValue(false);
                }
            });
        } catch (Exception e) {
            LogManager.e(TAG, "Ошибка загрузки кэша валют: " + e.getMessage(), e);
            mIsCurrencyCacheLoaded.postValue(false);
        }
    }
    
    /**
     * Получает короткое имя валюты по ID
     */
    public String getCurrencyShortName(int currencyId) {
        return mCurrencyCache.getOrDefault(currencyId, "RUB");
    }
    
    /**
     * Возвращает состояние загрузки кэша валют
     */
    public LiveData<Boolean> isCurrencyCacheLoaded() {
        return mIsCurrencyCacheLoaded;
    }
    
    /**
     * Перезагружает кэш валют
     */
    public void reloadCurrencyCache() {
        mCurrencyCache.clear();
        mIsCurrencyCacheLoaded.setValue(false);
        loadCurrencyCache();
    }
    
    /**
     * Очищает ресурсы при уничтожении ViewModel
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        // Очищаем ExecutorService
        if (mIoExecutor != null && !mIoExecutor.isShutdown()) {
            mIoExecutor.shutdown();
        }
        LogManager.d(TAG, "AccountsSharedViewModel очищен");
    }
}


