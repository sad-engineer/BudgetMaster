package com.sadengineer.budgetmaster.income;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.sadengineer.budgetmaster.MainActivity;
import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.expense.ExpenseActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class IncomeActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private IncomePagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income);

        // Обработчики кнопок toolbar
        ImageButton menuButton = findViewById(R.id.menu_button);
        ImageButton incomeButton = findViewById(R.id.income_button);
        ImageButton expenseButton = findViewById(R.id.expense_button);

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IncomeActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        incomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Уже находимся на экране доходов
            }
        });
        expenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IncomeActivity.this, ExpenseActivity.class);
                startActivity(intent);
            }
        });

        // Инициализация ViewPager2 и TabLayout
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);

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