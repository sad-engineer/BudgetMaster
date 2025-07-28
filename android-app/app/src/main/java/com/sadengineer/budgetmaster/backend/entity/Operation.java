package com.sadengineer.budgetmaster.backend.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.sadengineer.budgetmaster.backend.converter.DateTimeConverter;

import java.time.LocalDateTime;

/**
 * Entity класс для операций (доходы/расходы)
 */
@Entity(tableName = "operations",
        foreignKeys = {
                @ForeignKey(entity = Account.class,
                        parentColumns = "id",
                        childColumns = "accountId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Category.class,
                        parentColumns = "id",
                        childColumns = "categoryId",
                        onDelete = ForeignKey.RESTRICT)
        })
@TypeConverters(DateTimeConverter.class)
public class Operation {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private int accountId;
    private int categoryId;
    private int amount; // Сумма в копейках
    private String description;
    private LocalDateTime operationDate; // Изменено с Date на LocalDateTime
    private String type; // "income" или "expense"
    private int currencyId; // ID валюты операции
    private Integer toAccountId; // ID целевого счета (для переводов)
    private Integer toCurrencyId; // ID целевой валюты (для переводов)
    private Integer toAmount; // Сумма в целевой валюте (для переводов)
    
    // Поля из BaseEntity
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime deleteTime;
    private String createdBy;
    private String updatedBy;
    private String deletedBy;
    
    // Конструкторы
    public Operation() {}
    
    public Operation(int accountId, int categoryId, int amount, String description, 
                   LocalDateTime operationDate, String type) {
        this.accountId = accountId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.description = description;
        this.operationDate = operationDate;
        this.type = type;
    }
    
    // Геттеры и сеттеры
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getAccountId() {
        return accountId;
    }
    
    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }
    
    public int getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
    
    public int getAmount() {
        return amount;
    }
    
    public void setAmount(int amount) {
        this.amount = amount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getOperationDate() {
        return operationDate;
    }
    
    public void setOperationDate(LocalDateTime operationDate) {
        this.operationDate = operationDate;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    // Геттеры и сеттеры для полей BaseEntity
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    public LocalDateTime getDeleteTime() {
        return deleteTime;
    }
    
    public void setDeleteTime(LocalDateTime deleteTime) {
        this.deleteTime = deleteTime;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    public String getDeletedBy() {
        return deletedBy;
    }
    
    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }
    
    public int getCurrencyId() {
        return currencyId;
    }
    
    public void setCurrencyId(int currencyId) {
        this.currencyId = currencyId;
    }
    
    public Integer getToAccountId() {
        return toAccountId;
    }
    
    public void setToAccountId(Integer toAccountId) {
        this.toAccountId = toAccountId;
    }
    
    public Integer getToCurrencyId() {
        return toCurrencyId;
    }
    
    public void setToCurrencyId(Integer toCurrencyId) {
        this.toCurrencyId = toCurrencyId;
    }
    
    public Integer getToAmount() {
        return toAmount;
    }
    
    public void setToAmount(Integer toAmount) {
        this.toAmount = toAmount;
    }
    
    // Методы для проверки статуса
    public boolean isDeleted() {
        return deleteTime != null;
    }
    
    // Методы для проверки типа операции
    public boolean isExpense() {
        return "expense".equals(type);
    }
    
    public boolean isIncome() {
        return "income".equals(type);
    }
    
    public boolean isTransfer() {
        return toAccountId != null && toCurrencyId != null && toAmount != null;
    }
} 