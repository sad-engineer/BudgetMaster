package com.sadengineer.budgetmaster.navigation;

import android.content.Context;
import android.util.Log;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.navigation.navigation_tree.NavigationNode;
import com.sadengineer.budgetmaster.navigation.navigation_tree.NavigationTree;

import java.util.HashMap;
import java.util.Map;

/**
 * Построитель меню навигации из NavigationTree
 * Автоматически извлекает информацию из NavigationTree без жестких зависимостей
 */
public class MenuBuilder {
    
    private static final String TAG = "MenuBuilder";
    
    // Карта соответствия ID пунктов меню и классов Activity
    private static final Map<Integer, Class<?>> menuIdToActivityMap = new HashMap<>();
    private static boolean isInitialized = false;
    
    /**
     * Инициализирует карту соответствий на основе NavigationTree
     * @param context контекст приложения
     */
    public static void initialize(Context context) {
        if (isInitialized) {
            Log.d(TAG, "MenuBuilder уже инициализирован, пропускаем повторную инициализацию");
            return;
        }
        
        Log.d(TAG, "Инициализация MenuBuilder...");
        
        // Инициализируем NavigationTree если еще не инициализирован
        NavigationTree.initialize(context);
        
        // Автоматически заполняем карту на основе NavigationTree
        populateMenuMap();
        
        isInitialized = true;
        Log.d(TAG, "MenuBuilder инициализирован");
    }
    
    /**
     * Автоматически заполняет карту соответствий на основе NavigationTree
     */
    private static void populateMenuMap() {
        // Получаем все узлы из NavigationTree
        Map<Class<?>, NavigationNode> allNodes = NavigationTree.getAllNodes();
        
        for (Map.Entry<Class<?>, NavigationNode> entry : allNodes.entrySet()) {
            Class<?> activityClass = entry.getKey();
            NavigationNode node = entry.getValue();
            
            // Проверяем, есть ли у узла пункт в меню
            if (node.hasMenu()) {
                // Получаем ID пункта меню из ресурсов
                Integer menuId = getMenuIdFromResources(node.menuId);
                
                if (menuId != null) {
                    menuIdToActivityMap.put(menuId, activityClass);
                    Log.d(TAG, "Добавлен пункт меню: " + node.name + " -> " + activityClass.getSimpleName() + " (ID: " + menuId + ")");
                } else {
                    Log.w(TAG, "Не найден ID меню в ресурсах для: " + node.menuId);
                }
            } else {
                Log.d(TAG, "Узел " + node.name + " не имеет пункта в меню");
            }
        }
    }
    
    /**
     * Получает ID пункта меню из ресурсов по строковому идентификатору
     * @param menuIdString строковый идентификатор пункта меню
     * @return ID пункта меню или null, если не найден
     */
    private static Integer getMenuIdFromResources(String menuIdString) {
        if (menuIdString == null || menuIdString.isEmpty()) {
            return null;
        }
        
        try {
            // Получаем ID ресурса по строковому имени
            return R.id.class.getField(menuIdString).getInt(null);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка получения ID меню для: " + menuIdString, e);
            return null;
        }
    }
    
    /**
     * Получает класс Activity по ID пункта меню
     * @param menuItemId ID пункта меню
     * @return класс Activity или null, если не найден
     */
    public static Class<?> getActivityForMenuId(int menuItemId) {
        ensureInitialized();
        return menuIdToActivityMap.get(menuItemId);
    }
    
    /**
     * Получает ID пункта меню по классу Activity
     * @param activityClass класс Activity
     * @return ID пункта меню или null, если не найден
     */
    public static Integer getMenuIdForActivity(Class<?> activityClass) {
        ensureInitialized();
        for (Map.Entry<Integer, Class<?>> entry : menuIdToActivityMap.entrySet()) {
            if (entry.getValue().equals(activityClass)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    /**
     * Проверяет, существует ли пункт меню для Activity
     * @param activityClass класс Activity
     * @return true, если пункт меню существует
     */
    public static boolean hasMenuForActivity(Class<?> activityClass) {
        ensureInitialized();
        return menuIdToActivityMap.containsValue(activityClass);
    }
    
    /**
     * Получает количество пунктов меню
     * @return количество пунктов меню
     */
    public static int getMenuItemsCount() {
        ensureInitialized();
        return menuIdToActivityMap.size();
    }
    
    /**
     * Логирует все доступные пункты меню
     */
    public static void logAvailableMenuItems() {
        ensureInitialized();
        Log.d(TAG, "Доступные пункты меню (" + getMenuItemsCount() + "):");
        for (Map.Entry<Integer, Class<?>> entry : menuIdToActivityMap.entrySet()) {
            NavigationNode node = NavigationTree.getNode(entry.getValue());
            String nodeName = node != null ? node.name : "Неизвестно";
            Log.d(TAG, "  " + entry.getValue().getSimpleName() + " -> " + nodeName + " (ID: " + entry.getKey() + ")");
        }
    }
    
    /**
     * Проверяет, что MenuBuilder инициализирован
     */
    private static void ensureInitialized() {
        if (!isInitialized) {
            Log.w(TAG, "MenuBuilder не инициализирован! Вызовите initialize()");
        }
    }
}
