package com.sadengineer.budgetmaster.budget;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.viewpager2.widget.ViewPager2;
import androidx.lifecycle.ViewModelProvider;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.base.BaseContentActivity;
import com.sadengineer.budgetmaster.backend.entity.Budget;
import com.sadengineer.budgetmaster.budget.BudgetAdapter;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;
import java.util.ArrayList;

public class BudgetActivity extends BaseContentActivity {

    private static final String TAG = "BudgetActivity";
    
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private BudgetPagerAdapter pagerAdapter;
    private ImageButton addBudgetButton;
    private ImageButton deleteBudgetButton;
    private boolean isSelectionMode = false;
    private BudgetSharedViewModel viewModel;
    private BudgetAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButton(R.id.back_button);
        
        // Устанавливаем заголовок
        setToolbarTitle(R.string.toolbar_title_budgets, R.dimen.toolbar_text);

        // Shared ViewModel для управления режимом выбора и мягким удалением
        viewModel = new ViewModelProvider(this).get(BudgetSharedViewModel.class);

        // Обработчики кнопок бюджета
        setupButtons();

        // Кнопки скрыты, поэтому режим выбора не нужен
        Log.d(TAG, "Режим выбора отключен - кнопки скрыты");

        // Инициализация ViewPager2 и TabLayout
        setupViewPager();
    }
    
    /**
     * Настраивает кнопки
     */
    private void setupButtons() {
        addBudgetButton = findViewById(R.id.add_budget_button_bottom);
        deleteBudgetButton = findViewById(R.id.delete_budget_button_bottom);

        // Скрываем кнопки, так как бюджеты создаются только через категории
        addBudgetButton.setVisibility(View.GONE);
        deleteBudgetButton.setVisibility(View.GONE);
        
        Log.d(TAG, "Кнопки создания/удаления бюджетов скрыты - бюджеты управляются через категории");
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
    
    // Метод updateSelectionCount удален, так как режим выбора отключен
} 