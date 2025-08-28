package com.sadengineer.budgetmaster.navigation.navigation_tree;

/**
 * Узел навигационного дерева
 * Представляет экран приложения с возможными вкладками и связями с другими экранами
 */
public class NavigationNode {
    public final Class<?> activityClass;
    public final String name;
    public final int tabCount;
    public final String[] tabNames;
    public final String menuId;
    public NavigationNode up;
    public NavigationNode down;
    public NavigationNode left;
    public NavigationNode right;
    
    /**
     * Конструктор узла навигационного дерева
     * @param activityClass класс Activity
     * @param name название экрана
     * @param tabCount количество вкладок (0 если нет вкладок)
     * @param tabNames названия вкладок (null если нет вкладок)
     * @param menuId ID пункта меню (null если нет пункта в меню)
     * @param up узел выше
     * @param down узел ниже
     * @param left узел слева
     * @param right узел справа
     */
    public NavigationNode(
            Class<?> activityClass, 
            String name, 
            int tabCount, 
            String[] tabNames,
            String menuId,
            NavigationNode up, 
            NavigationNode down, 
            NavigationNode left, 
            NavigationNode right) 
    {
        this.activityClass = activityClass;
        this.name = name;
        this.tabCount = tabCount;
        this.tabNames = tabNames;
        this.menuId = menuId;
        this.up = up;
        this.down = down;
        this.left = left;
        this.right = right;
    }
    
    /**
     * Проверяет, имеет ли узел вкладки
     * @return true, если узел имеет вкладки, иначе false
     */
    public boolean hasTabs() {
        return tabCount > 0;
    }
    
    /**
     * Получает название вкладки по индексу
     * @param tabIndex индекс вкладки
     * @return название вкладки или null, если индекс неверный
     */
    public String getTabName(int tabIndex) {
        if (hasTabs() && tabIndex >= 0 && tabIndex < tabCount) {
            return tabNames[tabIndex];
        }
        return null;
    }
    
    /**
     * Проверяет, имеет ли узел пункт в меню
     * @return true, если узел имеет пункт в меню, иначе false
     */
    public boolean hasMenu() {
        return menuId != null && !menuId.isEmpty();
    }

    /**
     * Преобразует узел в строку
     * @return строковое представление узла
     */
    @Override
    public String toString() {
        return "NavigationNode{" +
                "name='" + name + '\'' +
                ", activityClass=" + activityClass.getSimpleName() +
                ", tabCount=" + tabCount +
                ", hasMenu=" + hasMenu() +
                '}';
    }
}
