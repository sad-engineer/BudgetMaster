// -*- coding: utf-8 -*-
package com.sadengineer.budgetmaster.backend.model;

import java.time.LocalDateTime;
import java.util.Objects;
import com.sadengineer.budgetmaster.backend.constants.ModelConstants;

/**
 * Модель счета пользователя
 * 
 * <p>Представляет финансовый счет с различными типами:
 * <ul>
 *   <li>Расчетный счет (ACCOUNT_TYPE_CURRENT)</li>
 *   <li>Сберегательный счет (ACCOUNT_TYPE_SAVINGS)</li>
 *   <li>Кредитный счет (ACCOUNT_TYPE_CREDIT)</li>
 * </ul>
 * 
 * <p>Счет может быть открытым или закрытым, иметь баланс в определенной валюте.
 * Кредитные счета могут иметь дополнительные параметры: лимит, категории для комиссий.
 */
public class Account extends BaseEntity {
    
    private int position;   // Позиция счета в списке (для сортировки)
    private String title; // Название счета
    private int amount; // Текущий баланс счета (в копейках/центах)
    private int type; // Тип счета (1-расчетный, 2-сберегательный, 3-кредитный)
    private int currencyId; // ID валюты счета
    private int closed; // Статус закрытия счета (0-открыт, 1-закрыт)
    private Integer creditCardLimit; // Лимит кредитной карты (только для кредитных счетов)
    private Integer creditCardCategoryId; // ID категории для операций по кредитной карте
    private Integer creditCardCommissionCategoryId; // ID категории для комиссий по кредитной карте

    public Account() {}

    /**
     * @param id уникальный идентификатор
     * @param createTime время создания записи
     * @param updateTime время последнего обновления
     * @param deleteTime время удаления (может быть null)
     * @param createdBy пользователь, создавший запись
     * @param updatedBy пользователь, последний раз обновивший запись
     * @param deletedBy пользователь, удаливший запись (может быть null)
     * @param position позиция счета в списке
     * @param title название счета
     * @param amount текущий баланс счета
     * @param type тип счета
     * @param currencyId ID валюты счета
     * @param closed статус закрытия счета
     * @param creditCardLimit лимит кредитной карты (может быть null)
     * @param creditCardCategoryId ID категории для операций по кредитной карте (может быть null)
     * @param creditCardCommissionCategoryId ID категории для комиссий по кредитной карте (может быть null)
     */
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
    
    // МЕТОДЫ ДЛЯ РАБОТЫ С КОНСТАНТАМИ
    
    /**
     * Проверяет, является ли счет расчетным
     * @return true, если счет расчетный
     */
    public boolean isCurrentAccount() {
        return type == ModelConstants.ACCOUNT_TYPE_CURRENT;
    }
    
    /**
     * Проверяет, является ли счет сберегательным
     * @return true, если счет сберегательный
     */
    public boolean isSavingsAccount() {
        return type == ModelConstants.ACCOUNT_TYPE_SAVINGS;
    }
    
    /**
     * Проверяет, является ли счет кредитным
     * @return true, если счет кредитный
     */
    public boolean isCreditAccount() {
        return type == ModelConstants.ACCOUNT_TYPE_CREDIT;
    }

    /**
     * Проверяет, удален ли счет
     * @return true, если счет удален
     */
    public boolean isDeleted() {
        return deleteTime != null;
    }
    
    /**
     * Проверяет, открыт ли счет
     * @return true, если счет открыт
     */
    public boolean isOpen() {
        return closed == ModelConstants.ACCOUNT_STATUS_OPEN;
    }
    
    /**
     * Проверяет, закрыт ли счет
     * @return true, если счет закрыт
     */
    public boolean isClosed() {
        return closed == ModelConstants.ACCOUNT_STATUS_CLOSED;
    }
    
    /**
     * Проверяет, имеет ли кредитная карта установленный лимит
     * @return true, если лимит установлен и больше нуля
     */
    public boolean hasCreditCardLimit() {
        return creditCardLimit != null && creditCardLimit > ModelConstants.MIN_AMOUNT;
    }
    
    /**
     * Проверяет, является ли счет счетом по умолчанию
     * @return true, если счет имеет настройки по умолчанию
     */
    public boolean isDefaultAccount() {
        return type == ModelConstants.DEFAULT_ACCOUNT_TYPE && 
               currencyId == ModelConstants.DEFAULT_CURRENCY_ID && 
               closed == ModelConstants.ACCOUNT_STATUS_OPEN;
    }
    
    /**
     * Проверяет, имеет ли счет положительный баланс
     * @return true, если баланс больше нуля
     */
    public boolean hasAmount() {
        return amount > ModelConstants.MIN_AMOUNT;
    }

    /**
     * Проверяет, равны ли два объекта
     * @param o объект для сравнения
     * @return true, если объекты равны
     */
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

    /**
     * Возвращает хэш-код объекта
     * @return хэш-код объекта
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), position, title, amount, type, currencyId, closed, creditCardLimit, creditCardCategoryId, creditCardCommissionCategoryId);
    }

    /**
     * Возвращает строковое представление объекта
     * @return строковое представление объекта
     */
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