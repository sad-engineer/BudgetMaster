
package com.sadengineer.budgetmaster.backend.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.sadengineer.budgetmaster.backend.converter.DateTimeConverter;

import java.time.LocalDateTime;

/**
 * Entity класс для бюджетов
 */
@Entity(tableName = "budgets",
        foreignKeys = {
                @ForeignKey(entity = Category.class,
                        parentColumns = "id",
                        childColumns = "categoryId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Currency.class,
                        parentColumns = "id",
                        childColumns = "currencyId",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {
                @Index("categoryId"),
                @Index("currencyId")
        })
@TypeConverters(DateTimeConverter.class)
public class Budget {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private int amount; // Сумма в копейках
    private int currencyId; // ID валюты
    private Integer categoryId; // может быть null для общего бюджета
    private int position; // Позиция для сортировки
    
    // Поля из BaseEntity
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime deleteTime;
    private String createdBy;
    private String updatedBy;
    private String deletedBy;
    
    // Конструктор для Room
    public Budget() {}
    
    // Геттеры и сеттеры
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getAmount() {
        return amount;
    }
    
    public void setAmount(int amount) {
        this.amount = amount;
    }
    
    public int getCurrencyId() {
        return currencyId;
    }
    
    public void setCurrencyId(int currencyId) {
        this.currencyId = currencyId;
    }
    
    public Integer getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }
    
    public int getPosition() {
        return position;
    }
    
    public void setPosition(int position) {
        this.position = position;
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
} 