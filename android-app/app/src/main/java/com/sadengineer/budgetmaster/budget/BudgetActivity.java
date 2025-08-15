package com.sadengineer.budgetmaster.budget;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.viewpager2.widget.ViewPager2;
import androidx.lifecycle.ViewModelProvider;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.base.BaseCardsActivity;
import com.sadengineer.budgetmaster.backend.entity.Budget;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;
import java.util.ArrayList;

public class BudgetActivity extends BaseCardsActivity<Budget> {

    private static final String TAG = "BudgetActivity";
    
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private BudgetPagerAdapter pagerAdapter;
    private BudgetSharedViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButton(R.id.back_button);

        // Устанавливаем заголовок
        setToolbarTitle(R.string.toolbar_title_budgets, R.dimen.toolbar_text_budgets);

        // Общая привязка кнопок и placeholder для индикатора
        setupCommonCardsUi(0, R.id.add_budget_button_bottom, R.id.delete_budget_button_bottom, null);

        // Shared ViewModel для управления режимом выбора и мягким удалением
        viewModel = new ViewModelProvider(this).get(BudgetSharedViewModel.class);
        
        // Привязываем ViewModel к базовой логике кнопок/индикатора
        bindSelectionViewModel(viewModel);

        // Инициализация ViewPager2 и TabLayout
        setupViewPager();
    }
    
    /**
     * Настраивает ViewPager2 и TabLayout
     */
    private void setupViewPager() {
        viewPager = findViewById(R.id.budget_view_pager);
        tabLayout = findViewById(R.id.budget_tab_layout);

        // Создание адаптера для ViewPager2
        pagerAdapter = new BudgetPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Связывание TabLayout с ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.tab_limits);
                    break;
                case 1:
                    tab.setText(R.string.tab_remaining);
                    break;
            }
        }).attach();
    }
    
    /**
     * Получает текущий фрагмент
     */
    private BudgetLimitsFragment getCurrentFragment() {
        if (viewPager.getCurrentItem() == 0) {
            return (BudgetLimitsFragment) pagerAdapter.getFragment(0);
        }
        return null;
    }
    
    /**
     * Обновляет количество выбранных элементов
     */
    public void updateSelectionCount(int count) {
        Log.d(TAG, "🔄 Выбрано элементов: " + count);
        // Можно добавить отображение количества в UI
    }

    /**
     * Обработчик клика «Добавить». Реализация для BaseCardsActivity.
     */
    @Override
    protected void onAddClicked() {
        Log.d(TAG, "➕ Нажата кнопка 'Добавить бюджет'");
        // TODO: Реализовать добавление бюджета
        Intent intent = new Intent(BudgetActivity.this, BudgetEditActivity.class);
        startActivity(intent);
    }

    /**
     * Обработчик клика «Удалить/Режим выбора». Реализация для BaseCardsActivity.
     */
    @Override
    protected void onDeleteClicked() {
        Log.d(TAG, "🗑️ Нажата кнопка 'Удалить/Режим выбора'");
        toggleSelectionMode();
    }

    /**
     * Переопределяем для обработки удаления выбранных бюджетов
     */
    @Override
    protected void onSelectionModeChanged(boolean enabled) {
        if (enabled) {
            Log.d(TAG, "✅ Режим выбора бюджетов включен");
        } else {
            Log.d(TAG, "❌ Режим выбора бюджетов отменен");
        }
    }
} 