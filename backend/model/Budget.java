// -*- coding: utf-8 -*-
package model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Budget extends BaseEntity {
    private int amount;
    private int currencyId;
    private Integer categoryId;
    private int position;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Budget)) return false;
        if (!super.equals(o)) return false;
        Budget budget = (Budget) o;
        return amount == budget.amount && currencyId == budget.currencyId && Objects.equals(categoryId, budget.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), amount, currencyId, categoryId);
    }

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