package com.sadengineer.budgetmaster.navigation.navigation_tree;

import android.content.Context;
import android.content.Intent;

import com.sadengineer.budgetmaster.utils.LogManager;

/**
 * Фабрика для создания Intent'ов навигации
 * Содержит методы для создания Intent'ов для перехода на узлы навигационного дерева
 * и для перехода к Activity по классу
 */
public class IntentFactory {
    
    private static final String TAG = "IntentFactory";
    
    /**
     * Создает Intent для перехода на узел
     * @param context контекст
     * @param node узел навигации
     * @param tabIndex индекс вкладки
     * @return Intent для запуска Activity
     */
    public static Intent createIntent(Context context, NavigationNode node, int tabIndex) {
        if (node == null || node.activityClass == null) {
            LogManager.w(TAG, "Не удалось создать Intent: узел или класс Activity равен null");
            return null;
        }
        
        Intent intent = new Intent(context, node.activityClass);
        
        if (node.hasTabs() && tabIndex >= 0 && tabIndex < node.tabCount) {
            intent.putExtra("selected_tab", tabIndex);
            LogManager.d(TAG, "Создан Intent для " + node.name + " с вкладкой " + tabIndex);
        } else {
            LogManager.d(TAG, "Создан Intent для " + node.name);
        }
        
        return intent;
    }
    
    /**
     * Создает Intent для перехода к Activity по классу
     * @param context контекст
     * @param activityClass класс Activity
     * @param tabIndex индекс вкладки
     * @return Intent для запуска Activity
     */
    public static Intent createIntent(Context context, Class<?> activityClass, int tabIndex) {
        if (activityClass == null) {
            LogManager.w(TAG, "Не удалось создать Intent: класс Activity равен null");
            return null;
        }
        
        Intent intent = new Intent(context, activityClass);
        
        if (tabIndex > 0) {
            intent.putExtra("selected_tab", tabIndex);
            LogManager.d(TAG, "Создан Intent для " + activityClass.getSimpleName() + " с вкладкой " + tabIndex);
        } else {
            LogManager.d(TAG, "Создан Intent для " + activityClass.getSimpleName());
        }
        
        return intent;
    }
}
