package com.sadengineer.budgetmaster.interfaces;

import java.util.List;

/**
 * Интерфейс для адаптеров с поддержкой выбора
 */
public interface SelectionAdapter<T> {
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
}