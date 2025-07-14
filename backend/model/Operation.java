// -*- coding: utf-8 -*-
package model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Operation extends BaseEntity {
    private int type;
    private LocalDateTime date;
    private int amount;
    private String comment;
    private int categoryId;
    private int accountId;
    private int currencyId;
    private Integer toAccountId;
    private Integer toCurrencyId;
    private Integer toAmount;

    public Operation() {}

    public Operation(int id, LocalDateTime createTime, LocalDateTime updateTime, LocalDateTime deleteTime,
                     String createdBy, String updatedBy, String deletedBy, int type, LocalDateTime date, int amount, String comment,
                     int categoryId, int accountId, int currencyId, Integer toAccountId, Integer toCurrencyId, Integer toAmount) {
        super(id, createTime, updateTime, deleteTime, createdBy, updatedBy, deletedBy);
        this.type = type;
        this.date = date;
        this.amount = amount;
        this.comment = comment;
        this.categoryId = categoryId;
        this.accountId = accountId;
        this.currencyId = currencyId;
        this.toAccountId = toAccountId;
        this.toCurrencyId = toCurrencyId;
        this.toAmount = toAmount;
    }

    public int getType() { return type; }
    public void setType(int type) { this.type = type; }
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }
    public int getCurrencyId() { return currencyId; }
    public void setCurrencyId(int currencyId) { this.currencyId = currencyId; }
    public Integer getToAccountId() { return toAccountId; }
    public void setToAccountId(Integer toAccountId) { this.toAccountId = toAccountId; }
    public Integer getToCurrencyId() { return toCurrencyId; }
    public void setToCurrencyId(Integer toCurrencyId) { this.toCurrencyId = toCurrencyId; }
    public Integer getToAmount() { return toAmount; }
    public void setToAmount(Integer toAmount) { this.toAmount = toAmount; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Operation)) return false;
        if (!super.equals(o)) return false;
        Operation that = (Operation) o;
        return type == that.type && amount == that.amount && categoryId == that.categoryId && accountId == that.accountId && currencyId == that.currencyId &&
                Objects.equals(date, that.date) &&
                Objects.equals(comment, that.comment) &&
                Objects.equals(toAccountId, that.toAccountId) &&
                Objects.equals(toCurrencyId, that.toCurrencyId) &&
                Objects.equals(toAmount, that.toAmount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type, date, amount, comment, categoryId, accountId, currencyId, toAccountId, toCurrencyId, toAmount);
    }

    @Override
    public String toString() {
        return "Operation{" +
                "id=" + id +
                ", type=" + type +
                ", date=" + date +
                ", amount=" + amount +
                ", comment='" + comment + '\'' +
                ", categoryId=" + categoryId +
                ", accountId=" + accountId +
                ", currencyId=" + currencyId +
                ", toAccountId=" + toAccountId +
                ", toCurrencyId=" + toCurrencyId +
                ", toAmount=" + toAmount +
                '}';
    }
} 