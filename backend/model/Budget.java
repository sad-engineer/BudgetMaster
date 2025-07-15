// -*- coding: utf-8 -*-
package model;

import java.time.LocalDateTime;
import java.util.Objects;
import constants.ModelConstants;

/**
 * Модель бюджета для планирования расходов
 * 
 * <p>Представляет бюджетную категорию с установленным лимитом расходов:
 * <ul>
 *   <li>Сумма бюджета - максимально допустимые расходы</li>
 *   <li>Валюта бюджета - в какой валюте установлен лимит</li>
 *   <li>Категория бюджета - для какой категории расходов</li>
 * </ul>
 * 
 * <p>Бюджет может быть привязан к конкретной категории расходов
 * или быть общим для всех расходов (categoryId = null).
 * 
 * @author BudgetMaster Team
 * @version 1.0
 */
public class Budget extends BaseEntity {
    private int amount; // Сумма бюджета (в копейках/центах)
    private int currencyId; // ID валюты бюджета
    private Integer categoryId; // ID категории бюджета (null для общего бюджета)
    private int position; // Позиция бюджета в списке (для сортировки)

    public Budget() {}

    public Budget(int id, LocalDateTime createTime, LocalDateTime updateTime, LocalDateTime deleteTime,
                  String createdBy, String updatedBy, String deletedBy, int position, int amount, int currencyId, Integer categoryId) {
        super(id, createTime, updateTime, deleteTime, createdBy, updatedBy, deletedBy);
        this.amount = amount;
        this.currencyId = currencyId;
        this.categoryId = categoryId;
        this.position = position;
    }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
    public int getCurrencyId() { return currencyId; }
    public void setCurrencyId(int currencyId) { this.currencyId = currencyId; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    
    // МЕТОДЫ ДЛЯ РАБОТЫ С КОНСТАНТАМИ
    
    /**
     * Проверяет, имеет ли бюджет положительную сумму
     * @return true, если сумма бюджета больше нуля
     */
    public boolean hasAmount() {
        return amount > ModelConstants.MIN_AMOUNT;
    }
    
    /**
     * Проверяет, привязан ли бюджет к конкретной категории
     * @return true, если бюджет имеет категорию
     */
    public boolean hasCategory() {
        return categoryId != null && categoryId != ModelConstants.INVALID_ID;
    }
    
    /**
     * Проверяет, является ли бюджет бюджетом по умолчанию
     * @return true, если сумма бюджета равна значению по умолчанию
     */
    public boolean isDefaultBudget() {
        return amount == ModelConstants.DEFAULT_BUDGET_AMOUNT;
    }

    /**
     * Проверяет, удален ли бюджет
     * @return true, если бюджет удален
     */
    public boolean isDeleted() {
        return getDeletedBy() != null;
    }

    /**
     * Проверяет, равны ли два объекта
     * @param o объект для сравнения
     * @return true, если объекты равны
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Budget)) return false;
        if (!super.equals(o)) return false;
        Budget budget = (Budget) o;
        return amount == budget.amount && currencyId == budget.currencyId && Objects.equals(categoryId, budget.categoryId);
    }

    /**
     * Возвращает хэш-код объекта
     * @return хэш-код объекта
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), amount, currencyId, categoryId);
    }

    /**
     * Возвращает строковое представление объекта
     * @return строковое представление объекта
     */
    @Override
    public String toString() {
        return "Budget{" +
                "id=" + id +
                ", amount=" + amount +
                ", currencyId=" + currencyId +
                ", categoryId=" + categoryId +
                '}';
    }
} 