package com.sadengineer.budgetmaster.budget;

import android.content.Intent;
import android.util.Log;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.base.BaseListFragment;
import com.sadengineer.budgetmaster.backend.database.BudgetMasterDatabase;
import com.sadengineer.budgetmaster.backend.entity.Budget;
import com.sadengineer.budgetmaster.backend.entity.Category;
import com.sadengineer.budgetmaster.backend.entity.Currency;
import com.sadengineer.budgetmaster.backend.service.BudgetService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Фрагмент для отображения лимитов бюджетов
 */
public class BudgetLimitsFragment extends BaseListFragment<Budget, BudgetAdapter, BudgetSharedViewModel, BudgetService> {
    
    private static final String TAG = "BudgetLimitsFragment";   

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
    protected Class<BudgetService> getServiceClass() {
        return BudgetService.class;
    }

    @Override
    protected Object getLoadParameters() {
        return null; // Загружаем все бюджеты
    }

    @Override
    protected int getSourceTab() {
        return 0; // 0 = Лимиты
    }

    @Override
    protected void performDataLoading() {
        BudgetMasterDatabase database = BudgetMasterDatabase.getDatabase(requireContext());
        
        // Загружаем только бюджеты для категорий расходов (operation_type = 1)
        database.budgetDao().getAllActiveForExpenses().observe(getViewLifecycleOwner(), this::handleDataLoaded);
        Log.d(TAG, "Загружаем бюджеты только для категорий расходов (operation_type = 1)");
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
    }
    
    /**
     * Загружает категории и валюты один раз при создании фрагмента
     */
    private void loadCategoriesAndCurrencies() {
        BudgetMasterDatabase database = BudgetMasterDatabase.getDatabase(requireContext());
        
        // Загружаем только категории расходов (operation_type = 1)
        database.categoryDao().getAllActive().observe(getViewLifecycleOwner(), allCategories -> {
            if (allCategories != null) {
                // Фильтруем только категории расходов
                List<Category> expenseCategories = allCategories.stream()
                    .filter(category -> category.getOperationType() ==  ModelConstants.OPERATION_TYPE_EXPENSE) // 1 = расход
                    .collect(Collectors.toList());
                
                if (adapter != null) {
                    adapter.setCategories(expenseCategories);
                    Log.d(TAG, "Установлено категорий расходов в адаптер: " + expenseCategories.size());
                }
            }
        });
        
        // Загружаем валюты один раз
        database.currencyDao().getAll().observe(getViewLifecycleOwner(), currencies -> {
            if (adapter != null) {
                adapter.setCurrencies(currencies);
                Log.d(TAG, "Установлено валют в адаптер: " + (currencies != null ? currencies.size() : 0));
            }
        });
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
    protected void performDelete(BudgetService service, Budget item) {
        service.delete(false, item); // false = hard delete
    }

    @Override
    protected String getItemTitle(Budget item) {
        return "ID: " + item.getId() + ", amount: " + item.getAmount() + 
               ", categoryId: " + item.getCategoryId() + ", currencyId: " + item.getCurrencyId();
    }
} 