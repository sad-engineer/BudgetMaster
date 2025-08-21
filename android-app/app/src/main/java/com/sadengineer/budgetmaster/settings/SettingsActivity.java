package com.sadengineer.budgetmaster.settings;


import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.base.BaseContentActivity;

public class SettingsActivity extends BaseContentActivity {

    private static final String TAG = "SettingsActivity";

    private AppSettings appSettings;
    private CheckBox checkboxShowPosition;
    private CheckBox checkboxShowId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Инициализация настроек
        appSettings = new AppSettings(this);

        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButton(R.id.back_button);
        
        // Устанавливаем заголовок
        setToolbarTitle(R.string.menu_settings, R.dimen.toolbar_text);

        // Инициализация UI элементов
        initializeViews();
        
        // Загружаем текущие настройки
        loadSettings();

        Log.d(TAG, "SettingsActivity создана");
    }

    /**
     * Инициализирует UI элементы
     */
    private void initializeViews() {
        checkboxShowPosition = findViewById(R.id.checkbox_show_position);
        checkboxShowId = findViewById(R.id.checkbox_show_id);

        // Настраиваем обработчики событий
        checkboxShowPosition.setOnCheckedChangeListener((buttonView, isChecked) -> {
            appSettings.setShowPosition(isChecked);
            Log.d(TAG, "Настройка show_position изменена на: " + isChecked);
        });

        checkboxShowId.setOnCheckedChangeListener((buttonView, isChecked) -> {
            appSettings.setShowId(isChecked);
            Log.d(TAG, "Настройка show_id изменена на: " + isChecked);
        });
    }

    /**
     * Загружает текущие настройки в UI
     */
    private void loadSettings() {
        boolean showPosition = appSettings.isShowPosition();
        boolean showId = appSettings.isShowId();

        checkboxShowPosition.setChecked(showPosition);
        checkboxShowId.setChecked(showId);

        Log.d(TAG, "Загружены настройки: show_position=" + showPosition + ", show_id=" + showId);
    }

} 