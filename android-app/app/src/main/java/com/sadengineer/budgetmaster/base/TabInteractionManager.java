package com.sadengineer.budgetmaster.base;

import android.content.Context;
import android.content.DialogInterface;
 
import androidx.appcompat.app.AlertDialog;

import com.sadengineer.budgetmaster.backend.interfaces.IService;
import com.sadengineer.budgetmaster.utils.LogManager;

import java.io.Serializable;

/**
 * Менеджер для управления взаимодействием с вкладками:
 * - Удаление элементов
 * - Мягкое удаление
 * - Создание новых элементов
 * 
 * @param <T> Тип сущности (должен реализовывать Serializable)
 * @param <S> Тип сервиса
 */
public class TabInteractionManager<T extends Serializable, S extends IService<T>> {
    private static final String TAG = "TabInteractionManager";
    
    // Зависимости
    private final Context context;
    private final S service;
    private final String userName;

    private String textTemplate;
    
    /**
     * Интерфейс для получения заголовка элемента
     */
    public interface ItemTitleProvider<T> {
        String getItemTitle(T item);
    }
    
    /**
     * Конструктор
     */
    public TabInteractionManager(Context context, S service, String userName) {
        this.context = context;
        this.service = service;
        this.userName = userName;

        setTextTemplate("Вы уверены, что хотите полностью удалить %s?\n\n" +
                          "⚠️ Это действие нельзя отменить!");
    }

    public String getTextTemplate() {
        return textTemplate;
    }

    public void setTextTemplate(String textTemplate) {
        this.textTemplate = textTemplate;
    }
    
    /**
     * Показывает диалог подтверждения удаления элемента
     * @param item элемент
     * @param itemTitle заголовок элемента для вывода в лог
     */
    public void showDeleteConfirmationDialog(T item, String itemTitle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Удаление")
               .setMessage(String.format(textTemplate, itemTitle))
                .setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteItem(item, itemTitle);
                    }
                })
               .setNegativeButton("Отмена", null)
               .setIcon(android.R.drawable.ic_dialog_alert)
               .show();
    }
    
    /**
     * Удаляет элемент из базы данных (полное удаление)
     * @param item элемент
     * @param itemTitle заголовок элемента для вывода в лог
     */
    public void deleteItem(T item, String itemTitle ) {
        LogManager.d(TAG, "Удаление элемента: " + itemTitle);
        try {
            if (service != null) {
                service.delete(item, false);
                LogManager.d(TAG, "Запрос на удаление элемента отправлен");
            } else {
                LogManager.e(TAG, "Сервис не найден");
            }
        } catch (Exception e) {
            LogManager.e(TAG, "Ошибка удаления элемента " + itemTitle + ": " + e.getMessage(), e);
        }
    }
}
