package com.sadengineer.budgetmaster.currencies;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.sadengineer.budgetmaster.MainActivity;
import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.income.IncomeActivity;
import com.sadengineer.budgetmaster.expense.ExpenseActivity;

public class CurrenciesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currencies);

        // Обработчики кнопок toolbar
        ImageButton backButton = findViewById(R.id.back_button);
        ImageButton menuButton = findViewById(R.id.menu_button);
        ImageButton incomeButton = findViewById(R.id.income_button);
        ImageButton expenseButton = findViewById(R.id.expense_button);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Возвращаемся на главный экран
                Intent intent = new Intent(CurrenciesActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Открываем меню (если есть drawer layout) или идем на главный экран
                Intent intent = new Intent(CurrenciesActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        incomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CurrenciesActivity.this, IncomeActivity.class);
                startActivity(intent);
            }
        });
        expenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CurrenciesActivity.this, ExpenseActivity.class);
                startActivity(intent);
            }
        });

        // Обработчики кнопок валют
        ImageButton addCurrencyButton = findViewById(R.id.add_currency_button);
        ImageButton deleteCurrencyButton = findViewById(R.id.delete_currency_button);

        addCurrencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Реализовать добавление валюты
                Toast.makeText(CurrenciesActivity.this, "Добавить валюту", Toast.LENGTH_SHORT).show();
            }
        });

        deleteCurrencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Реализовать удаление валюты
                Toast.makeText(CurrenciesActivity.this, "Удалить валюту", Toast.LENGTH_SHORT).show();
            }
        });
    }
} 