// -*- coding: utf-8 -*-
package model;

import java.time.LocalDateTime;
import java.util.Objects;
import constants.ModelConstants;

/**
 * Модель валюты
 * 
 * <p>Представляет валюту в системе:
 * <ul>
 *   <li>Название валюты (рубли, доллары, евро и т.д.)</li>
 *   <li>Позиция в списке валют (для сортировки)</li>
 *   <li>Валюта по умолчанию (рубли)</li>
 * </ul>
 * 
 * <p>Валюта используется для счетов, операций и бюджетов.
 * Все суммы хранятся в копейках/центах для точности расчетов.
 */
public class Currency extends BaseEntity {
    private int position; // Позиция валюты в списке (для сортировки)
    private String title; // Название валюты

    public Currency() {}

    public Currency(int id, LocalDateTime createTime, LocalDateTime updateTime, LocalDateTime deleteTime,
                    String createdBy, String updatedBy, String deletedBy, int position, String title) {
        super(id, createTime, updateTime, deleteTime, createdBy, updatedBy, deletedBy);
        this.position = position;
        this.title = title;
    }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    // МЕТОДЫ ДЛЯ РАБОТЫ С КОНСТАНТАМИ
    
    /**
     * Проверяет, является ли валюта валютой по умолчанию
     * @return true, если валюта является валютой по умолчанию (рубли)
     */
    public boolean isDefaultCurrency() {
        return id == ModelConstants.DEFAULT_CURRENCY_ID;
    }
    
    /**
     * Проверяет, имеет ли валюта название
     * @return true, если название валюты не пустое
     */
    public boolean hasTitle() {
        return title != null && !title.trim().isEmpty();
    }

    /**
     * Проверяет, равны ли два объекта
     * @param o объект для сравнения
     * @return true, если объекты равны
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Currency)) return false;
        if (!super.equals(o)) return false;
        Currency currency = (Currency) o;
        return position == currency.position && Objects.equals(title, currency.title);
    }

    /**
     * Возвращает хэш-код объекта
     * @return хэш-код объекта
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), position, title);
    }

    /**
     * Возвращает строковое представление объекта
     * @return строковое представление объекта
     */
    @Override
    public String toString() {
        return "Currency{" +
                "id=" + id +
                ", position=" + position +
                ", title='" + title + '\'' +
                '}';
    }
} 