package com.sadengineer.budgetmaster;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.widget.Toolbar;
import com.sadengineer.budgetmaster.base.BaseContentActivity;

/**
 * Экран авторов
 */
public class AuthorsActivity extends BaseContentActivity {

    private static final String TAG = "AuthorsActivity";

    /**
     * Создает экран авторов
     * @param savedInstanceState - сохраненное состояние
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authors);
        // Инициализация навигации
        initializeNavigation();
        // Устанавливаем заголовок
        setToolbarTitle(R.string.toolbar_title_authors, R.dimen.toolbar_text);

        Log.d(TAG, "AuthorsActivity создана");
    }
} 