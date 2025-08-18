package com.sadengineer.budgetmaster.base;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.AlertDialog;
import android.content.DialogInterface;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 * Универсальный базовый фрагмент для работы с любыми сущностями.
 * Предоставляет общую функциональность для загрузки, отображения и управления списками данных.
 * 
 * @param <T> Тип сущности (должен реализовывать Serializable)
 * @param <A> Тип адаптера
 * @param <V> Тип ViewModel (должен наследоваться от ViewModel)
 * @param <S> Тип сервиса
 */
public abstract class BaseListFragment<T extends Serializable, A extends RecyclerView.Adapter, V extends ViewModel, S> extends Fragment {
    
    protected final String TAG = getClass().getSimpleName();
    protected RecyclerView recyclerView;
    protected A adapter;
    protected V viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutResourceId(), container, false);
        
        // Настраиваем RecyclerView
        recyclerView = view.findViewById(getRecyclerViewId());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // ViewModel из Activity
        viewModel = new ViewModelProvider(requireActivity()).get(getViewModelClass());

        // Создаем адаптер
        setupAdapter();

        // Наблюдаем за режимом выбора из ViewModel
        observeSelectionMode();
        
        // Загружаем данные
        loadData();
        
        return view;
    }
    
    /**
     * Возвращает ID layout ресурса для фрагмента
     */
    protected abstract int getLayoutResourceId();
    
    /**
     * Возвращает ID RecyclerView в layout
     */
    protected abstract int getRecyclerViewId();
    
    /**
     * Возвращает класс ViewModel
     */
    protected abstract Class<V> getViewModelClass();
    
    /**
     * Возвращает класс сервиса для работы с сущностью
     */
    protected abstract Class<S> getServiceClass();
    
    /**
     * Возвращает параметры для загрузки данных
     */
    protected abstract Object getLoadParameters();
    
    /**
     * Возвращает source_tab для передачи в EditActivity
     */
    protected abstract int getSourceTab();
    
    /**
     * Возвращает true, если нужно загружать только активные записи
     */
    protected boolean loadOnlyActiveItems() {
        return false;
    }
    
    /**
     * Загружает данные
     */
    protected void loadData() {
        Log.d(TAG, "Загрузка данных с параметрами: " + getLoadParameters());
        
        try {
            // Вызываем абстрактный метод для загрузки данных
            performDataLoading();
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Ошибка загрузки данных: " + e.getMessage(), e);
        }
    }
    
    /**
     * Выполняет загрузку данных. Должен быть переопределен в наследниках.
     */
    protected abstract void performDataLoading();
    
    /**
     * Обрабатывает загруженные данные
     */
    protected void handleDataLoaded(List<T> items) {
        Log.d(TAG, "Загружено: " + (items != null ? items.size() : 0));
        
        if (items != null && !items.isEmpty()) {
            // Логируем детали каждой загруженной записи
            for (T item : items) {
                Log.d(TAG, "   - " + getItemTitle(item));
            }
            
            setAdapterData(items);
            Log.d(TAG, "Список данных обновлён");
            
            // Сбрасываем счетчик свайпов при изменении содержимого списка
            if (getActivity() instanceof BaseNavigationActivity) {
                ((BaseNavigationActivity) getActivity()).resetSwipeCount();
            }
        } else {
            Log.i(TAG, "Данные не найдены");
        }
    }
    
    /**
     * Возвращает заголовок элемента для логирования
     */
    protected String getItemTitle(T item) {
        return item.toString();
    }
    
    /**
     * Устанавливает данные в адаптер
     */
    protected abstract void setAdapterData(List<T> items);
    
    /**
     * Устанавливает режим выбора
     */
    public void setSelectionMode(boolean enabled) {
        if (adapter != null && adapter instanceof SelectionAdapter) {
            ((SelectionAdapter) adapter).setSelectionMode(enabled);
        }
    }
    
    /**
     * Получает выбранные элементы
     */
    public List<T> getSelectedItems() {
        if (adapter != null && adapter instanceof SelectionAdapter) {
            return ((SelectionAdapter<T>) adapter).getSelectedItems();
        }
        return new ArrayList<>();
    }
    
    /**
     * Переходит на экран редактирования элемента
     * @param item - выбранный элемент
     */
    protected void goToEdit(T item) {
        Log.d(TAG, "Переход к окну редактирования");
        Intent intent = new Intent(getActivity(), getEditActivityClass());
        intent.putExtra("item", item);
        intent.putExtra("source_tab", getSourceTab());
        startActivity(intent);
    }
    
    /**
     * Возвращает класс активности для редактирования
     */
    protected abstract Class<?> getEditActivityClass();
    
    /**
     * Наблюдает за режимом выбора
     */
    protected abstract void observeSelectionMode();

    /**
     * Настраивает адаптер с обработчиками
     */
    protected abstract void setupAdapter();

    /**
     * Показывает диалог подтверждения удаления элемента
     */
    protected void showDeleteConfirmationDialog(T item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Удаление")
               .setMessage("Вы уверены, что хотите полностью удалить '" + getItemTitle(item) + "'?\n\n" +
                          "⚠️ Это действие нельзя отменить!")
               .setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       deleteItem(item);
                   }
               })
               .setNegativeButton("Отмена", null)
               .setIcon(android.R.drawable.ic_dialog_alert)
               .show();
    }

    /**
     * Удаляет элемент из базы данных
     */
    protected void deleteItem(T item) {
        try {
            Log.d(TAG, "Удаление элемента: " + getItemTitle(item));
            
            S service = getServiceInstance();
            if (service != null) {
                performDelete(service, item);
            }
            
            Log.d(TAG, "Запрос на удаление элемента отправлен");
            
        } catch (Exception e) {
            Log.e(TAG, "Ошибка удаления элемента " + getItemTitle(item) + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Выполняет удаление элемента через сервис
     */
    protected abstract void performDelete(S service, T item);
    
    /**
     * Возвращает экземпляр сервиса
     */
    protected S getServiceInstance() {
        try {
            return getServiceClass().getDeclaredConstructor(Context.class, String.class)
                    .newInstance(requireContext(), "default_user");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка создания сервиса: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Интерфейс для адаптеров с поддержкой выбора
     */
    public interface SelectionAdapter<T> {
        void setSelectionMode(boolean enabled);
        List<T> getSelectedItems();
    }
}

