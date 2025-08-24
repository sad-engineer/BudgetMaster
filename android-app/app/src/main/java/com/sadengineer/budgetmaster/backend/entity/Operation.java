package com.sadengineer.budgetmaster.backend.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.sadengineer.budgetmaster.backend.converter.DateTimeConverter;
import com.sadengineer.budgetmaster.backend.constants.RepositoryConstants;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entity класс для операций (доходы/расходы)
 */
@Entity(tableName = RepositoryConstants.TABLE_OPERATIONS,
        foreignKeys = {
                @ForeignKey(entity = Account.class,
                        parentColumns = "id",
                        childColumns = "accountId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Category.class,
                        parentColumns = "id",
                        childColumns = "categoryId",
                        onDelete = ForeignKey.RESTRICT)
        },
        indices = {
                @Index("accountId"),
                @Index("categoryId")
        })
@TypeConverters(DateTimeConverter.class)
public class Operation implements Serializable {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private int accountId;
    private int categoryId;
    private long amount; // Сумма в копейках (long)
    private String description;
    private LocalDateTime operationDate; // Изменено с Date на LocalDateTime
    private int type; // 1 - "income", 2 - "expense"
    private int currencyId; // ID валюты операции
    private Integer toAccountId; // ID целевого счета (для переводов)
    private Integer toCurrencyId; // ID целевой валюты (для переводов)
    private Long toAmount; // Сумма в целевой валюте (для переводов)
    
    // Поля из BaseEntity
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime deleteTime;
    private String createdBy;
    private String updatedBy;
    private String deletedBy;
    
    // Конструктор для Room
    public Operation() {}
    
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
    
    public long getAmount() {
        return amount;
    }
    
    public void setAmount(long amount) {
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
    
    public int getType() {
        return type;
    }
    
    public void setType(int type) {
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
    
    public Long getToAmount() {
        return toAmount;
    }
    
    public void setToAmount(Long toAmount) {
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