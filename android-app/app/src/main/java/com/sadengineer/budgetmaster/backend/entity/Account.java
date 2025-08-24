package com.sadengineer.budgetmaster.backend.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.sadengineer.budgetmaster.backend.converter.DateTimeConverter;
import com.sadengineer.budgetmaster.backend.constants.RepositoryConstants;

import java.io.Serializable;

import java.time.LocalDateTime;

/**
 * Entity класс для счетов
 */
@Entity(tableName = RepositoryConstants.TABLE_ACCOUNTS)
@TypeConverters(DateTimeConverter.class)
public class Account implements Serializable {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String title; // Название счета
    private int position; // Позиция для сортировки
    private long amount; // Сумма в копейках // Баланс счета
    private int type; // Тип счета (1=current, 2=savings, 3=credit)
    private int currencyId; // ID валюты
    private int closed; // Статус закрытия (0=open, 1=closed)
    
    // Поля для кредитных карт
    private Integer creditCardLimit; // Лимит кредитной карты
    private Integer creditCardCategoryId; // ID категории для кредитной карты
    private Integer creditCardCommissionCategoryId; // ID категории для комиссии
    
    // Поля из BaseEntity
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime deleteTime;
    private String createdBy;
    private String updatedBy;
    private String deletedBy;
    
    // Конструктор для Room
    public Account() {}
    
    // Геттеры и сеттеры
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public int getPosition() {
        return position;
    }
    
    public void setPosition(int position) {
        this.position = position;
    }
    
    public long getAmount() {
        return amount;
    }
    
    public void setAmount(long amount) {
        this.amount = amount;
    }
    
    public int getType() {
        return type;
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    public int getCurrencyId() {
        return currencyId;
    }
    
    public void setCurrencyId(int currencyId) {
        this.currencyId = currencyId;
    }
    
    public int getClosed() {
        return closed;
    }
    
    public void setClosed(int closed) {
        this.closed = closed;
    }
    
    // Геттеры и сеттеры для кредитных карт
    public Integer getCreditCardLimit() {
        return creditCardLimit;
    }
    
    public void setCreditCardLimit(Integer creditCardLimit) {
        this.creditCardLimit = creditCardLimit;
    }
    
    public Integer getCreditCardCategoryId() {
        return creditCardCategoryId;
    }
    
    public void setCreditCardCategoryId(Integer creditCardCategoryId) {
        this.creditCardCategoryId = creditCardCategoryId;
    }
    
    public Integer getCreditCardCommissionCategoryId() {
        return creditCardCommissionCategoryId;
    }
    
    public void setCreditCardCommissionCategoryId(Integer creditCardCommissionCategoryId) {
        this.creditCardCommissionCategoryId = creditCardCommissionCategoryId;
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
    
    // Методы для проверки статуса
    public boolean isDeleted() {
        return deleteTime != null;
    }
    
    public boolean isClosed() {
        return closed == 1;
    }
    
    public boolean isActive() {
        return !isDeleted() && !isClosed();
    }
} 