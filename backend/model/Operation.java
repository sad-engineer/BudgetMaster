// -*- coding: utf-8 -*-
package model;

import java.time.LocalDateTime;
import java.util.Objects;
import constants.ModelConstants;

/**
 * Модель финансовой операции
 * 
 * <p>Представляет финансовую операцию в системе:
 * <ul>
 *   <li>Расход (OPERATION_TYPE_EXPENSE) - трата денег</li>
 *   <li>Доход (OPERATION_TYPE_INCOME) - получение денег</li>
 *   <li>Перевод (transfer) - перемещение денег между счетами</li>
 * </ul>
 * 
 * <p>Операция содержит информацию о сумме, дате, категории, счете и валюте.
 * Для переводов дополнительно указываются целевой счет, валюта и сумма.
 * 
 * @author BudgetMaster Team
 * @version 1.0
 */
public class Operation extends BaseEntity {
    private int type; // Тип операции (1-расход, 2-доход)
    private LocalDateTime date; // Дата и время операции
    private int amount; // Сумма операции (в копейках/центах)
    private String comment; // Комментарий к операции
    private int categoryId; // ID категории операции
    private int accountId; // ID счета операции
    private int currencyId; // ID валюты операции
    private Integer toAccountId; // ID целевого счета (для переводов)
    private Integer toCurrencyId; // ID целевой валюты (для переводов)
    private Integer toAmount; // Сумма в целевой валюте (для переводов)

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
    
    // МЕТОДЫ ДЛЯ РАБОТЫ С КОНСТАНТАМИ
    
    /**
     * Проверяет, является ли операция расходом
     * @return true, если операция расход
     */
    public boolean isExpense() {
        return type == ModelConstants.OPERATION_TYPE_EXPENSE;
    }
    
    /**
     * Проверяет, является ли операция доходом
     * @return true, если операция доход
     */
    public boolean isIncome() {
        return type == ModelConstants.OPERATION_TYPE_INCOME;
    }
    
    /**
     * Проверяет, является ли операция переводом
     * @return true, если операция перевод между счетами
     */
    public boolean isTransfer() {
        return toAccountId != null && toCurrencyId != null && toAmount != null;
    }
    
    /**
     * Проверяет, имеет ли операция комментарий
     * @return true, если комментарий не пустой
     */
    public boolean hasComment() {
        return comment != null && !comment.trim().isEmpty();
    }
    
    /**
     * Проверяет, имеет ли операция положительную сумму
     * @return true, если сумма больше нуля
     */
    public boolean hasAmount() {
        return amount > ModelConstants.MIN_AMOUNT;
    }
    
    /**
     * Проверяет, является ли операция операцией по умолчанию
     * @return true, если операция имеет тип по умолчанию
     */
    public boolean isDefaultOperation() {
        return type == ModelConstants.DEFAULT_OPERATION_TYPE;
    }

    /**
     * Проверяет, равны ли два объекта
     * @param o объект для сравнения
     * @return true, если объекты равны
     */
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

    /**
     * Возвращает хэш-код объекта
     * @return хэш-код объекта
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type, date, amount, comment, categoryId, accountId, currencyId, toAccountId, toCurrencyId, toAmount);
    }

    /**
     * Возвращает строковое представление объекта
     * @return строковое представление объекта
     */
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