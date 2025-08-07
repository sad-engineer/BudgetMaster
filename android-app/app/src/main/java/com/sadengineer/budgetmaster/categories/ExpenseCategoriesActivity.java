package com.sadengineer.budgetmaster.categories;

import android.os.Bundle;
import android.util.Log;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;

/**
 * Activity для отображения категорий расходов
 */
public class ExpenseCategoriesActivity extends BaseNavigationActivity {
    
    private static final String TAG = "ExpenseCategoriesActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_categories);

        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButton(R.id.back_button);

        Log.d(TAG, "✅ ExpenseCategoriesActivity создана");
    }
} 