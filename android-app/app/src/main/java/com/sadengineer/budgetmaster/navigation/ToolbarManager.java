package com.sadengineer.budgetmaster.navigation;

import android.app.Activity;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.sadengineer.budgetmaster.R;

/**
 * Менеджер для управления тулбаром
 * Отвечает за настройку кнопок и обработку их кликов
 */
public class ToolbarManager {
    
    private static final String TAG = "ToolbarManager";
    
    private final Activity activity;
    private final DrawerLayout drawerLayout;
    private NavigationController navigationController;
    
    public ToolbarManager(Activity activity, DrawerLayout drawerLayout) {
        this.activity = activity;
        this.drawerLayout = drawerLayout;
    }
    
    /**
     * Установка связи с NavigationController
     */
    public void setNavigationController(NavigationController navigationController) {
        this.navigationController = navigationController;
    }

    /**
     * Комплексная инициализация стандартного тулбара
     */
    public void setupStandardToolbar() {
        setupMenuButton();
        setupBackButton();
        setupPositionChangeButton();
    }

    /**
     * Комплексная инициализация тулбара для главного экрана
     */
    public void setupMainToolbar() {
        setupMenuButton();
        setupPositionChangeButton();
    }
    
    /**
     * Настройка кнопки меню для открытия бокового меню
     */
    public void setupMenuButton() {
        ImageButton menuButton = activity.findViewById(R.id.menu_button);
        if (menuButton != null) {
            menuButton.setOnClickListener(v -> {
                if (drawerLayout != null) {
                    drawerLayout.openDrawer(GravityCompat.START);
                    Log.d(TAG, "Нажата кнопка меню");
                }
            });
        }
    }
    
    /**
     * Настройка кнопки "назад" для возврата на главный экран
     */
    public void setupBackButton() {
        ImageButton backButton = activity.findViewById(R.id.back_button);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                if (navigationController != null) {
                    navigationController.goToRoot();
                    Log.d(TAG, "Нажата кнопка назад");
                }
            });
        }
    }
    
    /**
     * Настройка кнопки смены позиции
     */
    public void setupPositionChangeButton() {
        ImageButton positionButton = activity.findViewById(R.id.position_change_button);
        if (positionButton != null) {
            positionButton.setOnClickListener(v -> Log.d(TAG, "Нажата кнопка смены позиции"));
        }
    }   
    
    /**
     * Устанавливает заголовок тулбара
     * @param titleResId - ресурс строки для заголовка
     * @param textSizeResId - ресурс размера шрифта
     */
    protected void setToolbarTitle(int titleResId, int textSizeResId) {
        TextView toolbarTitle = activity.findViewById(R.id.toolbar_title);
        if (toolbarTitle != null) {
            toolbarTitle.setText(titleResId);
            Log.d(TAG, "Заголовок тулбара установлен");
            
            // Устанавливаем размер шрифта
            float textSize = activity.getResources().getDimension(textSizeResId);
            toolbarTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            Log.d(TAG, "Размер шрифта установлен");
        }
    }

    /**
     * Устанавливает заголовок тулбара
     * @param titleText - текст для заголовка
     * @param textSize - размер шрифта
     */
    protected void setToolbarTitle(String titleText, float textSize) {
        TextView toolbarTitle = activity.findViewById(R.id.toolbar_title);
        if (toolbarTitle != null) {
            toolbarTitle.setText(titleText);
            Log.d(TAG, "Заголовок тулбара установлен");

            toolbarTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            Log.d(TAG, "Размер шрифта установлен");
        }
    }
}
