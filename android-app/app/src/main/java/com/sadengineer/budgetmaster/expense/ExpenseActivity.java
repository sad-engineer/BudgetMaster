package com.sadengineer.budgetmaster.expense;

import java.util.List;
import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.base.BaseCardsActivity;
import com.sadengineer.budgetmaster.backend.entity.Operation;
import com.sadengineer.budgetmaster.backend.filters.OperationTypeFilter;
import com.sadengineer.budgetmaster.operations.OperationEditActivity;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Activity для отображения списка операций расходов
 */
public class ExpenseActivity extends BaseCardsActivity<Operation> {
    
    private static final String TAG = "ExpenseActivity";
    
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ExpenseSharedViewModel viewModel;

    /**
     * Метод вызывается при создании Activity
     * @param savedInstanceState - сохраненное состояние Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);
        // Инициализация навигации
        initializeNavigation();
        // Устанавливаем заголовок для режима создания
        setToolbarTitle(R.string.toolbar_title_expense, R.dimen.toolbar_text_expense);

        // Общая привязка кнопок и placeholder для индикатора
        // Используем резерв в тулбаре как placeholder индикатора
        setupCommonCardsUi(0, R.id.add_expense_button_bottom, R.id.delete_expense_button_bottom, R.id.toolbar_reserve);
        // Shared ViewModel для управления режимом выбора и мягким удалением
        viewModel = new ViewModelProvider(this).get(ExpenseSharedViewModel.class);
        // Привязываем ViewModel к базовой логике кнопок/индикатора (важно: после setupCommonCardsUi)
        bindSelectionViewModel(viewModel);

        // Настраиваем TabLayout и ViewPager2
        setupViewPager();
    }

    /**
     * Обработчик клика «Добавить».
     */
    @Override
    protected void onAddClicked() {
        // Запускаем окно создания операции расхода
        Intent intent = new Intent(ExpenseActivity.this, OperationEditActivity.class);
        intent.putExtra("operation_type", OperationTypeFilter.EXPENSE.getIndex());
        intent.putExtra("source_tab", viewPager != null ? viewPager.getCurrentItem() : 0);
        startActivity(intent);
    }

    /**
     * Обработчик клика «Удалить/Режим выбора».
     */
    @Override
    protected void onDeleteClicked() {
        // Поведение переключения режима выбора обрабатывается базовым классом через ViewModel
    }
    
    /**
     * Настраивает ViewPager2 и TabLayout
     */
    private void setupViewPager() {
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        ExpensePagerAdapter adapter = new ExpensePagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Получаем индекс вкладки из Intent (по умолчанию 0)
        int tabIndex = getIntent().getIntExtra("selected_tab", 0);
        viewPager.setCurrentItem(tabIndex, false);
        Log.d(TAG, "Открыта вкладка: " + tabIndex);
        
        // Массив названий вкладок
        String[] tabTitles = {
            getString(R.string.tab_all),
            getString(R.string.tab_days),
            getString(R.string.tab_categories),
            getString(R.string.tab_charts)
        };

        new TabLayoutMediator(tabLayout, viewPager,
            (tab, position) -> {
                if (position >= 0 && position < tabTitles.length) {
                    tab.setText(tabTitles[position]);
                }
            }
        ).attach();
    }
} 