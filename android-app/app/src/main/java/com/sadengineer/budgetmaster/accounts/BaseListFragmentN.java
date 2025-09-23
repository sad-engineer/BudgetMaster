package com.sadengineer.budgetmaster.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sadengineer.budgetmaster.interfaces.ISelectionAdapter;
import com.sadengineer.budgetmaster.backend.interfaces.IService;
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;
import com.sadengineer.budgetmaster.utils.LogManager;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 * Универсальный базовый фрагмент для работы с любыми сущностями.
 * Предоставляет общую функциональность для загрузки, отображения и управления списками данных.
 * 
 * @param <T> Тип сущности (должен реализовывать Serializable)
 * @param <A> Тип адаптера (должен реализовывать ISelectionAdapter<T>)
 * @param <V> Тип ViewModel (должен наследоваться от ViewModel)
 * @param <S> Тип сервиса (должен реализовывать IService<T>)
 */
public abstract class BaseListFragmentN<T extends Serializable,
        A extends RecyclerView.Adapter & ISelectionAdapter<T>,
        V extends ViewModel, S extends IService<T> > extends Fragment {
    // тег для логирования
    protected final String TAG = this.getClass().getSimpleName();

    /* Имя пользователя по умолчанию */
    /* TODO: переделать на получение имени пользователя из SharedPreferences */
    protected final String mUserName = "default_user";
    
    // переменные класса, которые используются во всех фрагментах
    protected RecyclerView mRecyclerView;
    protected A mAdapter;
    protected V mViewModel;

    // --- Переменные класса, которые настраиваются в наследниках ---
    // ресурс разметки
    protected int mLayoutResourceId;
    // ресурс recyclerView
    protected int mRecyclerViewId;
    // класс viewModel 
    protected Class<V> mViewModelClass;
    // экземпляр сервиса
    protected S mService;
    // класс активности для редактирования
    protected Class<?> mEditActivityClass;
    // номер вкладки
    protected int mSourceTab;  // передает пейджер при создании фрагмента
    
    
    /**
     * Создает View для фрагмента
     */
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(mLayoutResourceId, container, false);
        
        // Настраиваем RecyclerView
        mRecyclerView = view.findViewById(mRecyclerViewId);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // ViewModel из Activity
        mViewModel = new ViewModelProvider(requireActivity()).get(mViewModelClass);
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
        LogManager.d(TAG, "Загрузка данных с параметрами: " + getLoadParameters());
        
        try {
            // Вызываем абстрактный метод для загрузки данных
            performDataLoading();
            
        } catch (Exception e) {
            LogManager.e(TAG, "Ошибка загрузки данных: " + e.getMessage(), e);
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
        LogManager.d(TAG, "Загружено: " + (items != null ? items.size() : 0));
        
        if (items != null && !items.isEmpty()) {
            // Логируем детали каждой загруженной записи
            for (T item : items) {
                LogManager.d(TAG, "   - " + getItemTitle(item));
            }
            setAdapterData(items);
            LogManager.d(TAG, "Список данных обновлён");
        } else {
            LogManager.i(TAG, "Данные не найдены");
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
        if (mAdapter != null) {
            mAdapter.setSelectionMode(enabled);
        }
    }
    
    /**
     * Получает выбранные элементы
     */
    public List<T> getSelectedItems() {
        if (mAdapter != null) {
            return mAdapter.getSelectedItems();
        }
        return new ArrayList<>();
    }
    
    /**
     * Переходит на экран редактирования элемента
     * @param item - выбранный элемент
     */
    protected void goToEdit(T item) {
        LogManager.d(TAG, "Переход к окну редактирования элемента: " + getItemTitle(item));
        
        // Проверяем, что активность поддерживает навигацию
        if (getActivity() instanceof BaseNavigationActivity) {
            BaseNavigationActivity navActivity = (BaseNavigationActivity) getActivity();
            
            // Используем централизованную навигацию с объектом
            navActivity.goTo(mEditActivityClass, false, item, mSourceTab);
        } else {
            // выбрасываем ошибку 
            LogManager.e(TAG, "Фрагмент используется в активности, которая не поддерживает навигацию");
            throw new RuntimeException("Фрагмент используется в активности, которая не поддерживает навигацию");
        }
    }    
    
}

