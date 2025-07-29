package com.sadengineer.budgetmaster.expense;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.viewpager2.widget.ViewPager2;
import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ExpenseActivity extends BaseNavigationActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ExpensePagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButton(R.id.back_button);

        // Обработчики кнопок расходов
        ImageButton addExpenseButton = findViewById(R.id.add_expense_button);
        ImageButton deleteExpenseButton = findViewById(R.id.delete_expense_button);

        addExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Открываем экран добавления расхода
                Intent intent = new Intent(ExpenseActivity.this, AddExpenseActivity.class);
                startActivity(intent);
            }
        });

        deleteExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Реализовать удаление расхода
                android.widget.Toast.makeText(ExpenseActivity.this, "Удалить расход", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        // Инициализация ViewPager2 и TabLayout
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);

        // Создание адаптера для ViewPager2
        pagerAdapter = new ExpensePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Связывание TabLayout с ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.tab_all);
                    break;
                case 1:
                    tab.setText(R.string.tab_days);
                    break;
                case 2:
                    tab.setText(R.string.tab_categories);
                    break;
                case 3:
                    tab.setText(R.string.tab_charts);
                    break;
            }
        }).attach();
    }
} 