// -*- coding: utf-8 -*-
package model;

import java.time.LocalDateTime;
import java.util.Objects;
import constants.ModelConstants;

/**
 * Модель категории для классификации операций
 * 
 * <p>Представляет категорию для группировки доходов и расходов:
 * <ul>
 *   <li>Родительская категория (CATEGORY_TYPE_PARENT) - основная группа</li>
 *   <li>Дочерняя категория (CATEGORY_TYPE_CHILD) - подкатегория</li>
 * </ul>
 * 
 * <p>Категория может быть связана с определенным типом операций:
 * <ul>
 *   <li>Расходы (OPERATION_TYPE_EXPENSE)</li>
 *   <li>Доходы (OPERATION_TYPE_INCOME)</li>
 * </ul>
 * 
 * <p>Поддерживает иерархическую структуру через parentId.
 * 
 * @author BudgetMaster Team
 * @version 1.0
 */
public class Category extends BaseEntity {
    private int position; // Позиция категории в списке (для сортировки)
    private String title; // Название категории
    private int operationType; // Тип операций для категории (1-расход, 2-доход)
    private int type; // Тип категории (0-родительская, 1-дочерняя)
    private Integer parentId; // ID родительской категории (null для корневых категорий)

    public Category() {}

    public Category(int id, LocalDateTime createTime, LocalDateTime updateTime, LocalDateTime deleteTime,
                    String createdBy, String updatedBy, String deletedBy, int position, String title, int operationType, int type, Integer parentId) {
        super(id, createTime, updateTime, deleteTime, createdBy, updatedBy, deletedBy);
        this.position = position;
        this.title = title;
        this.operationType = operationType;
        this.type = type;
        this.parentId = parentId;
    }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getOperationType() { return operationType; }
    public void setOperationType(int operationType) { this.operationType = operationType; }
    public int getType() { return type; }
    public void setType(int type) { this.type = type; }
    public Integer getParentId() { return parentId; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }
    
    // МЕТОДЫ ДЛЯ РАБОТЫ С КОНСТАНТАМИ
    
    /**
     * Проверяет, является ли категория родительской
     * @return true, если категория родительская
     */
    public boolean isParentCategory() {
        return type == ModelConstants.CATEGORY_TYPE_PARENT;
    }
    
    /**
     * Проверяет, является ли категория дочерней
     * @return true, если категория дочерняя
     */
    public boolean isChildCategory() {
        return type == ModelConstants.CATEGORY_TYPE_CHILD;
    }
    
    /**
     * Проверяет, предназначена ли категория для расходов
     * @return true, если категория для расходов
     */
    public boolean isExpenseCategory() {
        return operationType == ModelConstants.OPERATION_TYPE_EXPENSE;
    }
    
    /**
     * Проверяет, предназначена ли категория для доходов
     * @return true, если категория для доходов
     */
    public boolean isIncomeCategory() {
        return operationType == ModelConstants.OPERATION_TYPE_INCOME;
    }
    
    /**
     * Проверяет, является ли категория корневой (без родителя)
     * @return true, если категория корневая
     */
    public boolean isRootCategory() {
        return parentId == null || parentId == ModelConstants.ROOT_CATEGORY_ID;
    }
    
    /**
     * Проверяет, имеет ли категория родительскую категорию
     * @return true, если у категории есть родитель
     */
    public boolean hasParent() {
        return parentId != null && parentId != ModelConstants.ROOT_CATEGORY_ID;
    }

    /**
     * Проверяет, равны ли два объекта
     * @param o объект для сравнения
     * @return true, если объекты равны
     */
    @Override   
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) return false;
        if (!super.equals(o)) return false;
        Category category = (Category) o;
        return position == category.position && operationType == category.operationType && type == category.type &&
                Objects.equals(title, category.title) &&
                Objects.equals(parentId, category.parentId);
    }

    /**
     * Возвращает хэш-код объекта
     * @return хэш-код объекта
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), position, title, operationType, type, parentId);
    }

    /**
     * Возвращает строковое представление объекта
     * @return строковое представление объекта
     */
    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", position=" + position +
                ", title='" + title + '\'' +
                ", operationType=" + operationType +
                ", type=" + type +
                ", parentId=" + parentId +
                '}';
    }
} 