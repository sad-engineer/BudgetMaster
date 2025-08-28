package com.sadengineer.budgetmaster.navigation.navigation_tree;

import android.content.Context;
import android.util.Log;

import com.sadengineer.budgetmaster.navigation.navigation_tree.NavigationNode;

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
        Map<Class<?>, NavigationNode> nodesByClass = new HashMap<>();
        Map<String, NavigationNode> nodesByClassName = new HashMap<>();
        
        // Загружаем конфигурацию
        List<NavigationConfigLoader.ActivityConfig> configs = NavigationConfigLoader.loadConfig(context);
        
        // Создаем узлы
        for (ActivityConfig config : configs) {
            try {
                Class<?> activityClass = Class.forName(config.className);
                NavigationNode node = createNode(activityClass, config);
                nodesByClass.put(activityClass, node);
                nodesByClassName.put(config.className, node);
                
                Log.d(TAG, "Создан узел: " + config.name + " (" + config.className + ")");
                
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "Класс Activity не найден: " + config.className, e);
            }
        }
        
        // Устанавливаем связи между узлами
        for (ActivityConfig config : configs) {
            NavigationNode currentNode = nodesByClassName.get(config.className);
            if (currentNode != null) {
                // Связь вверх
                if (config.upClass != null) {
                    NavigationNode upNode = nodesByClassName.get(config.upClass);
                    if (upNode != null) {
                        currentNode.up = upNode;
                        Log.d(TAG, "Установлена связь вверх: " + config.name + " -> " + upNode.name);
                    }
                }
                
                // Связь вниз
                if (config.downClass != null) {
                    NavigationNode downNode = nodesByClassName.get(config.downClass);
                    if (downNode != null) {
                        currentNode.down = downNode;
                        Log.d(TAG, "Установлена связь вниз: " + config.name + " -> " + downNode.name);
                    }
                }
            }
        }
        
        Log.d(TAG, "Навигационное дерево построено. Узлов: " + nodesByClass.size());
        return nodesByClass;
    }
    
    /**
     * Создает узел навигации из конфигурации
     */
    private static NavigationNode createNode(Class<?> activityClass, ActivityConfig config) {
        return new NavigationNode(
                activityClass,
                config.name,
                config.getTabCount(),
                config.tabs,
                config.menuId,
                null, // up - будет установлено позже
                null, // down - будет установлено позже
                null, // left - не используется
                null  // right - не используется
        );
    }
}
