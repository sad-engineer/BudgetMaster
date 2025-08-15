package com.sadengineer.budgetmaster.budget;

import android.content.Intent;
import android.util.Log;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.base.BaseListFragment;
import com.sadengineer.budgetmaster.backend.database.BudgetMasterDatabase;
import com.sadengineer.budgetmaster.backend.entity.Budget;
import com.sadengineer.budgetmaster.backend.service.BudgetService;

import java.util.List;

/**
 * Фрагмент для отображения лимитов бюджетов
 */
public class BudgetLimitsFragment extends BaseListFragment<Budget, BudgetAdapter, BudgetSharedViewModel, BudgetService> {

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
        database.budgetDao().getAll().observe(getViewLifecycleOwner(), this::handleDataLoaded);
    }

    @Override
    protected void setAdapterData(List<Budget> items) {
        adapter.setBudgets(items);
    }

    @Override
    protected Class<?> getEditActivityClass() {
        return BudgetEditActivity.class;
    }

    @Override
    protected void observeSelectionMode() {
        viewModel.getSelectionMode().observe(getViewLifecycleOwner(), enabled -> {
            if (adapter != null) {
                adapter.setSelectionMode(Boolean.TRUE.equals(enabled));
            }
        });
    }

    @Override
    protected void setupAdapter() {
        adapter = new BudgetAdapter();
        
        adapter.setClickListener(budget -> {
            Log.d(TAG, "👆 Клик по бюджету: " + budget.getId());
            goToEdit(budget);
        });
        
        adapter.setLongClickListener(budget -> {
            Log.d(TAG, "👆 Длительный клик по бюджету: " + budget.getId());
            showDeleteConfirmationDialog(budget);
        });
        
        adapter.setSelectionListener(selectedCount -> {
            Log.d(TAG, "🔄 Выбрано бюджетов: " + selectedCount);
            // Уведомляем Activity о количестве выбранных элементов
            if (getActivity() instanceof BudgetActivity) {
                ((BudgetActivity) getActivity()).updateSelectionCount(selectedCount);
            }
        });
        
        recyclerView.setAdapter(adapter);
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