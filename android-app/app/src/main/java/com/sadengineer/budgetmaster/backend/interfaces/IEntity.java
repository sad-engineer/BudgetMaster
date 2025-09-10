package com.sadengineer.budgetmaster.backend.interfaces;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Базовый интерфейс для всех entity классов
 * Определяет общие поля и методы для всех сущностей в системе
 */
public interface IEntity extends Serializable {
    
    /**
     * Получить время создания записи
     * @return время создания
     */
    LocalDateTime getCreateTime();
    
    /**
     * Установить время создания записи
     * @param createTime время создания
     */
    void setCreateTime(LocalDateTime createTime);
    
    /**
     * Получить время последнего обновления записи
     * @return время обновления
     */
    LocalDateTime getUpdateTime();
    
    /**
     * Установить время последнего обновления записи
     * @param updateTime время обновления
     */
    void setUpdateTime(LocalDateTime updateTime);
    
    /**
     * Получить время удаления записи
     * @return время удаления (null если запись не удалена)
     */
    LocalDateTime getDeleteTime();
    
    /**
     * Установить время удаления записи
     * @param deleteTime время удаления
     */
    void setDeleteTime(LocalDateTime deleteTime);
    
    /**
     * Получить пользователя, создавшего запись
     * @return имя пользователя
     */
    String getCreatedBy();
    
    /**
     * Установить пользователя, создавшего запись
     * @param createdBy имя пользователя
     */
    void setCreatedBy(String createdBy);
    
    /**
     * Получить пользователя, последним обновившего запись
     * @return имя пользователя
     */
    String getUpdatedBy();
    
    /**
     * Установить пользователя, последним обновившего запись
     * @param updatedBy имя пользователя
     */
    void setUpdatedBy(String updatedBy);
    
    /**
     * Получить пользователя, удалившего запись
     * @return имя пользователя
     */
    String getDeletedBy();
    
    /**
     * Установить пользователя, удалившего запись
     * @param deletedBy имя пользователя
     */
    void setDeletedBy(String deletedBy);
    
    /**
     * Проверить, удалена ли запись
     * @return true если запись удалена (deleteTime != null)
     */
    boolean isDeleted();
}
