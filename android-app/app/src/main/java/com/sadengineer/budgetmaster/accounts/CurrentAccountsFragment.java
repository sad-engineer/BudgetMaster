package com.sadengineer.budgetmaster.accounts;

import android.util.Log;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.backend.entity.Account;
import com.sadengineer.budgetmaster.backend.service.AccountService;
import com.sadengineer.budgetmaster.base.BaseListFragment;
import com.sadengineer.budgetmaster.backend.constants.ModelConstants;
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;
import com.sadengineer.budgetmaster.calculators.AccountCalculatorViewModel;
import com.sadengineer.budgetmaster.backend.filters.AccountTypeFilter;
import com.sadengineer.budgetmaster.formatters.CurrencyAmountFormatter;

import java.util.List;

/**
 * Фрагмент для отображения текущих счетов (тип 1)
 */
public class CurrentAccountsFragment extends BaseListFragment<Account, AccountsAdapter, AccountsSharedViewModel, AccountService> {
    
    private final int ACCOUNT_TYPE = ModelConstants.ACCOUNT_TYPE_CURRENT;
    
    // Калькулятор для общей суммы текущих счетов
    private AccountCalculatorViewModel accountCalculator;
    private CurrencyAmountFormatter formatter = new CurrencyAmountFormatter();
    
    /**
     * Возвращает layout для фрагмента
     */
    @Override
    protected int getLayoutResourceId() {
        return R.layout.fragment_current_accounts;
    }

    /**
     * Возвращает id recyclerView для фрагмента
     */
    @Override
    protected int getRecyclerViewId() {
        return R.id.accounts_current_recycler;
    }

    /**
     * Возвращает класс ViewModel для фрагмента
     */
    @Override
    protected Class<AccountsSharedViewModel> getViewModelClass() {
        return AccountsSharedViewModel.class;
    }

    /**
     * Возвращает класс сервиса для фрагмента
     */
    @Override
    protected Class<AccountService> getServiceClass() {
        return AccountService.class;
    }

    /**
     * Возвращает параметры для загрузки данных
     */
    @Override
    protected Object getLoadParameters() {
        return ACCOUNT_TYPE; // Тип текущих счетов
    }

    /**
     * Возвращает source_tab для фрагмента
     */
    @Override
    protected int getSourceTab() {
        return 0; // 0 = Текущие
    }

    /**
     * Выполняет загрузку данных
     */
    @Override
    protected void performDataLoading() {
        // Используем сервис из базового класса
        AccountService service = getServiceInstance();
        if (service != null) {
            service.getAllByType(ACCOUNT_TYPE, EntityFilter.ALL).observe(getViewLifecycleOwner(), this::handleDataLoaded);
        }
    }

    /**
     * Устанавливает данные в адаптер
     */
    @Override
    protected void setAdapterData(List<Account> items) {
        adapter.setAccounts(items);
    }

    /**
     * Возвращает класс активности для редактирования
     */
    @Override
    protected Class<?> getEditActivityClass() {
        return AccountsEditActivity.class;
    }

    /**
     * Наблюдает за режимом выбора
     */
    @Override
    protected void observeSelectionMode() {
        viewModel.getSelectionMode().observe(getViewLifecycleOwner(), enabled -> {
            if (adapter != null) {
                adapter.setSelectionMode(Boolean.TRUE.equals(enabled));
            }
        });
    }

    /**
     * Настраивает адаптер
     */
    @Override
    protected void setupAdapter() {
        adapter = new AccountsAdapter(new AccountsAdapter.OnAccountClickListener() {
            @Override
            public void onAccountClick(Account account) {
                Log.d(TAG, "Переход к окну редактирования счёта");
                goToEdit(account);
            }
        }, requireContext());
        
        adapter.setLongClickListener(new AccountsAdapter.OnAccountLongClickListener() {
            @Override
            public void onAccountLongClick(Account account) {
                Log.d(TAG, "Длительное нажатие на текущий счет: " + account.getTitle());
                showDeleteConfirmationDialog(account);
            }
        });
        
        recyclerView.setAdapter(adapter);

        adapter.setOnSelectedAccountsChanged(selected -> {
            viewModel.setSelectedAccounts(selected);
        });
        
        // Инициализируем калькулятор текущих счетов
        accountCalculator = new AccountCalculatorViewModel(requireActivity().getApplication(), AccountTypeFilter.CURRENT);
        accountCalculator.initialize();
        
        // Подписываемся на изменения общей суммы текущих счетов
        setupAccountCalculatorObserver();
    }

    /**
     * Настраивает Observer для калькулятора текущих счетов
     */
    private void setupAccountCalculatorObserver() {
        if (accountCalculator != null) {
            accountCalculator.getTotalAmount().observe(getViewLifecycleOwner(), totalAmount -> {
                if (totalAmount != null && adapter != null) {
                    adapter.setTotalAmount(totalAmount);
                    Log.d(TAG, "Общая сумма текущих счетов обновлена: " + totalAmount);
                }
            });
        }
    }
    
    /**
     * Выполняет удаление
     */
    @Override
    protected void performDelete(AccountService service, Account item) {
        service.delete(item, false);
    }

    /**
     * Возвращает заголовок элемента для логирования
     */
    @Override
    protected String getItemTitle(Account item) {
        return "ID: " + item.getId() + ", title: " + item.getTitle() + 
               ", position: " + item.getPosition() + ", deleteTime: " + item.getDeleteTime() + 
               ", deletedBy: " + item.getDeletedBy() + ", isDeleted: " + item.isDeleted();
    }
} 