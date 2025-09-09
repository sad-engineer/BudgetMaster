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

import com.sadengineer.budgetmaster.interfaces.SelectionAdapter;
import com.sadengineer.budgetmaster.backend.interfaces.IService;

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
 * @param <C> Тип калькулятора
 * @param <F> Тип форматтера
 */
public abstract class BaseListFragmentN<T extends Serializable, A extends RecyclerView.Adapter, V extends ViewModel, S extends IService<T>, C, F> extends Fragment {
    // тег для логирования
    protected final String TAG = this.getClass().getSimpleName();

    /** Имя пользователя по умолчанию */
    /** TODO: передлать на получение имени пользователя из SharedPreferences */
    private String userName = "default_user";
    
    // переменные класса, которые используются во всех фрагментах
    protected RecyclerView recyclerView;
    protected A adapter;
    protected V viewModel;

    // --- Переменные класса, которые настраиваются в наследниках ---
    // ресурс разметки
    protected int layoutResourceId;
    // ресурс recyclerView
    protected int recyclerViewId;
    // класс viewModel 
    protected Class<V> viewModelClass;
    // класс сервиса
    protected Class<S> serviceClass;
    // класс активности для редактирования
    protected Class<?> editActivityClass;
    // номер вкладки
    protected int sourceTab;  // передает пейджер при создании фрагмента
    // Калькулятор для итоговых полей (если есть)
    private C calculator;
    // форматтер для отображения итоговых полей (если есть)
    private F formatter = new F();
    
    /**
     * Создает View для фрагмента
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(layoutResourceId, container, false);
        
        // Настраиваем RecyclerView
        recyclerView = view.findViewById(recyclerViewId);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // ViewModel из Activity
        viewModel = new ViewModelProvider(requireActivity()).get(viewModelClass);
        // Создаем адаптер
        setupAdapter();
        // Наблюдаем за режимом выбора из ViewModel
        observeSelectionMode();
        // Загружаем данные
        loadData();
        
        return view;
    }    
    
    /**
     * Настраивает адаптер с обработчиками
     */
    protected abstract void setupAdapter();

    /**
     * Наблюдает за режимом выбора
     */
    protected abstract void observeSelectionMode();

    /**
     * Загружает данные
     */
    protected void loadData() {
        Log.d(TAG, "Загрузка данных с параметрами: " + getLoadParameters());
        
        try {
            // Вызываем абстрактный метод для загрузки данных
            performDataLoading();
            
        } catch (Exception e) {
            Log.e(TAG, "Ошибка загрузки данных: " + e.getMessage(), e);
        }
    }
    
    /**
     * Возвращает параметры для загрузки данных 
     * Сюда написать контекст для логирования
     */
    protected abstract Object getLoadParameters();
    
    /**
     * Выполняет загрузку данных. Должен быть переопределен в наследниках.
     * Здесь вызывать конкретные методы кокретных сервисов
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
     * TODO: проверить на использование методов навигации
     */
    protected void goToEdit(T item) {
        Log.d(TAG, "Переход к окну редактирования");
        Intent intent = new Intent(getActivity(), editActivityClass);
        intent.putExtra("item", item);
        intent.putExtra("source_tab", sourceTab);
        startActivity(intent);
    }    
    
    /**
     * Возвращает экземпляр сервиса
     * todo: Лучше слазу передавать настроенный сервис а не создавать его здесь
     */
    protected S getServiceInstance() {
        try {
            return getServiceClass().getDeclaredConstructor(Context.class, String.class)
                    .newInstance(requireContext(), userName);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка создания сервиса: " + e.getMessage(), e);
            return null;
        }
    }
    
    
}

