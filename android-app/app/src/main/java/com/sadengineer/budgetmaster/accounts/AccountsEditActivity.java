package com.sadengineer.budgetmaster.accounts;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.CheckBox;
import android.widget.ArrayAdapter;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;
import com.sadengineer.budgetmaster.backend.service.AccountService;
import com.sadengineer.budgetmaster.backend.entity.Account;
import com.sadengineer.budgetmaster.backend.validator.AccountValidator;
import com.sadengineer.budgetmaster.backend.service.CurrencyService;
import com.sadengineer.budgetmaster.backend.entity.Currency;
import com.sadengineer.budgetmaster.formatters.CurrencyAmountFormatter;

import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.ArrayList;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Activity для создания/изменения счета
 */
public class AccountsEditActivity extends BaseNavigationActivity {
    
    private static final String TAG = "AccountsEditActivity";
    
    private EditText accountNameEdit;
    private EditText accountBalanceEdit;
    private Spinner accountCurrencySpinner;
    private Spinner accountTypeSpinner;
    private CheckBox accountClosedCheckbox;
    private ImageButton saveButton;
    private ImageButton backButton;
    private ImageButton menuButton;
    private AccountService accountService;
    private CurrencyService currencyService;
    private AccountValidator accountValidator;
    private CurrencyAmountFormatter formatter;
    
    // Поля для хранения данных счета
    private Account currentAccount;
    private boolean isEditMode = false;
    private List<Currency> currencies = new ArrayList<>();
    private int sourceTab = 0; // Вкладка, с которой был вызван переход
    
    /**
     * Метод вызывается при создании Activity
     * @param savedInstanceState - сохраненное состояние Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_edit);

        // Инициализация всех View элементов
        accountNameEdit = findViewById(R.id.account_name_edit_text);
        accountBalanceEdit = findViewById(R.id.account_balance_edit_text);
        accountCurrencySpinner = findViewById(R.id.account_currency_spinner);
        accountTypeSpinner = findViewById(R.id.account_type_spinner);
        accountClosedCheckbox = findViewById(R.id.account_closed_checkbox);
        saveButton = findViewById(R.id.save_button);
        backButton = findViewById(R.id.back_button);
        menuButton = findViewById(R.id.menu_button);

        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButton(R.id.back_button);

        // Инициализация сервисов
        accountService = new AccountService(this, "default_user");
        currencyService = new CurrencyService(this, "default_user");
        
        // Инициализация форматтера для суммы счета
        formatter = new CurrencyAmountFormatter();
        
        // Настраиваем спиннеры
        setupSpinners();
        
        // Получаем данные из Intent и заполняем поля
        loadAccountData();
        
        // Обработчики кнопок
        setupButtonHandlers();
        
        // Настройка обработчика поля ввода суммы
        setupAmountFieldHandler();
    }
    
    /**
     * Настраивает спиннеры для валют и типов счетов
     */
    private void setupSpinners() {
        // Настройка спиннера валют
        currencyService.getAll().observe(this, currencies -> {
            if (currencies != null && !currencies.isEmpty()) {
                // Создаем массив названий валют
                String[] currencyTitles = new String[currencies.size()];
                for (int i = 0; i < currencies.size(); i++) {
                    currencyTitles[i] = currencies.get(i).getTitle();
                }
                
                ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(this, 
                    android.R.layout.simple_spinner_item, currencyTitles);
                currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                accountCurrencySpinner.setAdapter(currencyAdapter);
                
                // Сохраняем список валют для получения ID
                this.currencies = currencies;
            }
        });
        
        // Настройка спиннера типов счетов
        String[] accountTypes = {"Текущий", "Сберегательный", "Перевод"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, accountTypes);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accountTypeSpinner.setAdapter(typeAdapter);

        // Добавляем обработчики для убирания фокуса с поля суммы
        accountCurrencySpinner.setOnTouchListener((v, event) -> {
            accountBalanceEdit.clearFocus();
            return false; // Позволяем спиннеру обрабатывать событие дальше
        });
        
        accountTypeSpinner.setOnTouchListener((v, event) -> {
            accountBalanceEdit.clearFocus();
            return false; // Позволяем спиннеру обрабатывать событие дальше
        });
    }
    
    /**
     * Загружает данные счета из Intent и заполняет поля
     */
    @SuppressWarnings("deprecation") 
    private void loadAccountData() {
        try {
            // Получаем счет из Intent
            currentAccount = (Account) getIntent().getSerializableExtra("account");
            
            // Получаем информацию о вкладке
            sourceTab = getIntent().getIntExtra("source_tab", 0);
            Log.d(TAG, "Открыто редактирование счёта. Вкладка: " + sourceTab);
            
            if (currentAccount != null) {
                // Режим редактирования
                isEditMode = true;
                Log.d(TAG, "Режим редактирования счёта");
                
                // Заполняем поля данными счета
                accountNameEdit.setText(currentAccount.getTitle());
                // Показываем сумму в рублях (копейки -> рубли)
                double amount = currentAccount.getAmount() / 100.0;
                accountBalanceEdit.setText(formatter.format(amount));
                
                // Устанавливаем тип счета
                int accountType = currentAccount.getType();
                int[] accountTypePositions = {0, 0, 1, 2}; // [0, текущий, сберегательный, перевод]
                int position = (accountType >= 0 && accountType < accountTypePositions.length) 
                    ? accountTypePositions[accountType] : 0;
                accountTypeSpinner.setSelection(position);
                
                // Устанавливаем статус закрытия
                accountClosedCheckbox.setChecked(currentAccount.getClosed() == 1);
                
                // Устанавливаем валюту (будет установлена после загрузки валют)
                setSelectedCurrency(currentAccount.getCurrencyId());
                
                // Устанавливаем заголовок для режима редактирования
                setToolbarTitle(R.string.toolbar_title_account_edit, R.dimen.toolbar_text_account_edit);
                
            } else {
                // Режим создания нового счета
                isEditMode = false;
                Log.d(TAG, "Режим создания нового счета");
                
                // Устанавливаем заголовок для режима создания
                setToolbarTitle(R.string.toolbar_title_account_add, R.dimen.toolbar_text_account_add);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Ошибка загрузки данных счета: " + e.getMessage(), e);
            isEditMode = false;
            
            // Устанавливаем заголовок для режима создания по умолчанию
            setToolbarTitle(R.string.toolbar_title_account_add, R.dimen.toolbar_text_account_add);
        }
    }
    
    /**
     * Настраивает обработчик поля ввода суммы
     */
    private void setupAmountFieldHandler() {
        accountBalanceEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // При получении фокуса убираем форматирование
                String currentText = accountBalanceEdit.getText().toString();
                if (!TextUtils.isEmpty(currentText)) {
                    // Используем новый метод для преобразования в простой текст
                    String plainText = formatter.toPlainText(currentText);
                    accountBalanceEdit.setText(plainText);
                    // Устанавливаем курсор в конец текста
                    accountBalanceEdit.setSelection(plainText.length());
                }
            } else {
                // При потере фокуса форматируем обратно
                String currentText = accountBalanceEdit.getText().toString();
                if (!TextUtils.isEmpty(currentText)) {
                    try {
                        // Парсим число и форматируем через форматтер
                        double amount = formatter.parseSafe(currentText);
                        accountBalanceEdit.setText(formatter.format(amount));
                    } catch (Exception e) {
                        // Если не удалось распарсить, оставляем как есть
                    }
                }
            }
        });
    }
    
    /**
     * Настраивает обработчики кнопок
     */
    private void setupButtonHandlers() {
        // Кнопка сохранения
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Нажата кнопка 'Сохранить'");
                saveAccount();
            }
        });
    }
    
    /**
     * Переопределяем настройку кнопки "назад" для перехода к списку счетов
     */
    @Override
    protected void setupBackButton(int backButtonId) {
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                Log.d(TAG, "Нажата кнопка 'Назад'");
                // Возвращаемся к списку счетов
                returnToAccounts();
            });
        }
    }
    
    /**
     * Сохраняет счет в базу данных
     */
    private void saveAccount() {
        String accountName = accountNameEdit.getText().toString().trim();
        String balanceText = accountBalanceEdit.getText().toString().trim();
        
        // Валидация названия счета
        // TODO: Валидация названия счета
        
        // Валидация баланса
        int balance = 0;
        if (!TextUtils.isEmpty(balanceText)) {
            try {
                // Конвертируем рубли в копейки
                double amount = formatter.parseSafe(balanceText);
                balance = (int) (amount * 100);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Ошибка парсинга баланса: " + balanceText, e);
                // Показываем ошибку в поле ввода
                accountBalanceEdit.setError("Неверный формат суммы. Используйте числа и запятую или точку (например: 1500,50)");
                accountBalanceEdit.requestFocus();
                return; // Прерываем сохранение
            }
        }
        
        
        // Получаем выбранные значения из спиннеров
        int selectedCurrencyId = getSelectedCurrencyId();
        int accountType = getAccountTypeFromSpinner();
        int isClosed = accountClosedCheckbox.isChecked() ? 1 : 0; // 0=open, 1=closed

        try {
            if (isEditMode && currentAccount != null) {
                // Режим редактирования
                Log.d(TAG, "Нажата кнопка 'Сохранить'");
                
                // Обновляем данные счета через сервис
                currentAccount.setTitle(accountName);
                currentAccount.setAmount(balance);
                currentAccount.setType(accountType);
                currentAccount.setCurrencyId(selectedCurrencyId);
                currentAccount.setClosed(isClosed);

                if (currentAccount.isDeleted()) {
                    Log.d(TAG, "Сохранение счёта: ID=" + currentAccount.getId() + ", действие: restore");
                    accountService.restore(currentAccount);
                } else {
                    Log.d(TAG, "Сохранение счёта: ID=" + currentAccount.getId() + ", действие: update");
                    accountService.update(currentAccount);
                }
                
                Log.d(TAG, "Запрос на обновление счета отправлен");
                
            } else {
                // Режим создания нового счета
                Log.d(TAG, "Нажата кнопка 'Сохранить'");

                // Проверяем существование счета
                Account existingAccount = accountService.getByTitle(accountName).getValue();
                if (existingAccount != null) {
                    Log.i(TAG, "Создание отменено: счёт уже существует");
                    return;
                }
                // Если счет не существует, то создаем его
                accountService.create(accountName, selectedCurrencyId, balance, accountType, isClosed);
                
                Log.d(TAG, "Запрос на создание счета отправлен");
            }
            
            // Возвращаемся к списку счетов
            returnToAccounts();

        } catch (Exception e) {
            Log.e(TAG, "Критическая ошибка при сохранении счета: " + e.getMessage(), e);
        }
    }
    
    /**
     * Получает ID выбранной валюты
     */
    private int getSelectedCurrencyId() {
        int position = accountCurrencySpinner.getSelectedItemPosition();
        if (position >= 0 && position < currencies.size()) {
            return currencies.get(position).getId();
        }
        return 1; // По умолчанию RUB
    }
    
    /**
     * Устанавливает выбранную валюту по ID
     */
    private void setSelectedCurrency(int currencyId) {
        for (int i = 0; i < currencies.size(); i++) {
            if (currencies.get(i).getId() == currencyId) {
                accountCurrencySpinner.setSelection(i);
                break;
            }
        }
    }
    
    /**
     * Получает тип счета из спиннера
     */
    private int getAccountTypeFromSpinner() {
        int position = accountTypeSpinner.getSelectedItemPosition();
        int[] accountTypes = {1, 2, 3}; // [текущий, сберегательный, перевод]
        return (position >= 0 && position < accountTypes.length) 
            ? accountTypes[position] : 1; // По умолчанию текущий
    }
    
    /**
     * Устанавливает заголовок тулбара
     * @param titleResId - ресурс строки для заголовка
     * @param textSizeResId - ресурс размера шрифта
     */
    private void setToolbarTitle(int titleResId, int textSizeResId) {
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        if (toolbarTitle != null) {
            toolbarTitle.setText(titleResId);
                Log.d(TAG, "Заголовок тулбара установлен");
            
            // Устанавливаем размер шрифта
            float textSize = getResources().getDimension(textSizeResId);
            toolbarTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                Log.d(TAG, "Размер шрифта установлен");
        }
    }

    /**
     * Возвращается к списку счетов
     */
    private void returnToAccounts() {
        // Переходим к списку счетов
        Log.d(TAG, "Переход к окну списка счетов, вкладка " + sourceTab);
        Intent intent = new Intent(this, AccountsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("selected_tab", sourceTab);
        startActivity(intent);
        finish();
    }   
}