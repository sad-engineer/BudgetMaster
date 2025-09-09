package com.sadengineer.budgetmaster.accounts;

import android.util.Log;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.backend.entity.Account;
import com.sadengineer.budgetmaster.backend.service.AccountService;
import com.sadengineer.budgetmaster.base.BaseListFragmentN;
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;
import com.sadengineer.budgetmaster.calculators.AccountCalculatorViewModel;
import com.sadengineer.budgetmaster.backend.filters.AccountTypeFilter;
import com.sadengineer.budgetmaster.formatters.CurrencyAmountFormatter;
import com.sadengineer.budgetmaster.accounts.AccountsEditActivity;

import java.util.List;

/**
 * Базовый фрагмент для отображения счетов разных типов
 * Универсальный класс, который работает с любым типом счетов
 */
public abstract class BaseAccountsFragment extends BaseListFragmentN<Account, AccountsAdapter, AccountsSharedViewModel, AccountService, AccountCalculatorViewModel, CurrencyAmountFormatter> {
    
    // Переменные класса, которые настраиваются в наследниках
    // тип счета
    protected AccountTypeFilter accountType;
    
    /**
     * Конструктор, который заставляет инициализировать переменные 
     */
    protected BaseAccountsFragment(int sourceTab) {
        // Инициализируем переменные для BaseListFragmentN
        this.sourceTab = sourceTab;
        this.viewModelClass = AccountsSharedViewModel.class;
        this.serviceClass = AccountService.class;
        this.editActivityClass = AccountsEditActivity.class;
        this.calculator = new AccountCalculatorViewModel();
        this.formatter = new CurrencyAmountFormatter();
        
        // инициализируем ресурсы (layoutResourceId, recyclerViewId, accountType)
        initializeResources();
    }
    
    /**
     * Инициализирует ресурсы фрагмента layoutResourceId, recyclerViewId, accountType
     * Остальные переменные уже установлены в конструкторе
     * Должен быть переопределен в наследниках
     */
    protected abstract void initializeResources();
    
    /**
     * Возвращает параметры для загрузки данных
     */
    @Override
    protected Object getLoadParameters() {
        return "тип счетов: " + accountType.getIndex();
    }

    /**
     * Выполняет загрузку данных
     */
    @Override
    protected void performDataLoading() {
        AccountService service = getServiceInstance();
        if (service != null) {
            service.getAllByType(accountType.getIndex(), EntityFilter.ALL)
                    .observe(getViewLifecycleOwner(), this::handleDataLoaded);
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
                String message = "Вкладка %s счетов. Переход к окну редактирования счёта: %s";
                Log.d(TAG, String.format(message, getAccountTypeName(), account.getTitle()));
                goToEdit(account);
            }
        }, requireContext());
        
        adapter.setLongClickListener(new AccountsAdapter.OnAccountLongClickListener() {
            @Override
            public void onAccountLongClick(Account account) {
                String message = "Вкладка %s счетов. Длительное нажатие на: %s";
                Log.d(TAG, String.format(message, getAccountTypeName(), account.getTitle()));
                showDeleteConfirmationDialog(account);
            }
        });
        
        recyclerView.setAdapter(adapter);

        adapter.setOnSelectedAccountsChanged(selected -> viewModel.setSelectedAccounts(selected));
        
        // Инициализируем калькулятор для данного типа счетов
        calculator = new AccountCalculatorViewModel(requireActivity().getApplication(), accountType);
        calculator.initialize();
        
        // Подписываемся на изменения общей суммы счетов
        setupAccountCalculatorObserver();
    }

    /**
     * Настраивает Observer для калькулятора счетов
     */
    private void setupAccountCalculatorObserver() {
        if (calculator != null) {
            calculator.getTotalAmount().observe(getViewLifecycleOwner(), totalAmount -> {
                if (totalAmount != null && adapter != null) {
                    adapter.setTotalAmount(totalAmount);
                    Log.d(TAG, "Общая сумма " + getAccountTypeName() + " счетов обновлена: " + totalAmount);
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
    
    /**
     * Возвращает название типа счетов для логирования
     */
    private String getAccountTypeName() {
        switch (accountType) {
            case CURRENT:
                return "текущих";
            case SAVINGS:
                return "сберегательных";
            case CREDIT:
                return "кредитных";
            default:
                return "неизвестных";
        }
    }
}
