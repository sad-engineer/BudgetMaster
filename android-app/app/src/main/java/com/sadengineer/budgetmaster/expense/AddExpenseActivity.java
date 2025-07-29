package com.sadengineer.budgetmaster.expense;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;

public class AddExpenseActivity extends BaseNavigationActivity {
    
    private EditText dateEditText;
    private EditText amountEditText;
    private Spinner categorySpinner;
    private Spinner accountSpinner;
    private EditText commentEditText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);
        
        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButtonForAddExpense(R.id.back_button);
        
        // Инициализация элементов интерфейса
        initializeViews();
        
        // Настройка обработчиков кнопок
        setupButtonHandlers();
    }

    /**
     * Инициализация элементов интерфейса
     */
    private void initializeViews() {
        dateEditText = findViewById(R.id.expense_date_edit_text);
        amountEditText = findViewById(R.id.expense_amount_edit_text);
        categorySpinner = findViewById(R.id.expense_category_spinner);
        accountSpinner = findViewById(R.id.expense_account_spinner);
        commentEditText = findViewById(R.id.expense_comment_edit_text);
    }
    
    /**
     * Настройка кнопки "назад" для возврата на экран расходов
     * @param backButtonId ID кнопки назад в toolbar
     */
    private void setupBackButtonForAddExpense(int backButtonId) {
        ImageButton backButton = findViewById(backButtonId);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                // Возвращаемся на экран расходов
                Intent intent = new Intent(this, ExpenseActivity.class);
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
                // TODO: Реализовать сохранение расхода
                Toast.makeText(AddExpenseActivity.this, "Сохранить расход", Toast.LENGTH_SHORT).show();
            }
        });
    }
} 