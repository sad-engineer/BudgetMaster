package com.sadengineer.budgetmaster.budget;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.viewpager2.widget.ViewPager2;
import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class BudgetActivity extends BaseNavigationActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private BudgetPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButton(R.id.back_button);

        // Обработчики кнопок бюджета (заглушки)
        ImageButton addBudgetButton = findViewById(R.id.add_budget_button_bottom);
        ImageButton deleteBudgetButton = findViewById(R.id.delete_budget_button_bottom);

        addBudgetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Реализовать добавление бюджета
                android.widget.Toast.makeText(BudgetActivity.this, "Добавить бюджет", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        deleteBudgetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Реализовать удаление бюджета
                android.widget.Toast.makeText(BudgetActivity.this, "Удалить бюджет", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        // Инициализация ViewPager2 и TabLayout
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
} 