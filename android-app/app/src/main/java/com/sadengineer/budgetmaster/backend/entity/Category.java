// -*- coding: utf-8 -*-
package com.sadengineer.budgetmaster.backend.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.sadengineer.budgetmaster.backend.converter.DateTimeConverter;

import java.time.LocalDateTime;

/**
 * Entity класс для категорий
 */
@Entity(tableName = "categories",
        foreignKeys = {
                @ForeignKey(entity = Category.class,
                        parentColumns = "id",
                        childColumns = "parentId",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {
                @Index("parentId")
        })
@TypeConverters(DateTimeConverter.class)
public class Category {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private int position; // Позиция для сортировки
    private int operationType; // 1 - доход, 2 - расход
    private int type; // 0-родительская категория, 1-дочерняя категория
    private Integer parentId; // для иерархии категорий

    
    // Поля из BaseEntity
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime deleteTime;
    private String createdBy;
    private String updatedBy;
    private String deletedBy;
    
    // Конструктор для Room
    public Category() {}
    
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
    
    // Алиас для совместимости с валидаторами
    public String getName() {
        return title;
    }
    
    public void setName(String name) {
        this.title = name;
    }
    
    public int getOperationType() {
        return operationType;
    }
    
    public void setOperationType(int operationType) {
        this.operationType = operationType;
    }
    
    public int getType() {
        return type;
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    public Integer getParentId() {
        return parentId;
    }
    
    public void setParentId(Integer parentId) {
        this.parentId = parentId;
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