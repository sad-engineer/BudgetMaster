package com.sadengineer.budgetmaster.navigation;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
 
import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import com.sadengineer.budgetmaster.navigation.navigation_tree.NavigationTree;
import com.sadengineer.budgetmaster.navigation.navigation_tree.NavigationNode;
import com.sadengineer.budgetmaster.utils.LogManager;

/**
 * Обработчик навигационного меню
 * Отвечает за обработку выбора пунктов меню и переходы между экранами
 */
public class MenuHandler implements NavigationView.OnNavigationItemSelectedListener {
    
    private static final String TAG = "MenuHandler";
    
    private final Context context;
    private final DrawerLayout drawerLayout;
    
    public MenuHandler(Context context, DrawerLayout drawerLayout) {
        this.context = context;
        this.drawerLayout = drawerLayout;
    }
    
    /**
     * Обработка выбора пунктов меню
     * @param item - выбранный пункт меню
     * @return true, если обработка выполнена успешно
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // Используем NavigationTree для обработки выбора пунктов меню
        boolean handled = handleNavigationItemSelection(id);

        // Закрываем боковое меню
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        return handled;
    }
    
    /**
     * Обрабатывает выбор пункта меню через NavigationTree
     */
    private boolean handleNavigationItemSelection(int menuItemId) {
        // Автоматически получаем класс Activity по ID пункта меню
        Class<?> targetActivityClass = MenuBuilder.getActivityForMenuId(menuItemId);
        
        if (targetActivityClass != null) {
            // Получаем узел навигации
            NavigationNode targetNode = NavigationTree.getNode(targetActivityClass);
            
            if (targetNode != null) {
                Intent intent = NavigationTree.createIntent(context, targetNode, 0);
                if (intent != null) {
                    context.startActivity(intent);
                    LogManager.d(TAG, "Переход к " + targetNode.name);
                    return true;
                }
            } else {
                LogManager.w(TAG, "Узел навигации не найден для " + targetActivityClass.getSimpleName());
            }
        } else {
            LogManager.w(TAG, "Activity не найден для пункта меню с ID: " + menuItemId);
        }
        
        return false;
    }
}
