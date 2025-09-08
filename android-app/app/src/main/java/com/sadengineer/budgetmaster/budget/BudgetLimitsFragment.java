package com.sadengineer.budgetmaster.budget;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.lifecycle.ViewModelProvider;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.base.BaseListFragment;      
import com.sadengineer.budgetmaster.backend.entity.Budget;
import com.sadengineer.budgetmaster.backend.entity.Category;
import com.sadengineer.budgetmaster.backend.service.ServiceManager;
import com.sadengineer.budgetmaster.backend.filters.OperationTypeFilter; 
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;
import com.sadengineer.budgetmaster.calculators.BudgetCalculatorViewModel;
import com.sadengineer.budgetmaster.formatters.CurrencyAmountFormatter;
import com.sadengineer.budgetmaster.start.StartScreenViewModel;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Фрагмент для отображения лимитов бюджетов
 */
public class BudgetLimitsFragment extends BaseListFragment<Budget, BudgetAdapter, BudgetSharedViewModel, ServiceManager.Budgets> {
    
    private static final String TAG = "BudgetLimitsFragment";   

    /** Имя пользователя по умолчанию */
    /** TODO: передлать на получение имени пользователя из SharedPreferences */
    private String userName = "default_user";
    
    private ServiceManager serviceManager;
    
    private OperationTypeFilter operationType = OperationTypeFilter.EXPENSE;
    
    // Калькулятор для общей суммы бюджетов
    private BudgetCalculatorViewModel budgetCalculator;
    private CurrencyAmountFormatter formatter = new CurrencyAmountFormatter();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Инициализируем ServiceManager
        serviceManager = ServiceManager.getInstance(getContext(), userName);
        
        // Получаем калькулятор бюджетов из StartScreenViewModel
        // Это обеспечит синхронизацию с главным экраном
        StartScreenViewModel mainScreenViewModel = new ViewModelProvider(requireActivity()).get(StartScreenViewModel.class);
        budgetCalculator = mainScreenViewModel.getBudgetCalculator();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.fragment_budget_limits;
    }

    @Override
    protected int getRecyclerViewId() {
        return R.id.budget_limits_recycler;
    }

    @Override
    protected Class<BudgetSharedViewModel> getViewModelClass() {
        return BudgetSharedViewModel.class;
    }

    @Override
    protected Class<ServiceManager.Budgets> getServiceClass() {
        return ServiceManager.Budgets.class;
    }

    @Override
    protected Object getLoadParameters() {
        return null; // Загружаем все бюджеты
    }

    @Override
    protected int getSourceTab() {
        return 0; // 0 = Лимиты
    }

    /**
     * Загружаем бюджеты только для категорий расходов (operation_type = 1)
     */
    @Override
    protected void performDataLoading() {
        serviceManager.budgets.getAllByOperationType(operationType, EntityFilter.ACTIVE).observe(getViewLifecycleOwner(), this::handleDataLoaded);
        Log.d(TAG, "Загружаем бюджеты только для категорий расходов (operation_type = " + operationType.getIndex() + ")");
    }
    
    @Override
    protected void setupAdapter() {
        adapter = new BudgetAdapter();
        
        adapter.setClickListener(budget -> {
            Log.d(TAG, "Клик по бюджету: " + budget.getId());
            goToEdit(budget);
        });
        
        // Длительный клик отключен, так как удаление бюджетов не предусмотрено
        
        // Режим выбора отключен, так как кнопки скрыты
        
        recyclerView.setAdapter(adapter);
        
        // Загружаем категории и валюты один раз при создании адаптера
        loadCategoriesAndCurrencies();
        
        // Подписываемся на изменения общей суммы бюджетов
        setupBudgetCalculatorObserver();
    }
    
    /**
     * Загружает категории и валюты один раз при создании фрагмента
     */
    private void loadCategoriesAndCurrencies() {
        // Загружаем категории для текущего типа операций
        serviceManager.categories.getAllByOperationType(operationType.getIndex(), EntityFilter.ACTIVE)
            .observe(getViewLifecycleOwner(), categories -> {
                if (adapter != null && categories != null) {
                    adapter.setCategories(categories);
                    Log.d(TAG, "Установлено категорий в адаптер: " + categories.size());
                }
            });
        
        // Загружаем валюты
        serviceManager.currencies.getAll().observe(getViewLifecycleOwner(), currencies -> {
            if (adapter != null && currencies != null) {
                adapter.setCurrencies(currencies);
                Log.d(TAG, "Установлено валют в адаптер: " + currencies.size());
            }
        });
    }
    
    /**
     * Настраивает наблюдение за изменениями общей суммы бюджетов
     */
    private void setupBudgetCalculatorObserver() {
        if (budgetCalculator != null) {
            budgetCalculator.getResultAmount().observe(getViewLifecycleOwner(), totalAmount -> {
                if (totalAmount != null && adapter != null) {
                    // Обновляем карточку "Итого" в адаптере
                    adapter.updateTotalAmount(totalAmount);
                    Log.d(TAG, "Обновлена общая сумма бюджетов: " + formatter.formatFromCents(totalAmount));
                }
            });
        }
    }

    @Override
    protected void setAdapterData(List<Budget> items) {
        Log.d(TAG, "setAdapterData: получено бюджетов: " + (items != null ? items.size() : 0));
        if (items != null) {
            for (Budget budget : items) {
                Log.d(TAG, "  - Бюджет ID=" + budget.getId() + 
                          ", сумма=" + budget.getAmount() + 
                          ", категория=" + budget.getCategoryId() + 
                          ", валюта=" + budget.getCurrencyId());
            }
        }
        adapter.setBudgets(items);
    }

    @Override
    protected Class<?> getEditActivityClass() {
        return BudgetEditActivity.class;
    }

    @Override
    protected void observeSelectionMode() {
        // Режим выбора отключен, так как кнопки скрыты
        Log.d(TAG, "Режим выбора отключен - кнопки скрыты");
    }

    @Override
    protected void performDelete(ServiceManager.Budgets service, Budget item) {
        service.delete(item, false); // false = hard delete
    }

    @Override
    protected String getItemTitle(Budget item) {
        return "ID: " + item.getId() + ", amount: " + item.getAmount() + 
               ", categoryId: " + item.getCategoryId() + ", currencyId: " + item.getCurrencyId();
    }
} 