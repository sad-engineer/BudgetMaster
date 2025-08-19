package com.sadengineer.budgetmaster.statistics;

import android.os.Bundle;
import android.util.Log;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.base.BaseContentActivity;

/**
 * Activity для отображения статистики
 */
public class StatisticsActivity extends BaseContentActivity {
    
    private static final String TAG = "StatisticsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButton(R.id.back_button);
        
        // Устанавливаем заголовок
        setToolbarTitle(R.string.menu_statistics, R.dimen.toolbar_text);

        Log.d(TAG, "StatisticsActivity создана");
    }
} 