package com.sadengineer.budgetmaster.categories;

import com.sadengineer.budgetmaster.backend.entity.Category;

/**
 * Класс для представления узла дерева категорий
 */
public class CategoryTreeItem {
    private Category category;
    private int level; // Уровень вложенности (0 - корень, 1 - первый уровень, и т.д.)
    private boolean isExpanded; // Развернут ли узел
    private boolean hasChildren; // Есть ли дочерние элементы
    
    public CategoryTreeItem(Category category, int level) {
        this.category = category;
        this.level = level;
        this.isExpanded = true; // По умолчанию развернуто
        this.hasChildren = false;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public void setCategory(Category category) {
        this.category = category;
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public boolean isExpanded() {
        return isExpanded;
    }
    
    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }
    
    public boolean hasChildren() {
        return hasChildren;
    }
    
    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }
    
    public int getCategoryId() {
        return category != null ? category.getId() : -1;
    }
    
    public String getTitle() {
        return category != null ? category.getTitle() : "";
    }
    
    public Integer getParentId() {
        return category != null ? category.getParentId() : null;
    }
    
    public int getType() {
        return category != null ? category.getType() : 0;
    }
    
    public boolean isParent() {
        return category != null && category.getType() == 0; // CATEGORY_TYPE_PARENT
    }
    
    public boolean isChild() {
        return category != null && category.getType() == 1; // CATEGORY_TYPE_CHILD
    }
}
