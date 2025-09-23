package com.sadengineer.budgetmaster;

import android.os.Bundle;
 
import com.sadengineer.budgetmaster.base.BaseContentActivity;
import com.sadengineer.budgetmaster.utils.LogManager;

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

        LogManager.d(TAG, "AuthorsActivity создана");
    }
} 