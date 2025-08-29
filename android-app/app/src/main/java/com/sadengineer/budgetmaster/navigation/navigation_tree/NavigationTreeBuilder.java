package com.sadengineer.budgetmaster.navigation.navigation_tree;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Построитель навигационного дерева из конфигурации
 */
public class NavigationTreeBuilder {
    
    private static final String TAG = "NavigationTreeBuilder";
    
    /**
     * Строит навигационное дерево из конфигурации
     * @param context контекст приложения
     * @return карта узлов навигационного дерева
     */
    public static Map<Class<?>, NavigationNode> buildTree(Context context) {
        // Загружаем конфигурацию (узлы уже созданы с установленными связями)
        Map<Class<?>, NavigationNode> nodesByClass = NavigationConfigLoader.loadConfig(context);
        
        Log.d(TAG, "Навигационное дерево построено. Узлов: " + nodesByClass.size());
        return nodesByClass;
    }
    

}
