package com.sadengineer.budgetmaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.util.Log;
import androidx.appcompat.widget.Toolbar;
import com.sadengineer.budgetmaster.base.BaseContentActivity;

/**
 * Экран версии приложения
 */
public class VersionActivity extends BaseContentActivity {

    /**
     * Тег для логирования
     */
    private static final String TAG = "VersionActivity";

    /**
     * Создает экран версии
     * @param savedInstanceState - сохраненное состояние
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_version);

        Log.d(TAG, "🚀 VersionActivity создана");

        // Настройка Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButton(R.id.back_button);
        
        // Устанавливаем заголовок
        setToolbarTitle(R.string.toolbar_title_version, R.dimen.toolbar_text);

        // Получаем ссылки на TextView
        TextView frontendVersionText = findViewById(R.id.frontend_version_text);
        TextView backendVersionText = findViewById(R.id.backend_version_text);

        getFrontendVersion(frontendVersionText);
        getBackendVersion(backendVersionText);
    }

    /**
     * Получает версию фронтенда
     * @param frontendVersionText - поле ввода версии фронтенда
     * @return версия фронтенда
     */
    public String getFrontendVersion(TextView frontendVersionText) {
        // Устанавливаем версию фронтенда из BuildConfig
        try {
            String frontendVersion = BuildConfig.APP_VERSION;
            Log.d(TAG, "✅ Найдена версия фронтенда: " + frontendVersion);
            frontendVersionText.setText(frontendVersion);
            return frontendVersion;
        } catch (Exception e) {
            String errorVersion = "Неизвестно";
            frontendVersionText.setText(errorVersion);
            Log.e(TAG, "❌ Ошибка при получении версии фронтенда: " + e.getMessage(), e);
            return errorVersion;
        }
    }

    /**
     * Получает версию бекенда
     * @param backendVersionText - поле ввода версии бекенда
     * @return версия бекенда
     */
    public String getBackendVersion(TextView backendVersionText) {
        // Устанавливаем версию бекенда из BuildConfig
        try {
            String backendVersion = BuildConfig.BACKEND_VERSION;
            Log.d(TAG, "✅ Найдена версия бекенда: " + backendVersion);
            backendVersionText.setText(backendVersion);
            return backendVersion;
        } catch (Exception e) {
            String errorVersion = "Неизвестно";
            backendVersionText.setText(errorVersion);
            Log.e(TAG, "❌ Ошибка при получении версии бекенда: " + e.getMessage(), e);
            return errorVersion;
        }
    }
} 