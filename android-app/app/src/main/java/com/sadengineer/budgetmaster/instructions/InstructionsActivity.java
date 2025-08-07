package com.sadengineer.budgetmaster.instructions;

import android.os.Bundle;
import android.util.Log;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;

/**
 * Activity для отображения инструкций
 */
public class InstructionsActivity extends BaseNavigationActivity {
    
    private static final String TAG = "InstructionsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);

        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButton(R.id.back_button);

        Log.d(TAG, "✅ InstructionsActivity создана");
    }
} 