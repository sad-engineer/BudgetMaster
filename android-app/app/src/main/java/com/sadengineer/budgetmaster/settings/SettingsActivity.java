package com.sadengineer.budgetmaster.settings;


import android.os.Bundle;
import android.util.Log;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.base.BaseContentActivity;

public class SettingsActivity extends BaseContentActivity {

    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButton(R.id.back_button);
        
        // Устанавливаем заголовок
        setToolbarTitle(R.string.menu_settings, R.dimen.toolbar_text);

        Log.d(TAG, "SettingsActivity создана");
    }

} 