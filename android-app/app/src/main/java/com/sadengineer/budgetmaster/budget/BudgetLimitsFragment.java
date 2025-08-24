package com.sadengineer.budgetmaster.budget;

import android.os.Bundle;
import android.util.Log;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.base.BaseListFragment;      
import com.sadengineer.budgetmaster.backend.entity.Budget;
import com.sadengineer.budgetmaster.backend.entity.Category;
import com.sadengineer.budgetmaster.backend.service.BudgetService;
import com.sadengineer.budgetmaster.backend.service.CurrencyService;
import com.sadengineer.budgetmaster.backend.service.CategoryService;
import com.sadengineer.budgetmaster.backend.filters.OperationTypeFilter; 
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Фрагмент для отображения лимитов бюджетов
 */
public class BudgetLimitsFragment extends BaseListFragment<Budget, BudgetAdapter, BudgetSharedViewModel, BudgetService> {
    
    private static final String TAG = "BudgetLimitsFragment";   

    /** Имя пользователя по умолчанию */
    /** TODO: передлать на получение имени пользователя из SharedPreferences */
    private String userName = "default_user";
    
    private BudgetService budgetService;
    private CurrencyService currencyService;
    private CategoryService categoryService;
    
    private OperationTypeFilter operationType = OperationTypeFilter.EXPENSE;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Инициализируем сервисы с правильным контекстом
        budgetService = new BudgetService(requireContext(), userName);
        currencyService = new CurrencyService(requireContext(), userName);
        categoryService = new CategoryService(requireContext(), userName);
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

    /**
     * Загружаем бюджеты только для категорий расходов (operation_type = 1)
     */
    @Override
    protected void performDataLoading() {
        budgetService.getAllByOperationType(operationType, EntityFilter.ACTIVE).observe(getViewLifecycleOwner(), this::handleDataLoaded);
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
    }
    
    /**
     * Загружает категории и валюты один раз при создании фрагмента
     */
    private void loadCategoriesAndCurrencies() {
        // Загружаем категории для текущего типа операций
        categoryService.getAllByOperationType(operationType.getIndex(), EntityFilter.ACTIVE)
            .observe(getViewLifecycleOwner(), categories -> {
                if (adapter != null && categories != null) {
                    adapter.setCategories(categories);
                    Log.d(TAG, "Установлено категорий в адаптер: " + categories.size());
                }
            });
        
        // Загружаем валюты
        currencyService.getAll().observe(getViewLifecycleOwner(), currencies -> {
            if (adapter != null && currencies != null) {
                adapter.setCurrencies(currencies);
                Log.d(TAG, "Установлено валют в адаптер: " + currencies.size());
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
        service.delete(item, false); // false = hard delete
    }

    @Override
    protected String getItemTitle(Budget item) {
        return "ID: " + item.getId() + ", amount: " + item.getAmount() + 
               ", categoryId: " + item.getCategoryId() + ", currencyId: " + item.getCurrencyId();
    }
} 