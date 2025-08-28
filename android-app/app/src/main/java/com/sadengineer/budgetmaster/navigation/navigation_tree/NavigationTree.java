package com.sadengineer.budgetmaster.navigation.navigation_tree;

import android.content.Context;
import android.util.Log;

import com.sadengineer.budgetmaster.navigation.navigation_tree.NavigationNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Навигационное дерево приложения
 * Устанавливает зависимости между экранами и правила перехода между ними
 * Использует конфигурацию из JSON файла
 */
public class NavigationTree {
    
    private static final String TAG = "NavigationTree";
    
    private static Map<Class<?>, NavigationNode> nodesByClass = new HashMap<>();
    private static NavigationNode rootNode;
    private static boolean isInitialized = false;
    
    /**
     * Инициализирует навигационное дерево из конфигурации
     * @param context контекст приложения
     */
    public static void initialize(Context context) {
        if (isInitialized) {
            Log.d(TAG, "Навигационное дерево уже инициализировано");
            return;
        }
        
        Log.d(TAG, "Инициализация навигационного дерева из конфигурации");
        
        // Строим дерево из конфигурации
        nodesByClass = NavigationTreeBuilder.buildTree(context);
        
        // Устанавливаем корневой узел (MainActivity)
        try {
            Class<?> mainActivityClass = Class.forName("com.sadengineer.budgetmaster.MainActivity");
            rootNode = nodesByClass.get(mainActivityClass);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "MainActivity не найден", e);
        }
        
        isInitialized = true;
        Log.d(TAG, "Навигационное дерево инициализировано. Узлов: " + nodesByClass.size());
    }
    
    /**
     * Получает узел навигационного дерева по классу Activity
     * @param activityClass класс Activity
     * @return узел навигационного дерева
     */
    public static NavigationNode getNode(Class<?> activityClass) {
        ensureInitialized();
        return nodesByClass.get(activityClass);
    }
    
    /**
     * Получает корневой узел навигационного дерева
     * @return корневой узел навигационного дерева
     */
    public static NavigationNode getRootNode() {
        ensureInitialized();
        return rootNode;
    }
    
    /**
     * Получает все узлы навигационного дерева
     * @return карта всех узлов
     */
    public static Map<Class<?>, NavigationNode> getAllNodes() {
        ensureInitialized();
        return new HashMap<>(nodesByClass);
    }
    
    /**
     * Переходит на узел выше
     * @param currentActivity текущий экран
     * @return узел выше
     */
    public static NavigationNode navigateUp(Class<?> currentActivity) {
        ensureInitialized();
        NavigationNode currentNode = getNode(currentActivity);
        if (currentNode != null && currentNode.up != null) {
            Log.d(TAG, "Навигация вверх: " + currentNode.name + " -> " + currentNode.up.name);
            return currentNode.up;
        }
        Log.d(TAG, "Навигация вверх невозможна из " + currentActivity.getSimpleName());
        return null;
    }
    
    /**
     * Переходит на узел ниже
     * @param currentActivity текущий экран
     * @return узел ниже
     */
    public static NavigationNode navigateDown(Class<?> currentActivity) {
        ensureInitialized();
        NavigationNode currentNode = getNode(currentActivity);
        if (currentNode != null && currentNode.down != null) {
            Log.d(TAG, "Навигация вниз: " + currentNode.name + " -> " + currentNode.down.name);
            return currentNode.down;
        }
        Log.d(TAG, "Навигация вниз невозможна из " + currentActivity.getSimpleName());
        return null;
    }
    
    /**
     * Переходит на предыдущую вкладку (навигация влево)
     * @param currentActivity текущий экран
     * @param currentTabIndex текущая вкладка
     * @return узел с новой вкладкой или null, если переход невозможен
     */
    public static NavigationNode navigateLeft(Class<?> currentActivity, int currentTabIndex) {
        ensureInitialized();
        NavigationNode currentNode = getNode(currentActivity);
        if (currentNode != null && currentNode.hasTabs()) {
            int previousTab = (currentTabIndex - 1 + currentNode.tabCount) % currentNode.tabCount;
            Log.d(TAG, "Навигация влево: " + currentNode.name + " вкладка " + 
                  currentNode.getTabName(currentTabIndex) + " -> " + currentNode.getTabName(previousTab));
            return currentNode;
        }
        Log.d(TAG, "Навигация влево невозможна из " + currentActivity.getSimpleName() + 
              " (нет вкладок или неверный индекс)");
        return null;
    }
    
    /**
     * Переходит на следующую вкладку (навигация вправо)
     * @param currentActivity текущий экран
     * @param currentTabIndex текущая вкладка
     * @return узел с новой вкладкой или null, если переход невозможен
     */
    public static NavigationNode navigateRight(Class<?> currentActivity, int currentTabIndex) {
        ensureInitialized();
        NavigationNode currentNode = getNode(currentActivity);
        if (currentNode != null && currentNode.hasTabs()) {
            int nextTab = (currentTabIndex + 1) % currentNode.tabCount;
            Log.d(TAG, "Навигация вправо: " + currentNode.name + " вкладка " + 
                  currentNode.getTabName(currentTabIndex) + " -> " + currentNode.getTabName(nextTab));
            return currentNode;
        }
        Log.d(TAG, "Навигация вправо невозможна из " + currentActivity.getSimpleName() + 
              " (нет вкладок или неверный индекс)");
        return null;
    }
    
    /**
     * Получает индекс следующей вкладки
     * @param currentActivity текущий экран
     * @param currentTabIndex текущая вкладка
     * @return индекс следующей вкладки
     */
    public static int getNextTabIndex(Class<?> currentActivity, int currentTabIndex) {
        ensureInitialized();
        NavigationNode currentNode = getNode(currentActivity);
        if (currentNode != null && currentNode.hasTabs()) {
            return (currentTabIndex + 1) % currentNode.tabCount;
        }
        return currentTabIndex;
    }
    
    /**
     * Получает индекс предыдущей вкладки
     * @param currentActivity текущий экран
     * @param currentTabIndex текущая вкладка
     * @return индекс предыдущей вкладки
     */
    public static int getPreviousTabIndex(Class<?> currentActivity, int currentTabIndex) {
        ensureInitialized();
        NavigationNode currentNode = getNode(currentActivity);
        if (currentNode != null && currentNode.hasTabs()) {
            return (currentTabIndex - 1 + currentNode.tabCount) % currentNode.tabCount;
        }
        return currentTabIndex;
    }
    
    /**
     * Создает Intent для перехода на узел
     * @param context контекст
     * @param node узел
     * @param tabIndex индекс вкладки
     * @return Intent
     */
    public static android.content.Intent createIntent(Context context, NavigationNode node, int tabIndex) {
        return IntentFactory.createIntent(context, node, tabIndex);
    }
    
    /**
     * Получает название вкладки
     * @param activityClass класс Activity
     * @param tabIndex индекс вкладки
     * @return название вкладки
     */
    public static String getTabName(Class<?> activityClass, int tabIndex) {
        ensureInitialized();
        NavigationNode node = getNode(activityClass);
        if (node != null) {
            return node.getTabName(tabIndex);
        }
        return null;
    }
    
    /**
     * Проверяет, имеет ли узел вкладки
     * @param activityClass класс Activity
     * @return true, если узел имеет вкладки, иначе false
     */
    public static boolean hasTabs(Class<?> activityClass) {
        ensureInitialized();
        NavigationNode node = getNode(activityClass);
        return node != null && node.hasTabs();
    }
    
    /**
     * Получает количество вкладок
     * @param activityClass класс Activity
     * @return количество вкладок
     */
    public static int getTabCount(Class<?> activityClass) {
        ensureInitialized();
        NavigationNode node = getNode(activityClass);
        return node != null ? node.tabCount : 0;
    }

    /**
     * Переходит на узел по классу Activity
     * @param currentActivity текущий экран
     * @param targetActivity класс Activity
     * @return узел с новой Activity или null, если переход невозможен
     */
    public static NavigationNode navigateToActivity(Class<?> currentActivity, Class<?> targetActivity) {
        ensureInitialized();
        NavigationNode currentNode = getNode(currentActivity);
        NavigationNode targetNode = getNode(targetActivity);
        if (currentNode != null && targetNode != null) {
            Log.d(TAG, "Навигация: " + currentNode.name + " -> " + targetNode.name);
            return targetNode;
        }
        Log.d(TAG, "Навигация к " + targetActivity.getSimpleName() + " невозможна");
        return null;
    }
    
    /**
     * Проверяет, инициализировано ли дерево
     */
    private static void ensureInitialized() {
        if (!isInitialized) {
            throw new IllegalStateException("NavigationTree не инициализирован. Вызовите NavigationTree.initialize(context)");
        }
    }
}
