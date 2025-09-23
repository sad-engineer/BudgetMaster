package com.sadengineer.budgetmaster.accounts;

import android.content.Context;
import android.os.Bundle;
 
import com.sadengineer.budgetmaster.utils.LogManager;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sadengineer.budgetmaster.backend.entity.Account;
import com.sadengineer.budgetmaster.backend.service.AccountService;
import com.sadengineer.budgetmaster.base.BaseListFragmentN;
import com.sadengineer.budgetmaster.base.TabInteractionManager;
import com.sadengineer.budgetmaster.backend.filters.AccountTypeFilter;
import com.sadengineer.budgetmaster.calculators.AccountCalculatorViewModel;

import java.util.List;

/**
 * Базовый фрагмент для отображения счетов разных типов
 * Универсальный класс, который работает с любым типом счетов
 */
public abstract class BaseAccountsFragment extends BaseListFragmentN<Account,
        AccountsAdapter, AccountsSharedViewModel, AccountService> {
    
    
    // Переменные класса, которые настраиваются в наследниках
    // тип счета
    protected AccountTypeFilter mAccountType;

    // менеджер для управления взаимодействием с вкладками
    protected TabInteractionManager<Account, AccountService> mTabInteractionManager;
    
    /**
     * Конструктор, который заставляет инициализировать переменные 
     */
    protected BaseAccountsFragment(int sourceTab) {
        // Инициализируем переменные для BaseListFragmentN
        this.mSourceTab = sourceTab;
        this.mViewModelClass = AccountsSharedViewModel.class;
        this.mEditActivityClass = AccountsEditActivity.class;
        
        // инициализируем ресурсы (mLayoutResourceId, mRecyclerViewId, accountType)
        initializeResources();
    }
    
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }
    
    /**
     * Инициализирует ресурсы фрагмента layoutResourceId, recyclerViewId, accountType
     * Остальные переменные уже установлены в конструкторе
     * Должен быть переопределен в наследниках
     */
    protected abstract void initializeResources();
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Инициализируем TabInteractionManager после создания View и ViewModel
        if (mViewModel != null) {
            AccountService service = mViewModel.getService();
            this.mTabInteractionManager = new TabInteractionManager<>(requireContext(), service, "default_user");
        }
    }

    /**
     * Возвращает параметры для загрузки данных
     * *для логирования "LogManager.d(TAG, "Загрузка данных с параметрами: " + getLoadParameters());"
     */
    @Override
    protected Object getLoadParameters() {
        return "тип счетов: " + mAccountType.getIndex();
    }

    /**
     * Выполняет загрузку данных
     * логгирование не обьявляем - уже есть в базовом классе
     */
    @Override
    protected void performDataLoading() {
        // Получаем данные из ViewModel по типу счетов
        if (mViewModel != null) {
            mViewModel.loadAccountsByType(mAccountType).observe(getViewLifecycleOwner(), this::handleDataLoaded);
        }
    }

    /**
     * Устанавливает данные в адаптер
     */
    @Override
    protected void setAdapterData(List<Account> items) {
        mAdapter.setItems(items);
    }

    /**
     * Наблюдает за режимом выбора
     */
    @Override
    protected void observeSelectionMode() {
        mViewModel.getSelectionMode().observe(getViewLifecycleOwner(), enabled -> {
            if (mAdapter != null) {
                mAdapter.setSelectionMode(Boolean.TRUE.equals(enabled));
            }
        });
    }

    /**
     * Настраивает адаптер
     */
    @Override
    protected void setupAdapter() {
        mAdapter = new AccountsAdapter(new AccountsAdapter.OnAccountClickListener() {
            @Override
            public void onAccountClick(Account account) {
                String message = "Вкладка %s счетов. Переход к окну редактирования счёта: %s";
                LogManager.d(TAG, String.format(message, getAccountTypeName(), account.getTitle()));
                goToEdit(account);
            }
        }, new AccountsAdapter.CurrencyProvider() {
            @Override
            public String getCurrencyShortName(int currencyId) {
                return mViewModel != null ? mViewModel.getCurrencyShortName(currencyId) : "RUB";
            }
        });
        
        mAdapter.setLongClickListener(new AccountsAdapter.OnAccountLongClickListener() {
            @Override
            public void onAccountLongClick(Account account) {
                String message = "Вкладка %s счетов. Длительное нажатие на: %s";
                LogManager.d(TAG, String.format(message, getAccountTypeName(), account.getTitle()));
                mTabInteractionManager.showDeleteConfirmationDialog(account, account.getTitle());
            }
        });
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnSelectedAccountsChanged(selected -> mViewModel.setSelectedAccounts(selected));
        
        // Подписываемся на изменения общей суммы счетов через ViewModel
        setupAccountCalculatorObserver();
    }

    /**
     * Настраивает Observer для калькулятора счетов
     */
    private void setupAccountCalculatorObserver() {
        if (mViewModel != null) {
            AccountCalculatorViewModel calculator = mViewModel.getCalculator(mAccountType);
            if (calculator != null) {
                calculator.getTotalAmount().observe(getViewLifecycleOwner(), totalAmount -> {
                    if (totalAmount != null && mAdapter != null) {
                        mAdapter.setTotalAmount(totalAmount);
                        LogManager.d(TAG, "Общая сумма " + getAccountTypeName() + " счетов обновлена: " + totalAmount);
                    }
                });
            }
        }
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
        switch (mAccountType) {
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
