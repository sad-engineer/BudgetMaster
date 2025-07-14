// -*- coding: utf-8 -*-
package model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Account extends BaseEntity {
    private int position;
    private String title;
    private int amount;
    private int type;
    private int currencyId;
    private int closed;
    private Integer creditCardLimit;
    private Integer creditCardCategoryId;
    private Integer creditCardCommissionCategoryId;

    public Account() {}

    public Account(int id, LocalDateTime createTime, LocalDateTime updateTime, LocalDateTime deleteTime,
                   String createdBy, String updatedBy, String deletedBy, int position, String title, int amount, int type,
                   int currencyId, int closed, Integer creditCardLimit, Integer creditCardCategoryId, Integer creditCardCommissionCategoryId) {
        super(id, createTime, updateTime, deleteTime, createdBy, updatedBy, deletedBy);
        this.position = position;
        this.title = title;
        this.amount = amount;
        this.type = type;
        this.currencyId = currencyId;
        this.closed = closed;
        this.creditCardLimit = creditCardLimit;
        this.creditCardCategoryId = creditCardCategoryId;
        this.creditCardCommissionCategoryId = creditCardCommissionCategoryId;
    }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
    public int getType() { return type; }
    public void setType(int type) { this.type = type; }
    public int getCurrencyId() { return currencyId; }
    public void setCurrencyId(int currencyId) { this.currencyId = currencyId; }
    public int getClosed() { return closed; }
    public void setClosed(int closed) { this.closed = closed; }
    public Integer getCreditCardLimit() { return creditCardLimit; }
    public void setCreditCardLimit(Integer creditCardLimit) { this.creditCardLimit = creditCardLimit; }
    public Integer getCreditCardCategoryId() { return creditCardCategoryId; }
    public void setCreditCardCategoryId(Integer creditCardCategoryId) { this.creditCardCategoryId = creditCardCategoryId; }
    public Integer getCreditCardCommissionCategoryId() { return creditCardCommissionCategoryId; }
    public void setCreditCardCommissionCategoryId(Integer creditCardCommissionCategoryId) { this.creditCardCommissionCategoryId = creditCardCommissionCategoryId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;
        if (!super.equals(o)) return false;
        Account account = (Account) o;
        return position == account.position && amount == account.amount && type == account.type && currencyId == account.currencyId && closed == account.closed &&
                Objects.equals(title, account.title) &&
                Objects.equals(creditCardLimit, account.creditCardLimit) &&
                Objects.equals(creditCardCategoryId, account.creditCardCategoryId) &&
                Objects.equals(creditCardCommissionCategoryId, account.creditCardCommissionCategoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), position, title, amount, type, currencyId, closed, creditCardLimit, creditCardCategoryId, creditCardCommissionCategoryId);
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", position=" + position +
                ", title='" + title + '\'' +
                ", amount=" + amount +
                ", type=" + type +
                ", currencyId=" + currencyId +
                ", closed=" + closed +
                ", creditCardLimit=" + creditCardLimit +
                ", creditCardCategoryId=" + creditCardCategoryId +
                ", creditCardCommissionCategoryId=" + creditCardCommissionCategoryId +
                '}';
    }
} 