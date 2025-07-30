package com.sadengineer.budgetmaster.income;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;

public class AddIncomeCategoryActivity extends BaseNavigationActivity {
    
    private EditText nameEditText;
    private Spinner typeSpinner;
    private Spinner parentCategorySpinner;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);
        
        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButtonForAddIncomeCategory(R.id.back_button);
                
        // Настройка обработчиков кнопок
        setupButtonHandlers();
    }
    
    /**
     * Настройка кнопки "назад" для возврата на экран доходов
     * @param backButtonId ID кнопки назад в toolbar
     */
    private void setupBackButtonForAddIncomeCategory(int backButtonId) {
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
                // TODO: Реализовать сохранение категории дохода
                Toast.makeText(AddIncomeCategoryActivity.this, "Сохранить категорию дохода", Toast.LENGTH_SHORT).show();
            }
        });
    }
} 