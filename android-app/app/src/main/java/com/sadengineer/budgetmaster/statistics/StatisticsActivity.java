package com.sadengineer.budgetmaster.statistics;

import android.os.Bundle;
import android.util.Log;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;

/**
 * Activity для отображения статистики
 */
public class StatisticsActivity extends BaseNavigationActivity {
    
    private static final String TAG = "StatisticsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButton(R.id.back_button);

        Log.d(TAG, "✅ StatisticsActivity создана");
    }
} 