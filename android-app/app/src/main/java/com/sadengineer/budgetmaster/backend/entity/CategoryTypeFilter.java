package com.sadengineer.budgetmaster.backend.entity;

import com.sadengineer.budgetmaster.backend.constants.ModelConstants;

/**
 * Фильтр типов категорий
 */
public enum CategoryTypeFilter {

    /**
     * Только родительские категории
     */
    PARENT(ModelConstants.CATEGORY_TYPE_PARENT),
    
    /**
     * Только дочерние категории
     */
    CHILD(ModelConstants.CATEGORY_TYPE_CHILD),

    /**
     * Все категории
     */
    ALL(null);

    private final Integer categoryType;

    CategoryTypeFilter(Integer categoryType) {
        this.categoryType = categoryType;
    }

    /**
     * Получить значение типа операции
     * @return значение типа операции или null для ALL
     */
    public Integer getIndex() {
        return categoryType;
    }
} 


