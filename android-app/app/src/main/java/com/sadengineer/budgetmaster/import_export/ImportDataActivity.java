package com.sadengineer.budgetmaster.import_export;

import android.os.Bundle;
 
import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.base.BaseContentActivity;
import com.sadengineer.budgetmaster.utils.LogManager;

/**
 * Activity для импорта данных
 */
public class ImportDataActivity extends BaseContentActivity {
    
    private static final String TAG = "ImportDataActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_data);

        // Инициализация навигации
        initializeNavigation();
        // Устанавливаем заголовок
        setToolbarTitle(R.string.menu_import_data, R.dimen.toolbar_text);

        LogManager.d(TAG, "ImportDataActivity создана");
    }
} 