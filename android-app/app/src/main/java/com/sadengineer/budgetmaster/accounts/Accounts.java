package com.sadengineer.budgetmaster.accounts;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.sadengineer.budgetmaster.MainActivity;
import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.income.IncomeActivity;
import com.sadengineer.budgetmaster.expense.ExpenseActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import androidx.viewpager2.widget.ViewPager2;

public class Accounts extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_accounts);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Обработчики кнопок toolbar
        ImageButton backButton = findViewById(R.id.back_button);
        ImageButton menuButton = findViewById(R.id.menu_button);
        ImageButton incomeButton = findViewById(R.id.income_button);
        ImageButton expenseButton = findViewById(R.id.expense_button);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Возвращаемся на главный экран
                Intent intent = new Intent(Accounts.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Открываем меню (если есть drawer layout) или идем на главный экран
                Intent intent = new Intent(Accounts.this, MainActivity.class);
                startActivity(intent);
            }
        });
        incomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Accounts.this, IncomeActivity.class);
                startActivity(intent);
            }
        });
        expenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Accounts.this, ExpenseActivity.class);
                startActivity(intent);
            }
        });

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager2 viewPager = findViewById(R.id.view_pager);

        AccountsPagerAdapter adapter = new AccountsPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Получаем индекс вкладки из Intent (по умолчанию 0)
        int tabIndex = getIntent().getIntExtra("tab_index", 0);
        viewPager.setCurrentItem(tabIndex, false);

        new TabLayoutMediator(tabLayout, viewPager,
            (tab, position) -> {
                switch (position) {
                    case 0: tab.setText(getString(R.string.tab_current)); break;
                    case 1: tab.setText(getString(R.string.tab_savings)); break;
                    case 2: tab.setText(getString(R.string.tab_transfers)); break;
                }
            }
        ).attach();
    }
}