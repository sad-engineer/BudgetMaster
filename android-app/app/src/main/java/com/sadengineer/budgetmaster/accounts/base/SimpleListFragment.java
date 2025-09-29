package com.sadengineer.budgetmaster.accounts.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sadengineer.budgetmaster.base.BaseNavigationActivity;
import com.sadengineer.budgetmaster.utils.LogManager;

import java.io.Serializable;

/**
 * Упрощенный базовый фрагмент для списков
 * Только 2 generic параметра вместо 4
 * 
 * @param <T> Тип данных (должен реализовывать Serializable)
 * @param <V> Тип ViewModel (должен наследоваться от androidx.lifecycle.ViewModel)
 */
public abstract class SimpleListFragment<T extends Serializable, V extends androidx.lifecycle.ViewModel> extends Fragment {
    
    // Тег для логирования
    protected final String TAG = this.getClass().getSimpleName();
    
    // UI компоненты
    protected RecyclerView recyclerView;
    protected RecyclerView.Adapter<?> adapter;
    
    // ViewModel для данных
    protected V viewModel;
    
    /**
     * Создает View для фрагмента
     */
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        
        View view = inflater.inflate(getLayoutResourceId(), container, false);
        
        // Инициализация UI компонентов
        initializeViews(view);
        
        // Инициализация ViewModel
        initializeViewModel();
        
        // Настройка компонентов
        setupRecyclerView();
        setupAdapter();
        
        // Загрузка данных
        loadData();
        
        return view;
    }
    
    /**
     * Инициализация View компонентов
     */
    protected void initializeViews(View view) {
        recyclerView = view.findViewById(getRecyclerViewId());
        if (recyclerView == null) {
            throw new IllegalStateException("RecyclerView не найден. Проверьте getRecyclerViewId()");
        }
    }
    
    /**
     * Инициализация ViewModel
     */
    protected void initializeViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(getViewModelClass());
        LogManager.d(TAG, "ViewModel инициализирован: " + viewModel.getClass().getSimpleName());
    }
    
    /**
     * Настройка RecyclerView
     */
    protected void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        LogManager.d(TAG, "RecyclerView настроен");
    }
    
    /**
     * Навигация к редактированию элемента
     * Сохраняем BaseNavigationActivity как стандартную навигацию
     */
    protected void goToEdit(T item, Class<?> editActivityClass) {
        LogManager.d(TAG, "Переход к редактированию: " + getItemTitle(item));
        
        if (getActivity() instanceof BaseNavigationActivity) {
            BaseNavigationActivity navActivity = (BaseNavigationActivity) getActivity();
            navActivity.goTo(editActivityClass, false, item, getSourceTab());
        } else {
            LogManager.e(TAG, "Activity не поддерживает BaseNavigationActivity");
        }
    }
    
    /**
     * Навигация к созданию нового элемента
     */
    protected void navigateToCreate(Class<?> editActivityClass) {
        LogManager.d(TAG, "Переход к созданию нового элемента");
        
        if (getActivity() instanceof BaseNavigationActivity) {
            BaseNavigationActivity navActivity = (BaseNavigationActivity) getActivity();
            navActivity.goTo(editActivityClass, false, null, getSourceTab());
        } else {
            LogManager.e(TAG, "Activity не поддерживает BaseNavigationActivity");
        }
    }
    
    /**
     * Получает номер исходной вкладки
     */
    protected int getSourceTab() {
        return getArguments() != null ? getArguments().getInt("source_tab", 0) : 0;
    }
    
    /**
     * Возвращает заголовок элемента для логирования
     */
    protected String getItemTitle(T item) {
        return item != null ? item.toString() : "null";
    }
    
    // Абстрактные методы для реализации в наследниках
    
    /**
     * Возвращает ID layout ресурса
     */
    protected abstract int getLayoutResourceId();
    
    /**
     * Возвращает ID RecyclerView
     */
    protected abstract int getRecyclerViewId();
    
    /**
     * Возвращает класс ViewModel
     */
    protected abstract Class<V> getViewModelClass();
    
    /**
     * Настраивает адаптер
     */
    protected abstract void setupAdapter();
    
    /**
     * Загружает данные
     */
    protected abstract void loadData();
}
