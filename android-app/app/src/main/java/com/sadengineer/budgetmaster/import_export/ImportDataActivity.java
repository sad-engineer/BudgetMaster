package com.sadengineer.budgetmaster.import_export;

import android.os.Bundle;
import android.util.Log;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.base.BaseContentActivity;

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
        setupMenuButton(R.id.menu_button);
        setupBackButton(R.id.back_button);
        
        // Устанавливаем заголовок
        setToolbarTitle(R.string.menu_import_data, R.dimen.toolbar_text);

        Log.d(TAG, "ImportDataActivity создана");
    }
} 