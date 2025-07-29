package com.sadengineer.budgetmaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;

// BackendVersion больше не используется - используем Room ORM

public class VersionActivity extends BaseNavigationActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_version);

        // Настройка Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButton(R.id.back_button);

        // Получаем ссылки на TextView
        TextView frontendVersionText = findViewById(R.id.frontend_version_text);
        TextView backendVersionText = findViewById(R.id.backend_version_text);

        // Устанавливаем версию фронтенда из BuildConfig
        try {
            String frontendVersion = BuildConfig.APP_VERSION;
            frontendVersionText.setText(frontendVersion);
        } catch (Exception e) {
            frontendVersionText.setText("Неизвестно");
        }

        // Устанавливаем версию бекенда из BuildConfig
        try {
            String backendVersion = BuildConfig.BACKEND_VERSION;
            backendVersionText.setText(backendVersion);
        } catch (Exception e) {
            backendVersionText.setText("Неизвестно");
        }
    }
} 