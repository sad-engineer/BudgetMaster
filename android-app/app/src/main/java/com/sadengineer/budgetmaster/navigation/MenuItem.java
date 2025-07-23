package com.sadengineer.budgetmaster.navigation;

public class MenuItem {
    private String title;
    private int iconResId;
    private MenuItemType type;

    public enum MenuItemType {
        MAIN,
        ACCOUNTS,
        INCOME,
        EXPENSE,
        BUDGET,
        STATISTICS,
        CURRENCIES,
        INCOME_CATEGORIES,
        EXPENSE_CATEGORIES,
        IMPORT_DATA,
        EXPORT_DATA,
        SETTINGS,
        VERSION,
        AUTHORS,
        INSTRUCTIONS
    }

    public MenuItem(String title, int iconResId, MenuItemType type) {
        this.title = title;
        this.iconResId = iconResId;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public int getIconResId() {
        return iconResId;
    }

    public MenuItemType getType() {
        return type;
    }
} 