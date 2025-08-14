package com.sadengineer.budgetmaster.base;

import androidx.lifecycle.LiveData;

/**
 * Контракт для ViewModel списков с режимом выбора и мягким удалением.
 * Экран передаёт реализацию этого интерфейса в базовую активити.
 */
public interface SelectionListViewModel {
    LiveData<Boolean> getSelectionMode();
    void enableSelectionMode();
    void cancelSelectionMode();

    LiveData<Boolean> getDeleting();
    LiveData<Integer> getSoftDeletionDone();

    void deleteSelectedItemsSoft();
}


