package com.sadengineer.budgetmaster.accounts;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;

public class AddAccountActivity extends BaseNavigationActivity {
    
    private EditText nameEditText;
    private EditText balanceEditText;
    private Spinner currencySpinner;
    private Spinner typeSpinner;
    private CheckBox closedCheckBox;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);
        
        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButtonForAddAccount(R.id.back_button);
        
        // Инициализация элементов интерфейса
        initializeViews();
        
        // Настройка обработчиков кнопок
        setupButtonHandlers();
    }

    /**
     * Инициализация элементов интерфейса
     */
    private void initializeViews() {
        nameEditText = findViewById(R.id.account_name_edit_text);
        balanceEditText = findViewById(R.id.account_balance_edit_text);
        currencySpinner = findViewById(R.id.account_currency_spinner);
        typeSpinner = findViewById(R.id.account_type_spinner);
        closedCheckBox = findViewById(R.id.account_closed_checkbox);
    }
    
    /**
     * Настройка кнопки "назад" для возврата на экран счетов
     * @param backButtonId ID кнопки назад в toolbar
     */
    private void setupBackButtonForAddAccount(int backButtonId) {
        ImageButton backButton = findViewById(backButtonId);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                // Возвращаемся на экран счетов
                Intent intent = new Intent(this, Accounts.class);
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
                // TODO: Реализовать сохранение счета
                Toast.makeText(AddAccountActivity.this, "Сохранить счет", Toast.LENGTH_SHORT).show();
            }
        });
    }
} 