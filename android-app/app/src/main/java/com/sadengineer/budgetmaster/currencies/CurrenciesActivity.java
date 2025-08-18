package com.sadengineer.budgetmaster.currencies;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.lifecycle.ViewModelProvider;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.base.BaseCardsActivity;
import com.sadengineer.budgetmaster.backend.entity.Currency;

import java.util.List;
import java.util.ArrayList;


/**
 * Activity для отображения списка валют
 */
public class CurrenciesActivity extends BaseCardsActivity<Currency> {
    
    private static final String TAG = "CurrenciesActivity";
    private CurrenciesSharedViewModel viewModel;

    /**
     * Метод вызывается при создании Activity
     * @param savedInstanceState - сохраненное состояние Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currencies);
        
        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButton(R.id.back_button);
        
        // Устанавливаем заголовок
        setToolbarTitle(R.string.toolbar_title_currencies, R.dimen.toolbar_text);

        // Общая привязка кнопок и placeholder для индикатора
        setupCommonCardsUi(0, R.id.add_currency_button_bottom, R.id.delete_currency_button_bottom, R.id.toolbar_reserve);
        
        // Shared ViewModel для управления режимом выбора и мягким удалением
        viewModel = new ViewModelProvider(this).get(CurrenciesSharedViewModel.class);
        
        // Привязываем ViewModel к базовой логике кнопок/индикатора
        bindSelectionViewModel(viewModel);

        // Настраиваем список валют
        setupCurrenciesList();
    }
    
    /**
     * Обработчик клика «Добавить».
     */
    @Override
    protected void onAddClicked() {
        // Запускаем окно создания валюты (режим выбора обрабатывается базовым классом)
        Intent intent = new Intent(CurrenciesActivity.this, CurrencyEditActivity.class);
        startActivity(intent);
    }

    /**
     * Обработчик клика «Удалить/Режим выбора».
     */
    @Override
    protected void onDeleteClicked() {
        // Поведение переключения режима выбора обрабатывается базовым классом через ViewModel
    }
    
    /**
     * Настраивает список валют
     */
    private void setupCurrenciesList() {
        // Создаем и добавляем фрагмент списка валют
        CurrenciesListFragment fragment = new CurrenciesListFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.currencies_container, fragment)
                .commit();
    }
    
    /**
     * Обновляет количество выбранных элементов
     */
    public void updateSelectionCount(int count) {
        Log.d(TAG, "🔄 Выбрано элементов: " + count);
        // Можно добавить отображение количества в UI
    }
} 