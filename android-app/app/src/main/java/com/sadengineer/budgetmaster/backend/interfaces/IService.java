package com.sadengineer.budgetmaster.interfaces;

import androidx.lifecycle.LiveData;
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;
import java.util.List;

/**
 * Базовый интерфейс для всех сервисов приложения
 * Определяет общие методы для работы с сущностями
 * 
 * @param <T> тип сущности
 */
public interface IService<T> {
    
    // ========== CRUD операции ==========
    
    /**
     * Создать новую сущность
     * @param entity сущность для создания
     */
    void create(T entity);
    
    /**
     * Обновить существующую сущность
     * @param entity сущность для обновления
     */
    void update(T entity);
    
    /**
     * Удалить сущность
     * @param entity сущность для удаления
     * @param softDelete true - мягкое удаление, false - полное удаление
     */
    void delete(T entity, boolean softDelete);
    
    /**
     * Восстановить удаленную сущность (только для мягкого удаления)
     * @param entity сущность для восстановления
     */
    void restore(T entity);
    
    // ========== Получение данных ==========
    
    /**
     * Получить все сущности с фильтром
     * @param filter фильтр для выборки сущностей
     * @return LiveData со списком сущностей
     */
    LiveData<List<T>> getAll(EntityFilter filter);
    
    /**
     * Получить все сущности (включая удаленные)
     * @return LiveData со списком всех сущностей
     */
    LiveData<List<T>> getAll();
    
    /**
     * Получить сущность по ID
     * @param id ID сущности
     * @return LiveData с сущностью
     */
    LiveData<T> getById(int id);
    
    // ========== Подсчет ==========
    
    /**
     * Получить количество сущностей с фильтром
     * @param filter фильтр для выборки сущностей
     * @return количество сущностей
     */
    int getCount(EntityFilter filter);
    
    /**
     * Получить общее количество сущностей (включая удаленные)
     * @return общее количество сущностей
     */
    int getCount();
    
    // ========== Управление позициями ==========
    
    /**
     * Изменить позицию сущности
     * @param entity сущность
     * @param newPosition новая позиция
     */
    void changePosition(T entity, int newPosition);
    
    /**
     * Изменить позицию сущности по старой позиции
     * @param oldPosition старая позиция
     * @param newPosition новая позиция
     */
    void changePosition(int oldPosition, int newPosition);
    
    /**
     * Изменить позицию сущности по названию
     * @param title название сущности
     * @param newPosition новая позиция
     */
    void changePosition(String title, int newPosition);
}
