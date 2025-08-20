package com.sadengineer.budgetmaster.import_export;

import android.os.Bundle;
import android.util.Log;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.base.BaseContentActivity;

/**
 * Activity для экспорта данных
 */
public class ExportDataActivity extends BaseContentActivity {
    
    private static final String TAG = "ExportDataActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_data);

        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButton(R.id.back_button);
        
        // Устанавливаем заголовок
        setToolbarTitle(R.string.menu_export_data, R.dimen.toolbar_text);

        Log.d(TAG, "ExportDataActivity создана");
    }
} 