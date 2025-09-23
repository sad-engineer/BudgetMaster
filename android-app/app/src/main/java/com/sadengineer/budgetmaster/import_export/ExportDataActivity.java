package com.sadengineer.budgetmaster.import_export;

import android.os.Bundle;
 
import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.base.BaseContentActivity;
import com.sadengineer.budgetmaster.utils.LogManager;

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
        // Устанавливаем заголовок
        setToolbarTitle(R.string.menu_export_data, R.dimen.toolbar_text);

        LogManager.d(TAG, "ExportDataActivity создана");
    }
} 