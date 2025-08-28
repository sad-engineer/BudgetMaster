package com.sadengineer.budgetmaster.navigation.navigation_tree;

/**
 * Конфигурация Activity для навигационного дерева
 */
public class ActivityConfig {
    public final String className;
    public final String name;
    public final String[] tabs;
    public final String upClass;
    public final String downClass;
    public final String menuId;
    
    public ActivityConfig(String className, String name, String[] tabs, String upClass, String downClass, String menuId) {
        this.className = className;
        this.name = name;
        this.tabs = tabs;
        this.upClass = upClass;
        this.downClass = downClass;
        this.menuId = menuId;
    }
    
    /**
     * Проверяет, имеет ли Activity вкладки
     * @return true, если Activity имеет вкладки, иначе false
     */
    public boolean hasTabs() {
        return tabs != null && tabs.length > 0;
    }
    
    /**
     * Получает количество вкладок
     * @return количество вкладок
     */
    public int getTabCount() {
        return tabs != null ? tabs.length : 0;
    }
    
    /**
     * Получает название вкладки по индексу
     * @param tabIndex индекс вкладки
     * @return название вкладки или null, если индекс неверный
     */
    public String getTabName(int tabIndex) {
        if (hasTabs() && tabIndex >= 0 && tabIndex < tabs.length) {
            return tabs[tabIndex];
        }
        return null;
    }
    
    /**
     * Проверяет, имеет ли Activity пункт в меню
     * @return true, если Activity имеет пункт в меню, иначе false
     */
    public boolean hasMenu() {
        return menuId != null && !menuId.isEmpty();
    }
    
    /**
     * Преобразует ActivityConfig в строку
     * @return строковое представление ActivityConfig
     */
    @Override
    public String toString() {
        return "ActivityConfig{" +
                "className='" + className + '\'' +
                ", name='" + name + '\'' +
                ", tabCount=" + getTabCount() +
                ", hasMenu=" + hasMenu() +
                '}';
    }
}
