package com.sadengineer.budgetmaster.income;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.viewpager2.widget.ViewPager2;
import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class IncomeActivity extends BaseNavigationActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private IncomePagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income);

        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButton(R.id.back_button);

        // Обработчики кнопок доходов (заглушки)
        ImageButton addIncomeButton = findViewById(R.id.add_income_button_bottom);
        ImageButton deleteIncomeButton = findViewById(R.id.delete_income_button_bottom);

        addIncomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Реализовать добавление дохода
                android.widget.Toast.makeText(IncomeActivity.this, "Добавить доход", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        deleteIncomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Реализовать удаление дохода
                android.widget.Toast.makeText(IncomeActivity.this, "Удалить доход", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        // Инициализация ViewPager2 и TabLayout
        viewPager = findViewById(R.id.income_view_pager);
        tabLayout = findViewById(R.id.income_tab_layout);

        // Создание адаптера для ViewPager2
        pagerAdapter = new IncomePagerAdapter(this);
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