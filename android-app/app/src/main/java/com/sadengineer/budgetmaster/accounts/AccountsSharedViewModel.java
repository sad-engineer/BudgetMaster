package com.sadengineer.budgetmaster.accounts;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sadengineer.budgetmaster.backend.entity.Account;
import com.sadengineer.budgetmaster.backend.service.AccountService;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Shared ViewModel для экрана счетов. Держит общий режим выбора и
 * выполняет мягкое удаление в фоновой очереди.
 */
public class AccountsSharedViewModel extends AndroidViewModel {

    private final MutableLiveData<Boolean> selectionMode = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> deleting = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> softDeletionDone = new MutableLiveData<>();
     private final MutableLiveData<List<Account>> selectedAccounts = new MutableLiveData<>();

    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private final AccountService accountService;

    public AccountsSharedViewModel(@NonNull Application application) {
        super(application);
        accountService = new AccountService(application.getApplicationContext(), "default_user");
    }

    public LiveData<Boolean> getSelectionMode() {
        return selectionMode;
    }

    public LiveData<Boolean> getDeleting() {
        return deleting;
    }

    public LiveData<Integer> getSoftDeletionDone() {
        return softDeletionDone;
    }

     public LiveData<List<Account>> getSelectedAccounts() {
         return selectedAccounts;
     }

    public void enableSelectionMode() {
        selectionMode.setValue(true);
    }

    public void cancelSelectionMode() {
        selectionMode.setValue(false);
         selectedAccounts.setValue(null);
    }

    /**
     * Мягко удаляет переданный список счетов в фоне.
     */
    public void deleteAccountsSoft(List<Account> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            softDeletionDone.setValue(0);
            selectionMode.setValue(false);
            return;
        }

        deleting.setValue(true);
        ioExecutor.execute(() -> {
            int deletedCount = 0;
            for (Account account : accounts) {
                try {
                    accountService.softDelete(account);
                    deletedCount++;
                } catch (Exception ignored) {
                }
            }
            deleting.postValue(false);
            softDeletionDone.postValue(deletedCount);
            selectionMode.postValue(false);
             selectedAccounts.postValue(null);
        });
    }

     /**
      * Обновляет текущий набор выбранных счетов.
      */
     public void setSelectedAccounts(List<Account> accounts) {
         selectedAccounts.setValue(accounts);
     }

     /**
      * Мягко удаляет текущий набор выбранных счетов из ViewModel.
      */
     public void deleteSelectedAccountsSoft() {
         List<Account> accounts = selectedAccounts.getValue();
         deleteAccountsSoft(accounts);
     }
}


