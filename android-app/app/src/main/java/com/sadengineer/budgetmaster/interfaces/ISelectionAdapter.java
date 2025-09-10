package com.sadengineer.budgetmaster.interfaces;

import java.util.List;

/**
 * Интерфейс для адаптеров с поддержкой выбора
 */
public interface ISelectionAdapter<T> {
    /**
     * Включает или выключает режим выбора
     * @param enabled true - включить режим выбора, false - выключить
     */
    void setSelectionMode(boolean enabled);
    
    /**
     * Возвращает список выбранных элементов
     * @return список выбранных элементов
     */
    List<T> getSelectedItems();

    /**
     * Устанавливает данные в адаптер
     * @param items список элементов
     */
    void setItems(List<T> items);
}