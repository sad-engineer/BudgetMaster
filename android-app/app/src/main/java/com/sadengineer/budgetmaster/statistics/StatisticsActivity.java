package com.sadengineer.budgetmaster.statistics;

import android.os.Bundle;
 
import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.base.BaseContentActivity;
import com.sadengineer.budgetmaster.utils.LogManager;

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
        // Устанавливаем заголовок
        setToolbarTitle(R.string.menu_statistics, R.dimen.toolbar_text);

        LogManager.d(TAG, "StatisticsActivity создана");
    }
} 