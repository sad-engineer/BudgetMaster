package com.sadengineer.budgetmaster.navigation.navigation_tree;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Навигационное дерево приложения
 * Управляет структурой навигации между экранами
 */
public class NavigationTree {
    
    private static final String TAG = "NavigationTree";
    
    private static NavigationNode rootNode;
    private static Map<Class<?>, NavigationNode> nodesByClass;
    
    /**
     * Инициализирует навигационное дерево
     * @param context контекст приложения
     */
    public static void initialize(Context context) {
        if (rootNode != null) {
            Log.d(TAG, "Навигационное дерево уже инициализировано, пропускаем повторную инициализацию");
            return;
        }
        
        Log.d(TAG, "Инициализация навигационного дерева...");
        
        // Строим дерево
        nodesByClass = NavigationTreeBuilder.buildTree(context);
        
        // Находим корневой узел (MainActivity)
        for (NavigationNode node : nodesByClass.values()) {
            if (node.activityClass.getSimpleName().equals("MainActivity")) {
                rootNode = node;
                break;
            }
        }
        
        if (rootNode == null) {
            Log.w(TAG, "Корневой узел (MainActivity) не найден");
            // Берем первый узел как корневой
            if (!nodesByClass.isEmpty()) {
                rootNode = nodesByClass.values().iterator().next();
                Log.d(TAG, "Установлен корневой узел: " + rootNode.name);
            }
        } else {
            Log.d(TAG, "Установлен корневой узел: " + rootNode.name);
        }
        
        Log.d(TAG, "Навигационное дерево инициализировано. Узлов: " + nodesByClass.size());
    }
    
    /**
     * Получает корневой узел
     * @return корневой узел навигации
     */
    public static NavigationNode getRootNode() {
        ensureInitialized();
        return rootNode;
    }
    
    /**
     * Получает узел по классу Activity
     * @param activityClass класс Activity
     * @return узел навигации или null
     */
    public static NavigationNode getNode(Class<?> activityClass) {
        ensureInitialized();
        return nodesByClass.get(activityClass);
    }
    
    /**
     * Получает все узлы
     * @return карта всех узлов
     */
    public static Map<Class<?>, NavigationNode> getAllNodes() {
        ensureInitialized();
        return new HashMap<>(nodesByClass);
    }
    
    /**
     * Создает Intent для перехода к узлу
     * @param context контекст
     * @param node узел назначения
     * @param tabIndex индекс вкладки
     * @return Intent для запуска Activity
     */
    public static Intent createIntent(Context context, NavigationNode node, int tabIndex) {
        return IntentFactory.createIntent(context, node, tabIndex);
    }
    
    /**
     * Создает Intent для перехода к Activity по классу
     * @param context контекст
     * @param activityClass класс Activity
     * @param tabIndex индекс вкладки
     * @return Intent для запуска Activity
     */
    public static Intent createIntent(Context context, Class<?> activityClass, int tabIndex) {
        return IntentFactory.createIntent(context, activityClass, tabIndex);
    }

    /**
     * Навигация вверх по дереву
     * @param currentActivityClass текущий класс Activity
     * @return узел навигации или null
     */
    public static NavigationNode navigateUp(Class<?> currentActivityClass) {
        ensureInitialized();
        NavigationNode currentNode = getNode(currentActivityClass);
        if (currentNode != null && currentNode.up != null) {
            return currentNode.up;
        }
        return rootNode; // Возвращаемся к корню если нет родителя
    }

    /**
     * Навигация вниз по дереву
     * @param currentActivityClass текущий класс Activity
     * @return узел навигации или null
     */
    public static NavigationNode navigateDown(Class<?> currentActivityClass) {
        ensureInitialized();
        NavigationNode currentNode = getNode(currentActivityClass);
        if (currentNode != null && currentNode.down != null) {
            return currentNode.down; // Возвращаем первого ребенка
        }
        return null;
    }

    /**
     * Навигация влево (переход к предыдущей вкладке)
     * @param currentActivityClass текущий класс Activity
     * @param currentTabIndex текущий индекс вкладки
     * @return узел навигации или null
     */
    public static NavigationNode navigateLeft(Class<?> currentActivityClass, int currentTabIndex) {
        ensureInitialized();
        NavigationNode currentNode = getNode(currentActivityClass);
        if (currentNode != null && currentNode.left != null) {
            return currentNode.left; // Остаемся на той же Activity, но меняем вкладку
        }
        return null;
    }

    /**
     * Навигация вправо (переход к следующей вкладке)
     * @param currentActivityClass текущий класс Activity
     * @param currentTabIndex текущий индекс вкладки
     * @return узел навигации или null
     */
    public static NavigationNode navigateRight(Class<?> currentActivityClass, int currentTabIndex) {
        ensureInitialized();
        NavigationNode currentNode = getNode(currentActivityClass);
        if (currentNode != null && currentNode.right != null) {
            return currentNode.right; // Остаемся на той же Activity, но меняем вкладку
        }
        return null;
    }

    /**
     * Переход к конкретной Activity
     * @param targetActivityClass целевой класс Activity
     * @return узел навигации или null
     */
    public static NavigationNode navigateToActivity(Class<?> targetActivityClass) {
        ensureInitialized();
        return getNode(targetActivityClass);
    }

    /**
     * Получает индекс предыдущей вкладки
     * @param currentActivityClass текущий класс Activity
     * @param currentTabIndex текущий индекс вкладки
     * @return индекс предыдущей вкладки
     */
    public static int getPreviousTabIndex(Class<?> currentActivityClass, int currentTabIndex) {
        return Math.max(0, currentTabIndex - 1);
    }

    /**
     * Получает индекс следующей вкладки
     * @param currentActivityClass текущий класс Activity
     * @param currentTabIndex текущий индекс вкладки
     * @return индекс следующей вкладки
     */
    public static int getNextTabIndex(Class<?> currentActivityClass, int currentTabIndex) {
        NavigationNode currentNode = getNode(currentActivityClass);
        if (currentNode != null) {
            return Math.min(currentNode.tabCount - 1, currentTabIndex + 1);
        }
        return currentTabIndex;
    }
        
    /**
     * Проверяет, инициализировано ли дерево
     */
    private static void ensureInitialized() {
        if (rootNode == null || nodesByClass == null) {
            throw new IllegalStateException("Навигационное дерево не инициализировано. Вызовите NavigationTree.initialize(context)");
        }
    }
}
