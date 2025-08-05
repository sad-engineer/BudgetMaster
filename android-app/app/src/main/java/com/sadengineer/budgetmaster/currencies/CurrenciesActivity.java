package com.sadengineer.budgetmaster.currencies;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;
import com.sadengineer.budgetmaster.backend.entity.Currency;
import com.sadengineer.budgetmaster.backend.database.BudgetMasterDatabase;
import com.sadengineer.budgetmaster.backend.service.CurrencyService;

import java.util.List;


/**
 * Activity для отображения списка валют
 */
public class CurrenciesActivity extends BaseNavigationActivity implements CurrencyAdapter.OnCurrencyClickListener, CurrencyAdapter.OnSelectionChangedListener {
    
    private static final String TAG = "CurrenciesActivity";
    
    private RecyclerView recyclerView;
    private CurrencyAdapter adapter;
    private ImageButton addCurrencyButton;
    private ImageButton deleteCurrencyButton;
    private CurrencyService currencyService;
    private boolean isSelectionMode = false;

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

        // Инициализация CurrencyService
        currencyService = new CurrencyService(this, "default_user");

        // Инициализация RecyclerView
        setupRecyclerView();
        
        // Загружаем валюты из базы данных
        loadCurrenciesFromDatabase();

        // Обработчики кнопок валют
        setupButtons();
    }
    
    /**
     * Настраивает кнопки
     */
    private void setupButtons() {
        addCurrencyButton = findViewById(R.id.add_currency_button_bottom);
        deleteCurrencyButton = findViewById(R.id.delete_currency_button_bottom);

        /**
         * Обработчик нажатия на кнопку добавления валюты
         */
        addCurrencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSelectionMode) {
                    // В режиме выбора - удаляем выбранные валюты
                    deleteSelectedCurrencies();
                } else {
                    // Запускаем окно создания валюты
                    Intent intent = new Intent(CurrenciesActivity.this, CurrencyEditActivity.class);
                    startActivity(intent);
                }
            }
        });

        /**
         * Обработчик нажатия на кнопку удаления валют
         */
        deleteCurrencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSelectionMode) {
                    // В режиме выбора - отменяем выбор
                    cancelSelectionMode();
                } else {
                    // Включаем режим выбора
                    enableSelectionMode();
                }
            }
        });
    }
    
    /**
     * Включает режим выбора валют
     */
    private void enableSelectionMode() {
        isSelectionMode = true;
        
        // Меняем иконки кнопок
        addCurrencyButton.setImageResource(R.drawable.ic_save);
        deleteCurrencyButton.setImageResource(R.drawable.ic_back);
        
        // Небольшая задержка для плавного перехода
        recyclerView.postDelayed(() -> {
            adapter.setSelectionMode(true);
            adapter.setSelectionListener(this);
            Log.d(TAG, "✅ Режим выбора валют включен");
        }, 100);
    }
    
    /**
     * Отменяет режим выбора
     */
    private void cancelSelectionMode() {
        isSelectionMode = false;
        adapter.setSelectionMode(false);
        adapter.clearSelection();
        
        // Возвращаем иконки кнопок
        addCurrencyButton.setImageResource(R.drawable.ic_add);
        deleteCurrencyButton.setImageResource(R.drawable.ic_delete);
        
        Log.d(TAG, "❌ Режим выбора валют отменен");
    }
    
    /**
     * Удаляет выбранные валюты
     */
    private void deleteSelectedCurrencies() {
        List<Currency> selectedCurrencies = adapter.getSelectedCurrencies();
        
        Log.d(TAG, "🗑️ Удаляем выбранные валюты: " + selectedCurrencies.size());
        
        // Удаляем валюты из базы данных
        for (Currency currency : selectedCurrencies) {
            try {
                currencyService.softDelete(currency);
                Log.d(TAG, "✅ Удалена валюта: " + currency.getTitle());
            } catch (Exception e) {
                Log.e(TAG, "❌ Ошибка удаления валюты " + currency.getTitle() + ": " + e.getMessage(), e);
            }
        }
        
        // Отменяем режим выбора
        cancelSelectionMode();
        Log.d(TAG, "✅ Удалено валют: " + selectedCurrencies.size());
    }
    
    /**
     * Настраивает RecyclerView
     */
    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.currencies_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new CurrencyAdapter(this);
        recyclerView.setAdapter(adapter);
    }
    
    /**
     * Загружает валюты из базы данных
     */
    private void loadCurrenciesFromDatabase() {
        Log.d(TAG, "🔄 Загружаем валюты из базы данных...");
        
        try {
            // Получаем базу данных (уже инициализирована в MainActivity)
            BudgetMasterDatabase database = BudgetMasterDatabase.getDatabase(this);
            
            // Загружаем валюты через Observer
            database.currencyDao().getAllActive().observe(this, currencies -> {
                Log.d(TAG, "✅ Загружено валют: " + (currencies != null ? currencies.size() : 0));
                
                if (currencies != null && !currencies.isEmpty()) {
                    adapter.setCurrencies(currencies);
                    Log.d(TAG, "✅ Валюты отображены в списке");
                } else {
                    Log.w(TAG, "⚠️ Валюты не найдены в базе данных");
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Ошибка загрузки валют: " + e.getMessage(), e);
        }
    }
    
    /**
     * Обрабатывает нажатие на валюту
     * @param currency - выбранная валюта
     */
    @Override
    public void onCurrencyClick(Currency currency) {
        if (!isSelectionMode) {
            Log.d(TAG, "👆 Выбрана валюта: " + currency.getTitle() + " (ID: " + currency.getId() + ")");
            // Переходим на экран редактирования валюты
            goToCurrencyEdit(currency);
        }
    }

    /**
     * Переходит на экран редактирования валюты
     * @param currency - выбранная валюта
     */
    private void goToCurrencyEdit(Currency currency) {
        Log.d(TAG, "🔄 Переходим к окну редактирования валюты");
        Intent intent = new Intent(CurrenciesActivity.this, CurrencyEditActivity.class);
        intent.putExtra("currency", currency);
        startActivity(intent);
    }

    /**
     * Обрабатывает изменения в количестве выбранных валют
     * @param selectedCount - количество выбранных валют
     */
    @Override
    public void onSelectionChanged(int selectedCount) {
        Log.d(TAG, "📊 Выбрано валют: " + selectedCount);
    }
} 