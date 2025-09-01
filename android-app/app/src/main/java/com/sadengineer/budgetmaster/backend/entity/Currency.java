package com.sadengineer.budgetmaster.backend.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import androidx.room.Index;

import com.sadengineer.budgetmaster.backend.converter.DateTimeConverter;
import com.sadengineer.budgetmaster.backend.constants.RepositoryConstants;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

import java.time.LocalDateTime;

/**
 * Entity класс для валют
 */
@Entity(
    tableName = RepositoryConstants.TABLE_CURRENCIES, 
    indices = {
        @Index(
            value = "title", 
            unique = true
            ),
        @Index(
            value = "shortName", 
            unique = true
            )
        }
    )
@TypeConverters(DateTimeConverter.class)
public class Currency implements Serializable {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String title; // Название валюты
    private String shortName; // Короткое имя валюты 1-3 символа (RUB, ₽)
    //private String code; // Код валюты (USD, EUR, RUB)
    //private String symbol; // Символ валюты ($, €, ₽)
    //private boolean isDefault; // Валюта по умолчанию
    private int position; // Позиция для сортировки
    private double exchangeRate; // Обменный курс к главной валюте (1.0 для главной валюты)
    
    // Поля из BaseEntity
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime deleteTime;
    private String createdBy;
    private String updatedBy;
    private String deletedBy;
    
    // Конструктор для Room
    public Currency() {}
    
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
    
    public String getShortName() {
        return shortName;
    }
    
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
    
    public int getPosition() {
        return position;
    }
    
    public void setPosition(int position) {
        this.position = position;
    }
    
    public double getExchangeRate() {
        return exchangeRate;
    }
    
    public void setExchangeRate(double exchangeRate) {
        this.exchangeRate = exchangeRate;
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
    
    /**
     * Конвертировать сумму из этой валюты в главную валюту
     * @param amount сумма в копейках в текущей валюте
     * @return сумма с копейках в главной валюте
     */
    public long convertToMainCurrency(long amount) {
        // Используем BigDecimal для точных вычислений
        return BigDecimal.valueOf(amount)
                .multiply(BigDecimal.valueOf(exchangeRate))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
    }
    
    /**
     * Конвертировать сумму из главной валюты в эту валюту
     * @param amount сумма с копейках в главной валюте
     * @return сумма с копейках в текущей валюте
     */
    public long convertFromMainCurrency(long amount) {
        if (exchangeRate == 0L) {
            throw new ArithmeticException("Обменный курс не может быть равен нулю");
        }
        // Используем BigDecimal для точных вычислений
        return BigDecimal.valueOf(amount)
                .divide(BigDecimal.valueOf(exchangeRate), 0, RoundingMode.HALF_UP)
                .longValue();
    }
    
    /**
     * Получить обратный курс (для конвертации из главной валюты)
     */
    public double getReverseExchangeRate() {
        if (exchangeRate == 0.0) {
            throw new ArithmeticException("Обменный курс не может быть равен нулю");
        }
        // Используем BigDecimal для точных вычислений
        return BigDecimal.ONE
                .divide(BigDecimal.valueOf(exchangeRate), 10, RoundingMode.HALF_UP)
                .doubleValue();
    }
} 