package com.sadengineer.budgetmaster.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;

import com.sadengineer.budgetmaster.R;

public class SettingsActivity extends BaseNavigationActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButton(R.id.back_button);

        // TODO: Добавить логику для отображения информации о backend.jar
        // Пока что просто показываем заглушку
    }
} 