package com.sadengineer.budgetmaster.settings;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.base.BaseContentActivity;

public class SettingsActivity extends BaseContentActivity {

    private static final String TAG = "SettingsActivity";

    private AppSettings appSettings;
    private LinearLayout checkboxShowPositionContainer;
    private LinearLayout checkboxShowIdContainer;
    private ImageView checkboxShowPositionIcon;
    private ImageView checkboxShowIdIcon;
    private boolean showPositionChecked = false;
    private boolean showIdChecked = false;

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
        checkboxShowPositionContainer = findViewById(R.id.checkbox_show_position_container);
        checkboxShowIdContainer = findViewById(R.id.checkbox_show_id_container);
        checkboxShowPositionIcon = findViewById(R.id.checkbox_show_position_icon);
        checkboxShowIdIcon = findViewById(R.id.checkbox_show_id_icon);

        // Настраиваем обработчики событий для чекбокса позиции
        checkboxShowPositionContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPositionChecked = !showPositionChecked;
                updateCheckboxIcon(checkboxShowPositionIcon, showPositionChecked);
                appSettings.setShowPosition(showPositionChecked);
                Log.d(TAG, "Настройка show_position изменена на: " + showPositionChecked);
            }
        });

        // Настраиваем обработчики событий для чекбокса ID
        checkboxShowIdContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showIdChecked = !showIdChecked;
                updateCheckboxIcon(checkboxShowIdIcon, showIdChecked);
                appSettings.setShowId(showIdChecked);
                Log.d(TAG, "Настройка show_id изменена на: " + showIdChecked);
            }
        });
    }
    
    /**
     * Обновляет иконку чекбокса в зависимости от состояния
     */
    private void updateCheckboxIcon(ImageView icon, boolean isChecked) {
        if (isChecked) {
            icon.setImageResource(R.drawable.ic_checkbox_checked);
        } else {
            icon.setImageResource(R.drawable.ic_checkbox_unchecked);
        }
    }

    /**
     * Загружает текущие настройки в UI
     */
    private void loadSettings() {
        showPositionChecked = appSettings.isShowPosition();
        showIdChecked = appSettings.isShowId();

        updateCheckboxIcon(checkboxShowPositionIcon, showPositionChecked);
        updateCheckboxIcon(checkboxShowIdIcon, showIdChecked);

        Log.d(TAG, "Загружены настройки: show_position=" + showPositionChecked + ", show_id=" + showIdChecked);
    }

} 