package com.sadengineer.budgetmaster.accounts.base;

import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.accounts.AccountsAdapter;
import com.sadengineer.budgetmaster.accounts.AccountsEditActivity;
import com.sadengineer.budgetmaster.backend.entity.Account;
import com.sadengineer.budgetmaster.backend.filters.AccountTypeFilter;
import com.sadengineer.budgetmaster.utils.LogManager;

import java.util.List;

/**
 * Упрощенный фрагмент для счетов
 * Заменяет сложный BaseAccountsFragment
 * Использует новую архитектуру с SimpleListFragment
 */
public abstract class SimpleAccountsFragment extends SimpleListFragment<Account, AccountsListViewModel> {
    
    protected AccountTypeFilter accountType;
    
    /**
     * Конструктор с типом счетов
     */
    public SimpleAccountsFragment(AccountTypeFilter accountType) {
        this.accountType = accountType;
    }
    
    @Override
    protected Class<AccountsListViewModel> getViewModelClass() {
        return AccountsListViewModel.class;
    }
    
    @Override
    protected void setupAdapter() {
        LogManager.d(TAG, "Настройка адаптера для " + getAccountTypeName());
        
        adapter = new AccountsAdapter(
            // Обработчик клика на счет
            account -> {
                LogManager.d(TAG, "Клик на счет: " + account.getTitle());
                goToEdit(account, AccountsEditActivity.class);
            },
            // Провайдер валют
            currencyId -> viewModel.getCurrencyShortName(currencyId)
        );
        
        // Настройка длинного клика для удаления
        if (adapter instanceof AccountsAdapter) {
            ((AccountsAdapter) adapter).setLongClickListener(account -> {
                LogManager.d(TAG, "Длинный клик на счет: " + account.getTitle());
                showDeleteConfirmationDialog(account);
            });
        }
        
        recyclerView.setAdapter(adapter);
    }
    
    @Override
    protected void loadData() {
        LogManager.d(TAG, "Загрузка данных для " + getAccountTypeName());
        
        // Подписка на данные из ViewModel
        viewModel.getItems().observe(getViewLifecycleOwner(), this::handleDataLoaded);
        
        // Подписка на состояние загрузки
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), this::handleLoadingState);
        
        // Подписка на ошибки
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), this::handleError);
        
        // Подписка на загрузку кэша валют
        viewModel.isCurrencyCacheLoaded().observe(getViewLifecycleOwner(), this::handleCurrencyCacheLoaded);
        
        // Загружаем данные
        viewModel.loadAccountsByType(accountType);
    }
    
    /**
     * Обрабатывает загруженные данные
     */
    private void handleDataLoaded(List<Account> accounts) {
        LogManager.d(TAG, "Получены данные: " + (accounts != null ? accounts.size() : 0) + " счетов");
        
        if (adapter instanceof AccountsAdapter) {
            ((AccountsAdapter) adapter).setItems(accounts);
        }
    }
    
    /**
     * Обрабатывает состояние загрузки
     */
    private void handleLoadingState(Boolean isLoading) {
        if (isLoading != null && isLoading) {
            LogManager.d(TAG, "Загрузка данных...");
            // TODO: показать индикатор загрузки
        }
    }
    
    /**
     * Обрабатывает ошибки
     */
    private void handleError(String errorMessage) {
        if (errorMessage != null && !errorMessage.isEmpty()) {
            LogManager.e(TAG, "Ошибка: " + errorMessage);
            // TODO: показать ошибку пользователю
        }
    }
    
    /**
     * Обрабатывает загрузку кэша валют
     */
    private void handleCurrencyCacheLoaded(Boolean isLoaded) {
        if (isLoaded != null && isLoaded) {
            LogManager.d(TAG, "Кэш валют загружен. Размер: " + viewModel.getCurrencyCacheSize());
            // Можно обновить адаптер, если нужно
        }
    }
    
    /**
     * Показывает диалог подтверждения удаления
     */
    protected void showDeleteConfirmationDialog(Account account) {
        // TODO: Реализовать диалог подтверждения удаления
        LogManager.d(TAG, "Показать диалог удаления для: " + account.getTitle());
    }
    
    /**
     * Возвращает название типа счетов для логирования
     */
    private String getAccountTypeName() {
        switch (accountType) {
            case CURRENT:
                return "текущих счетов";
            case SAVINGS:
                return "сберегательных счетов";
            case CREDIT:
                return "кредитных счетов";
            default:
                return "неизвестного типа счетов";
        }
    }
}
