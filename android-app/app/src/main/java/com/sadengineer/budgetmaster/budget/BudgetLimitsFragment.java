package com.sadengineer.budgetmaster.budget;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.AlertDialog;
import android.content.DialogInterface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.backend.database.BudgetMasterDatabase;
import com.sadengineer.budgetmaster.backend.entity.Budget;
import com.sadengineer.budgetmaster.backend.entity.Category;
import com.sadengineer.budgetmaster.backend.entity.Currency;

import com.sadengineer.budgetmaster.backend.service.BudgetService;
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;

import java.util.List;
import java.util.ArrayList;

public class BudgetLimitsFragment extends Fragment {
    private static final String TAG = "BudgetLimitsFragment";
    private RecyclerView recyclerView;
    private BudgetAdapter adapter;
    private BudgetSharedViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget_limits, container, false);
        
        // Настраиваем RecyclerView
        recyclerView = view.findViewById(R.id.budget_limits_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Shared ViewModel из Activity
        viewModel = new ViewModelProvider(requireActivity()).get(BudgetSharedViewModel.class);

        // Создаем адаптер
        setupAdapter();

        // Наблюдаем за режимом выбора из Shared ViewModel
        viewModel.getSelectionMode().observe(getViewLifecycleOwner(), enabled -> {
            if (adapter != null) {
                adapter.setSelectionMode(Boolean.TRUE.equals(enabled));
            }
        });
        
        // Загружаем бюджеты
        loadBudgets();
        
        return view;
    }
    
    /**
     * Загружает бюджеты
     */
    private void loadBudgets() {
        Log.d(TAG, "🔄 Загружаем бюджеты...");
        
        try {
            BudgetMasterDatabase database = BudgetMasterDatabase.getDatabase(requireContext());
            
            // Загружаем бюджеты
            database.budgetDao().getAll().observe(getViewLifecycleOwner(), budgets -> {
                Log.d(TAG, "✅ Загружено бюджетов: " + (budgets != null ? budgets.size() : 0));
                
                if (budgets != null && !budgets.isEmpty()) {
                    adapter.setBudgets(budgets);
                    Log.d(TAG, "✅ Бюджеты отображены в списке");
                    
                    // Сбрасываем счетчик свайпов при изменении содержимого списка
                    if (getActivity() instanceof BaseNavigationActivity) {
                        ((BaseNavigationActivity) getActivity()).resetSwipeCount();
                    }
                } else {
                    Log.w(TAG, "⚠️ Бюджеты не найдены в базе данных");
                }
            });
            
            // Загружаем категории для отображения названий
            database.categoryDao().getAll().observe(getViewLifecycleOwner(), categories -> {
                Log.d(TAG, "✅ Загружено категорий: " + (categories != null ? categories.size() : 0));
                if (categories != null) {
                    adapter.setCategories(categories);
                }
            });
            
            // Загружаем валюты для отображения названий
            database.currencyDao().getAll().observe(getViewLifecycleOwner(), currencies -> {
                Log.d(TAG, "✅ Загружено валют: " + (currencies != null ? currencies.size() : 0));
                if (currencies != null) {
                    adapter.setCurrencies(currencies);
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Ошибка загрузки бюджетов: " + e.getMessage(), e);
        }
    }
    
    /**
     * Устанавливает режим выбора
     */
    public void setSelectionMode(boolean enabled) {
        if (adapter != null) {
            adapter.setSelectionMode(enabled);
        }
    }
    
    /**
     * Настраивает адаптер
     */
    private void setupAdapter() {
        adapter = new BudgetAdapter();
        recyclerView.setAdapter(adapter);
        
        // Устанавливаем слушатели
        adapter.setClickListener(budget -> {
            Log.d(TAG, "👆 Клик по бюджету: " + budget.getId());
            openBudgetEdit(budget);
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
    }
    
    /**
     * Показывает диалог подтверждения удаления
     */
    private void showDeleteConfirmationDialog(Budget budget) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Удаление бюджета")
            .setMessage("Вы уверены, что хотите полностью удалить этот бюджет? Это действие нельзя отменить.")
            .setPositiveButton("Удалить", (dialog, which) -> {
                deleteBudget(budget);
            })
            .setNegativeButton("Отмена", null)
            .show();
    }
    
    /**
     * Удаляет бюджет
     */
    private void deleteBudget(Budget budget) {
        try {
            BudgetMasterDatabase database = BudgetMasterDatabase.getDatabase(requireContext());
            BudgetService budgetService = new BudgetService(requireContext(), "system");
            
            budgetService.delete(false, budget); // false = hard delete
            Log.d(TAG, "✅ Бюджет удален: " + budget.getId());
        } catch (Exception e) {
            Log.e(TAG, "❌ Ошибка при удалении бюджета: " + e.getMessage(), e);
        }
    }
    
    /**
     * Возвращает выбранные бюджеты
     */
    public List<Budget> getSelectedBudgets() {
        if (adapter != null) {
            return adapter.getSelectedBudgets();
        }
        return new ArrayList<>();
    }
    
    /**
     * Открывает окно редактирования бюджета
     */
    private void openBudgetEdit(Budget budget) {
        Intent intent = new Intent(requireContext(), BudgetEditActivity.class);
        intent.putExtra("budget_id", budget.getId());
        startActivity(intent);
        Log.d(TAG, "🔄 Открываем редактирование бюджета ID: " + budget.getId());
    }
} 