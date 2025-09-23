package com.sadengineer.budgetmaster.instructions;

import android.os.Bundle;
 
import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.base.BaseContentActivity;
import com.sadengineer.budgetmaster.utils.LogManager;

/**
 * Activity для отображения инструкций
 */
public class InstructionsActivity extends BaseContentActivity {
    
    private static final String TAG = "InstructionsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);
        // Инициализация навигации
        initializeNavigation();
        // Устанавливаем заголовок
        setToolbarTitle(R.string.menu_instructions, R.dimen.toolbar_text);
        LogManager.d(TAG, "InstructionsActivity создана");
    }
} 