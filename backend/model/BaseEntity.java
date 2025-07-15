// -*- coding: utf-8 -*-
package model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Базовый класс для всех сущностей в системе
 * 
 * <p>Содержит общие поля для всех сущностей:
 * <ul>
 *   <li>id - уникальный идентификатор</li>
 *   <li>createTime - время создания записи</li>
 *   <li>updateTime - время последнего обновления</li>
 *   <li>deleteTime - время удаления (для soft delete)</li>
 *   <li>createdBy - пользователь, создавший запись</li>
 *   <li>updatedBy - пользователь, последний раз обновивший запись</li>
 *   <li>deletedBy - пользователь, удаливший запись</li>
 * </ul>
 * 
 * <p>Реализует паттерн "Soft Delete" - записи не удаляются физически,
 * а помечаются как удаленные с помощью поля deleteTime.
 */
public abstract class BaseEntity {
    protected int id; // Уникальный идентификатор сущности
    protected LocalDateTime createTime; // Время создания записи
    protected LocalDateTime updateTime; // Время последнего обновления записи
    protected LocalDateTime deleteTime; // Время удаления записи (для soft delete)
    protected String createdBy; // Пользователь, создавший запись
    protected String updatedBy; // Пользователь, последний раз обновивший запись
    protected String deletedBy;

    public BaseEntity() {}

    /**
     * @param id уникальный идентификатор
     * @param createTime время создания записи
     * @param updateTime время последнего обновления
     * @param deleteTime время удаления (может быть null)
     * @param createdBy пользователь, создавший запись
     * @param updatedBy пользователь, последний раз обновивший запись
     * @param deletedBy пользователь, удаливший запись (может быть null)
     */
    public BaseEntity(int id, LocalDateTime createTime, LocalDateTime updateTime, LocalDateTime deleteTime,
                      String createdBy, String updatedBy, String deletedBy) {
        this.id = id;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.deleteTime = deleteTime;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.deletedBy = deletedBy;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public LocalDateTime getDeleteTime() { return deleteTime; }
    public void setDeleteTime(LocalDateTime deleteTime) { this.deleteTime = deleteTime; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public String getDeletedBy() { return deletedBy; }
    public void setDeletedBy(String deletedBy) { this.deletedBy = deletedBy; }

    // МЕТОДЫ ДЛЯ РАБОТЫ С ОБЪЕКТАМИ

    /**
     * Проверяет, равны ли два объекта
     * @param o объект для сравнения
     * @return true, если объекты равны
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseEntity)) return false;
        BaseEntity that = (BaseEntity) o;
        return id == that.id &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateTime, that.updateTime) &&
                Objects.equals(deleteTime, that.deleteTime) &&
                Objects.equals(createdBy, that.createdBy) &&
                Objects.equals(updatedBy, that.updatedBy) &&
                Objects.equals(deletedBy, that.deletedBy);
    }

    /**
     * Возвращает хэш-код объекта
     * @return хэш-код объекта
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, createTime, updateTime, deleteTime, createdBy, updatedBy, deletedBy);
    }

    /**
     * Возвращает строковое представление объекта
     * @return строковое представление объекта
     */
    @Override
    public String toString() {
        return "BaseEntity{" +
                "id=" + id +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", deleteTime=" + deleteTime +
                ", createdBy='" + createdBy + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                ", deletedBy='" + deletedBy + '\'' +
                '}';
    }
} 