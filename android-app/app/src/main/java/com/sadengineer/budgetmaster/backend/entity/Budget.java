package com.sadengineer.budgetmaster.backend.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
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
                        onDelete = ForeignKey.CASCADE)
        })
@TypeConverters(DateTimeConverter.class)
public class Budget {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String name;
    private int amount; // Сумма в копейках
    private String currency;
    private LocalDateTime startDate; // Изменено с Date на LocalDateTime
    private LocalDateTime endDate;   // Изменено с Date на LocalDateTime
    private Integer categoryId; // может быть null для общего бюджета
    private String period; // "monthly", "yearly", "custom"
    private boolean isActive;
    private int position; // Позиция для сортировки
    
    // Поля из BaseEntity
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime deleteTime;
    private String createdBy;
    private String updatedBy;
    private String deletedBy;
    
    // Конструкторы
    public Budget() {}
    
    public Budget(String name, int amount, String currency, LocalDateTime startDate, 
                 LocalDateTime endDate, Integer categoryId, String period, boolean isActive) {
        this.name = name;
        this.amount = amount;
        this.currency = currency;
        this.startDate = startDate;
        this.endDate = endDate;
        this.categoryId = categoryId;
        this.period = period;
        this.isActive = isActive;
    }
    
    // Геттеры и сеттеры
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getAmount() {
        return amount;
    }
    
    public void setAmount(int amount) {
        this.amount = amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public LocalDateTime getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }
    
    public LocalDateTime getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
    
    public Integer getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getPeriod() {
        return period;
    }
    
    public void setPeriod(String period) {
        this.period = period;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
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
    
    public int getPosition() {
        return position;
    }
    
    public void setPosition(int position) {
        this.position = position;
    }
    
    // Методы для проверки статуса
    public boolean isDeleted() {
        return deleteTime != null;
    }
} 