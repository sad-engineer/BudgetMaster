package com.sadengineer.budgetmaster.currencies;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.base.BaseCardsActivity;
import com.sadengineer.budgetmaster.backend.entity.Currency;
import com.sadengineer.budgetmaster.backend.database.BudgetMasterDatabase;
import com.sadengineer.budgetmaster.backend.service.CurrencyService;

import java.util.List;


/**
 * Activity для отображения списка валют
 */
public class CurrenciesActivity extends BaseCardsActivity<Currency> implements CurrencyAdapter.OnCurrencyClickListener {
    
    private static final String TAG = "CurrenciesActivity";
    
    private CurrencyAdapter adapter;
    private CurrencyService currencyService;

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
        setToolbarTitle(R.string.toolbar_title_currencies, R.dimen.toolbar_text_currencies);

        // Инициализация CurrencyService
        currencyService = new CurrencyService(this, "default_user");

        // Настройка общих элементов UI
        setupCommonCardsUi(
            R.id.currencies_recycler_view,  // recyclerViewId
            R.id.add_currency_button_bottom, // addButtonId
            R.id.delete_currency_button_bottom, // deleteButtonId
            null // emptySpaceId - нет пустого места в layout
        );

        // Инициализация RecyclerView
        setupRecyclerView();
        
        // Загружаем валюты из базы данных
        loadCurrenciesFromDatabase();
    }
    
    /**
     * Настраивает RecyclerView
     */
    private void setupRecyclerView() {
        adapter = new CurrencyAdapter(this);
        
        // Настраиваем обработчик длительного нажатия
        adapter.setLongClickListener(new CurrencyAdapter.OnCurrencyLongClickListener() {
            @Override
            public void onCurrencyLongClick(Currency currency) {
                Log.d(TAG, "Длительное нажатие на валюту: " + currency.getTitle());
                showDeleteConfirmationDialog(currency);
            }
        });
        
        setupRecycler(adapter, new LinearLayoutManager(this));
    }
    
    /**
     * Показывает диалог подтверждения удаления валюты
     */
    private void showDeleteConfirmationDialog(Currency currency) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Удаление валюты")
               .setMessage("Вы уверены, что хотите полностью удалить валюту '" + currency.getTitle() + "'?\n\n" +
                          "⚠️ Это действие нельзя отменить!")
               .setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       deleteCurrency(currency);
                   }
               })
               .setNegativeButton("Отмена", null)
               .setIcon(android.R.drawable.ic_dialog_alert)
               .show();
    }
    
    /**
     * Удаляет валюту из базы данных
     */
    private void deleteCurrency(Currency currency) {
        try {
            Log.d(TAG, "🗑️ Удаляем валюту из базы данных: " + currency.getTitle());
            currencyService.delete(false, currency);
            Log.d(TAG, "✅ Запрос на удаление валюты отправлен: " + currency.getTitle());
        } catch (Exception e) {
            Log.e(TAG, "❌ Ошибка удаления валюты " + currency.getTitle() + ": " + e.getMessage(), e);
        }
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
            database.currencyDao().getAll().observe(this, currencies -> {
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
        if (!isInSelectionMode()) {
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
     * Обработчик клика «Добавить». Реализация для BaseCardsActivity.
     */
    @Override
    protected void onAddClicked() {
        Log.d(TAG, "➕ Нажата кнопка 'Добавить валюту'");
        Intent intent = new Intent(CurrenciesActivity.this, CurrencyEditActivity.class);
        startActivity(intent);
    }

    /**
     * Обработчик клика «Удалить/Режим выбора». Реализация для BaseCardsActivity.
     */
    @Override
    protected void onDeleteClicked() {
        Log.d(TAG, "🗑️ Нажата кнопка 'Удалить/Режим выбора'");
        toggleSelectionMode();
    }

    /**
     * Переопределяем для обработки удаления выбранных валют
     */
    @Override
    protected void onSelectionModeChanged(boolean enabled) {
        if (enabled) {
            adapter.setSelectionMode(true);
            Log.d(TAG, "✅ Режим выбора валют включен");
        } else {
            adapter.setSelectionMode(false);
            adapter.clearSelection();
            Log.d(TAG, "❌ Режим выбора валют отменен");
        }
    }
} 