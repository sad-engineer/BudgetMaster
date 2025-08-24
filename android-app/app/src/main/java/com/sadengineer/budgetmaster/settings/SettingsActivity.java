package com.sadengineer.budgetmaster.settings;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.lifecycle.LiveData;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.backend.entity.Currency;
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;
import com.sadengineer.budgetmaster.backend.service.CurrencyService;
import com.sadengineer.budgetmaster.base.BaseContentActivity;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends BaseContentActivity {

    private static final String TAG = "SettingsActivity";
    
    /** Имя пользователя по умолчанию */
    /** TODO: передлать на получение имени пользователя из SharedPreferences */
    private String userName = "default_user";

    private AppSettings appSettings;
    private LinearLayout checkboxShowPositionContainer;
    private LinearLayout checkboxShowIdContainer;
    private ImageView checkboxShowPositionIcon;
    private ImageView checkboxShowIdIcon;
    private Spinner spinnerDefaultCurrency;
    private boolean showPositionChecked = false;
    private boolean showIdChecked = false;
    private CurrencyService currencyService;
    private List<Currency> currencies = new ArrayList<>();
    private ArrayAdapter<String> currencyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Инициализация настроек
        appSettings = new AppSettings(this);

        // Инициализация сервиса валют
        currencyService = new CurrencyService(this, "settings_user");

        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButton(R.id.back_button);
        
        // Устанавливаем заголовок
        setToolbarTitle(R.string.menu_settings, R.dimen.toolbar_text);

        // Инициализация UI элементов
        initializeViews();
        
        // Загружаем валюты для спиннера
        loadCurrencies();
        
        // Загружаем текущие настройки
        loadSettings();

        Log.d(TAG, "SettingsActivity создана");
    }

    /**
     * Инициализирует UI элементы
     */
    private void initializeViews() {
        checkboxShowPositionContainer = findViewById(R.id.checkbox_show_position_container);
        checkboxShowIdContainer = findViewById(R.id.checkbox_show_id_container);
        checkboxShowPositionIcon = findViewById(R.id.checkbox_show_position_icon);
        checkboxShowIdIcon = findViewById(R.id.checkbox_show_id_icon);
        spinnerDefaultCurrency = findViewById(R.id.spinner_default_currency);

        // Настраиваем обработчики событий для чекбокса позиции
        checkboxShowPositionContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPositionChecked = !showPositionChecked;
                updateCheckboxIcon(checkboxShowPositionIcon, showPositionChecked);
                appSettings.setShowPosition(showPositionChecked);
                Log.d(TAG, "Настройка show_position изменена на: " + showPositionChecked);
            }
        });

        // Настраиваем обработчики событий для чекбокса ID
        checkboxShowIdContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showIdChecked = !showIdChecked;
                updateCheckboxIcon(checkboxShowIdIcon, showIdChecked);
                appSettings.setShowId(showIdChecked);
                Log.d(TAG, "Настройка show_id изменена на: " + showIdChecked);
            }
        });
    }
    
    /**
     * Обновляет иконку чекбокса в зависимости от состояния
     */
    private void updateCheckboxIcon(ImageView icon, boolean isChecked) {
        if (isChecked) {
            icon.setImageResource(R.drawable.ic_checkbox_checked);
        } else {
            icon.setImageResource(R.drawable.ic_checkbox_unchecked);
        }
    }

    /**
     * Загружает валюты для спиннера
     */
    private void loadCurrencies() {
        LiveData<List<Currency>> currenciesLiveData = currencyService.getAll(EntityFilter.ACTIVE);
        currenciesLiveData.observe(this, currenciesList -> {
            if (currenciesList != null) {
                currencies.clear();
                currencies.addAll(currenciesList);
                
                // Создаем список названий валют для адаптера
                List<String> currencyNames = new ArrayList<>();
                for (Currency currency : currencies) {
                    String displayName = currency.getShortName() != null ? 
                        currency.getShortName() + " - " + currency.getTitle() : 
                        currency.getTitle();
                    currencyNames.add(displayName);
                }
                
                // Создаем адаптер для спиннера
                currencyAdapter = new ArrayAdapter<>(this, 
                    android.R.layout.simple_spinner_item, currencyNames);
                currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerDefaultCurrency.setAdapter(currencyAdapter);
                
                // Устанавливаем текущую валюту по умолчанию
                setCurrentDefaultCurrency();
                
                Log.d(TAG, "Загружено валют: " + currencies.size());
            }
        });
    }
    
    /**
     * Устанавливает текущую валюту по умолчанию в спиннер
     */
    private void setCurrentDefaultCurrency() {
        int defaultCurrencyId = appSettings.getDefaultCurrencyId();
        for (int i = 0; i < currencies.size(); i++) {
            if (currencies.get(i).getId() == defaultCurrencyId) {
                spinnerDefaultCurrency.setSelection(i);
                break;
            }
        }
    }
    
    /**
     * Загружает текущие настройки в UI
     */
    private void loadSettings() {
        showPositionChecked = appSettings.isShowPosition();
        showIdChecked = appSettings.isShowId();

        updateCheckboxIcon(checkboxShowPositionIcon, showPositionChecked);
        updateCheckboxIcon(checkboxShowIdIcon, showIdChecked);

        Log.d(TAG, "Загружены настройки: show_position=" + showPositionChecked + ", show_id=" + showIdChecked);
    }

} 