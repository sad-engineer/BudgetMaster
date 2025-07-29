package com.sadengineer.budgetmaster.income;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;

public class AddIncomeActivity extends BaseNavigationActivity {
    
    private EditText dateEditText;
    private EditText amountEditText;
    private Spinner categorySpinner;
    private Spinner accountSpinner;
    private EditText commentEditText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_income);
        
        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButtonForAddIncome(R.id.back_button);
        
        // Инициализация элементов интерфейса
        initializeViews();
        
        // Настройка обработчиков кнопок
        setupButtonHandlers();
    }

    /**
     * Инициализация элементов интерфейса
     */
    private void initializeViews() {
        dateEditText = findViewById(R.id.income_date_edit_text);
        amountEditText = findViewById(R.id.income_amount_edit_text);
        categorySpinner = findViewById(R.id.income_category_spinner);
        accountSpinner = findViewById(R.id.income_account_spinner);
        commentEditText = findViewById(R.id.income_comment_edit_text);
    }
    
    /**
     * Настройка кнопки "назад" для возврата на экран доходов
     * @param backButtonId ID кнопки назад в toolbar
     */
    private void setupBackButtonForAddIncome(int backButtonId) {
        ImageButton backButton = findViewById(backButtonId);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                // Возвращаемся на экран доходов
                Intent intent = new Intent(this, IncomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            });
        }
    }
    
    /**
     * Настройка обработчиков кнопок
     */
    private void setupButtonHandlers() {
        // Кнопка "Сохранить"
        ImageButton saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Реализовать сохранение дохода
                Toast.makeText(AddIncomeActivity.this, "Сохранить доход", Toast.LENGTH_SHORT).show();
            }
        });
    }
} 