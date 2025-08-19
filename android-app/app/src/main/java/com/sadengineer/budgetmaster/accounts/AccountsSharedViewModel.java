package com.sadengineer.budgetmaster.accounts;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sadengineer.budgetmaster.backend.entity.Account;
import com.sadengineer.budgetmaster.backend.service.AccountService;
import com.sadengineer.budgetmaster.base.SelectionListViewModel;    

import java.util.ArrayList;
import java.util.List;
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

    private AccountService accountService;
    // LiveData для управления режимом выбора и мягким удалением
    private final MutableLiveData<Boolean> selectionMode = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> deleting = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> softDeletionDone = new MutableLiveData<>();
    private final MutableLiveData<List<Account>> selectedAccounts = new MutableLiveData<>();

    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    /**
     * Конструктор
     * @param application
     */
    public AccountsSharedViewModel(@NonNull Application application) {
        super(application);
        // Сервисы для работы с данными
        accountService = new AccountService(application.getApplicationContext(), userName);
    }

    /**
     * Возвращает режим выбора
     * @return режим выбора
     */
    @Override
    public LiveData<Boolean> getSelectionMode() {
        return selectionMode;
    }

    /**
     * Возвращает состояние удаления
     * @return состояние удаления
     */
    @Override
    public LiveData<Boolean> getDeleting() {
        return deleting;
    }

    /**
     * Возвращает количество удаленных счетов
     * @return количество удаленных счетов
     */
    @Override
    public LiveData<Integer> getSoftDeletionDone() {
        return softDeletionDone;
    }

    /**
     * Возвращает выбранные счета
     * @return выбранные счета
     */
     public LiveData<List<Account>> getSelectedAccounts() {
         return selectedAccounts;
     }

    /**
     * Включает режим выбора
     */
    @Override
    public void enableSelectionMode() {
        selectionMode.setValue(true);
    }

    /**
     * Отменяет режим выбора
     */
    @Override
    public void cancelSelectionMode() {
        selectionMode.setValue(false);
        selectedAccounts.setValue(null);
    }

    /**
     * Делает softDelete для каждого счета из переданного списка в фоне.
     */
    public void deleteAccountsSoft(List<Account> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            softDeletionDone.setValue(0);
            selectionMode.setValue(false);
            return;
        }

        // Фильтруем только неудаленные счета
        List<Account> accountsToDelete = new ArrayList<>();
        for (Account account : accounts) {
            if (!account.isDeleted()) {
                accountsToDelete.add(account);
            } else {
                Log.w(TAG, "Пропуск счёта: ID=" + account.getId() + ", уже удалён");
            }
        }

        if (accountsToDelete.isEmpty()) {
            Log.w(TAG, "Нет счетов для удаления - все уже удалены");
            softDeletionDone.setValue(0);
            selectionMode.setValue(false);
            return;
        }

        Log.d(TAG, "Начато удаление счетов. Количество: " + accountsToDelete.size());
        // Устанавливаем состояние удаления
        deleting.setValue(true);
        ioExecutor.execute(() -> {
            int deletedCount = 0;
            for (Account account : accountsToDelete) {
                try {
                    Log.d(TAG, "Удаление счёта: ID=" + account.getId());
                    accountService.delete(true, account);
                    deletedCount++;
                    Log.d(TAG, "Счет ID: " + account.getId() + " успешно удален");
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка удаления счёта: ID=" + account.getId() + ", причина: " + e.getMessage());
                }
            }
            deleting.postValue(false);
            softDeletionDone.postValue(deletedCount);
            selectionMode.postValue(false);
            selectedAccounts.postValue(null);
            Log.d(TAG, "Удаление завершено. Удалено счетов: " + deletedCount);
        });
    }

     /**
      * Обновляет текущий набор выбранных счетов.
      */
     public void setSelectedAccounts(List<Account> accounts) {
        Log.d(TAG, "Выбранных счетов: " + (accounts != null ? accounts.size() : 0));
        selectedAccounts.setValue(accounts);
     }

     /**
      * Делает softDelete для текущего набора выбранных счетов из ViewModel.
      */
    @Override
    public void deleteSelectedItemsSoft() {
         List<Account> accounts = selectedAccounts.getValue();
         deleteAccountsSoft(accounts);
     }
}


