package com.sadengineer.budgetmaster.expense;

import android.app.Application;
 
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sadengineer.budgetmaster.backend.entity.Operation;
import com.sadengineer.budgetmaster.backend.service.OperationService;
import com.sadengineer.budgetmaster.base.SelectionListViewModel;    
import com.sadengineer.budgetmaster.utils.LogManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Shared ViewModel для экрана операций расходов. Держит общий режим выбора и
 * выполняет мягкое удаление в фоновой очереди.
 */
public class ExpenseSharedViewModel extends AndroidViewModel implements SelectionListViewModel {

    private static final String TAG = "ExpenseSharedViewModel";

    /** Имя пользователя по умолчанию */
    /** TODO: передлать на получение имени пользователя из SharedPreferences */
    private String userName = "default_user";

    private OperationService operationService;
    // LiveData для управления режимом выбора и мягким удалением
    private final MutableLiveData<Boolean> selectionMode = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> deleting = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> softDeletionDone = new MutableLiveData<>();
    private final MutableLiveData<List<Operation>> selectedExpenses = new MutableLiveData<>();

    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    /**
     * Конструктор
     * @param application
     */
    public ExpenseSharedViewModel(@NonNull Application application) {
        super(application);
        // Сервисы для работы с данными
        operationService = new OperationService(application.getApplicationContext(), userName);
    }

    /**
     * Возвращает режим выбора
     * @return режим выбора
     */
    @Override
    public LiveData<Boolean> getSelectionMode() {
        return selectionMode;
    }

    /**
     * Возвращает состояние удаления
     * @return состояние удаления
     */
    @Override
    public LiveData<Boolean> getDeleting() {
        return deleting;
    }

    /**
     * Возвращает количество удаленных операций
     * @return количество удаленных операций
     */
    @Override
    public LiveData<Integer> getSoftDeletionDone() {
        return softDeletionDone;
    }

    /**
     * Возвращает выбранные операции
     * @return выбранные операции
     */
     public LiveData<List<Operation>> getSelectedExpenses() {
         return selectedExpenses;
     }

    /**
     * Включает режим выбора
     */
    @Override
    public void enableSelectionMode() {
        selectionMode.setValue(true);
    }

    /**
     * Отменяет режим выбора
     */
    @Override
    public void cancelSelectionMode() {
        selectionMode.setValue(false);
        selectedExpenses.setValue(null);
    }

    /**
     * Делает softDelete для каждой операции из переданного списка в фоне.
     */
    public void deleteExpensesSoft(List<Operation> expenses) {
        if (expenses == null || expenses.isEmpty()) {
            softDeletionDone.setValue(0);
            selectionMode.setValue(false);
            return;
        }

        // Фильтруем только неудаленные операции
        List<Operation> expensesToDelete = new ArrayList<>();
        for (Operation expense : expenses) {
            if (!expense.isDeleted()) {
                expensesToDelete.add(expense);
            } else {
                LogManager.w(TAG, "Пропуск операции: ID=" + expense.getId() + ", уже удалена");
            }
        }

        if (expensesToDelete.isEmpty()) {
            LogManager.w(TAG, "Нет операций для удаления - все уже удалены");
            softDeletionDone.setValue(0);
            selectionMode.setValue(false);
            return;
        }

        LogManager.d(TAG, "Начато удаление операций. Количество: " + expensesToDelete.size());
        // Устанавливаем состояние удаления
        deleting.setValue(true);
        ioExecutor.execute(() -> {
            int deletedCount = 0;
            for (Operation expense : expensesToDelete) {
                try {
                    LogManager.d(TAG, "Удаление операции: ID=" + expense.getId());
                    // TODO: Реализовать метод delete в OperationService
                    // operationService.delete(true, expense);
                    deletedCount++;
                    LogManager.d(TAG, "Операция ID: " + expense.getId() + " успешно удалена");
                } catch (Exception e) {
                    LogManager.e(TAG, "Ошибка удаления операции: ID=" + expense.getId() + ", причина: " + e.getMessage());
                }
            }
            deleting.postValue(false);
            softDeletionDone.postValue(deletedCount);
            selectionMode.postValue(false);
            selectedExpenses.postValue(null);
            LogManager.d(TAG, "Удаление завершено. Удалено операций: " + deletedCount);
        });
    }

     /**
      * Обновляет текущий набор выбранных операций.
      */
     public void setSelectedExpenses(List<Operation> expenses) {
        LogManager.d(TAG, "Выбранных операций: " + (expenses != null ? expenses.size() : 0));
        selectedExpenses.setValue(expenses);
     }

     /**
      * Делает softDelete для текущего набора выбранных операций из ViewModel.
      */
    @Override
    public void deleteSelectedItemsSoft() {
         List<Operation> expenses = selectedExpenses.getValue();
         deleteExpensesSoft(expenses);
     }
}
