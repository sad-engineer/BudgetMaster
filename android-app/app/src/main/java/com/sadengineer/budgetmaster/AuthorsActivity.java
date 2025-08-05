package com.sadengineer.budgetmaster;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.widget.Toolbar;
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;

/**
 * Экран авторов
 */
public class AuthorsActivity extends BaseNavigationActivity {

    private static final String TAG = "AuthorsActivity";

    /**
     * Создает экран авторов
     * @param savedInstanceState - сохраненное состояние
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authors);

        // Инициализация тулбара
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButton(R.id.back_button);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "🚀" + TAG + " запущена");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "▶️" + TAG + " возобновлена");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "⏸️" + TAG + " приостановлена");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "" + TAG + " остановлена");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "" + TAG + " уничтожена");
    }
} 