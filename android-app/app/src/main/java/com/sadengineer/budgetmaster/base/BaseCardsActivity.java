package com.sadengineer.budgetmaster.base;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
 
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;
import com.sadengineer.budgetmaster.utils.LogManager;

/**
 * Базовый Activity для отображения списков данных в виде карточек (RecyclerView)
 * с поддержкой кнопок действий и индикатора загрузки.
 */
public abstract class BaseCardsActivity<T> extends BaseNavigationActivity {

    protected final String TAG = this.getClass().getSimpleName();

    protected RecyclerView recyclerView;
    protected ImageButton addButton;
    protected ImageButton deleteButton;
    protected View emptySpace;
    protected ProgressBar loadingIndicator;
    @Nullable protected SelectionListViewModel selectionVm;
    protected boolean isSelectionMode = false;

    /**
     * Привязка общих элементов UI. Любой ID может быть null/0, если элемент отсутствует.
     */
    protected void setupCommonCardsUi(int recyclerViewId,
                                      Integer addButtonId,
                                      Integer deleteButtonId,
                                      Integer emptySpaceId) {
        recyclerView = findViewById(recyclerViewId);

        if (addButtonId != null && addButtonId != 0) {
            addButton = findViewById(addButtonId);
            if (addButton != null) {
                LogManager.d(TAG, "setupCommonCardsUi: Устанавливаем обработчик для кнопки добавления");
                addButton.setOnClickListener(v -> {
                    LogManager.d(TAG, "setupCommonCardsUi: Нажата кнопка добавления");
                    onAddClicked();
                });
            }
        }

        if (deleteButtonId != null && deleteButtonId != 0) {
            deleteButton = findViewById(deleteButtonId);
            if (deleteButton != null) {
                deleteButton.setOnClickListener(v -> onDeleteClicked());
            }
        }

        if (emptySpaceId != null && emptySpaceId != 0) {
            emptySpace = findViewById(emptySpaceId);
        }
    }

    /**
     * Привязать ViewModel списка с режимом выбора и операциями удаления, чтобы базовый класс
     * смог взять на себя стандартную логику обработки кнопок и прогресса.
     */
    protected void bindSelectionViewModel(@Nullable SelectionListViewModel vm) {
        this.selectionVm = vm;
        if (vm == null) return;

        vm.getSelectionMode().observe(this, enabled -> {
            boolean isSelection = Boolean.TRUE.equals(enabled);
            this.isSelectionMode = isSelection;
            if (isSelection) {
                updateButtonsIcons(R.drawable.ic_save, R.drawable.ic_back);
            } else {
                updateButtonsIcons(R.drawable.ic_add, R.drawable.ic_delete);
            }
            onSelectionModeChanged(this.isSelectionMode);
        });

        vm.getSoftDeletionDone().observe(this, count -> {
            // по умолчанию — no-op, можно переопределить в наследнике при необходимости
        });

        vm.getDeleting().observe(this, isDeleting -> {
            if (isDeleting != null) {
                if (isDeleting) showLoadingIndicator(); else hideLoadingIndicator();
            }
        });

        if (addButton != null) {
            addButton.setOnClickListener(v -> {
                if (isSelectionMode) {
                    vm.deleteSelectedItemsSoft();
                } else {
                    onAddClicked();
                }
            });
        }

        if (deleteButton != null) {
            deleteButton.setOnClickListener(v -> {
                toggleSelectionMode();
            });
        }
    }

    /**
     * Установка адаптера и LayoutManager для RecyclerView.
     */
    protected void setupRecycler(RecyclerView.Adapter<?> adapter, RecyclerView.LayoutManager layoutManager) {
        if (recyclerView == null) return;
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Обновить иконки кнопок действий.
     */
    protected void updateButtonsIcons(int addIconResId, int deleteIconResId) {
        if (addButton != null && addIconResId != 0) {
            addButton.setImageResource(addIconResId);
        }
        if (deleteButton != null && deleteIconResId != 0) {
            deleteButton.setImageResource(deleteIconResId);
        }
    }

    /**
     * Показать индикатор загрузки, заменяя пустое место во вью-иерархии.
     */
    protected void showLoadingIndicator() {
        if (emptySpace == null || loadingIndicator != null) return;
        ViewGroup parent = (ViewGroup) emptySpace.getParent();
        if (parent == null) return;
        LayoutInflater inflater = LayoutInflater.from(this);
        loadingIndicator = (ProgressBar) inflater.inflate(R.layout.loading_indicator, parent, false);
        int index = parent.indexOfChild(emptySpace);
        parent.removeView(emptySpace);
        parent.addView(loadingIndicator, index);
    }

    /**
     * Скрыть индикатор загрузки, вернув пустое место назад.
     */
    protected void hideLoadingIndicator() {
        if (loadingIndicator == null) return;
        ViewGroup parent = (ViewGroup) loadingIndicator.getParent();
        if (parent == null) return;
        int index = parent.indexOfChild(loadingIndicator);
        parent.removeView(loadingIndicator);
        parent.addView(emptySpace, index);
        loadingIndicator = null;
    }

    /**
     * Обработчик клика «Добавить». Реализуется в наследнике.
     */
    protected abstract void onAddClicked();

    /**
     * Обработчик клика «Удалить/Режим выбора». Реализуется в наследнике.
     */
    protected abstract void onDeleteClicked();


    /** Установить режим выбора через базу (делегирует во ViewModel, если привязана) */
    protected void setSelectionMode(boolean enabled) {
        if (selectionVm != null) {
            if (enabled) selectionVm.enableSelectionMode(); else selectionVm.cancelSelectionMode();
        } else {
            isSelectionMode = enabled;
            if (isSelectionMode) updateButtonsIcons(R.drawable.ic_save, R.drawable.ic_back);
            else updateButtonsIcons(R.drawable.ic_add, R.drawable.ic_delete);
            onSelectionModeChanged(isSelectionMode);
        }
    }

    /** Переключить режим выбора */
    protected void toggleSelectionMode() { setSelectionMode(!isSelectionMode); }

    /** Узнать текущий режим выбора */
    protected boolean isInSelectionMode() { return isSelectionMode; }

    /** Хук при смене режима выбора. Наследник может отреагировать на смену UI. */
    protected void onSelectionModeChanged(boolean enabled) { /* no-op */ }

    // /**
    //  * Устанавливает заголовок тулбара
    //  * @param titleResId - ресурс строки для заголовка
    //  * @param textSizeResId - ресурс размера шрифта
    //  */
//     protected void setToolbarTitle(int titleResId, int textSizeResId) {
//         //в базовом классе 
//     }

}


