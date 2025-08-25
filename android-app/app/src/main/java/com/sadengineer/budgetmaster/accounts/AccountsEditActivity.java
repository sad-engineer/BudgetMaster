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
import com.sadengineer.budgetmaster.base.BaseEditActivity;
import com.sadengineer.budgetmaster.backend.service.AccountService;
import com.sadengineer.budgetmaster.backend.entity.Account;
import com.sadengineer.budgetmaster.backend.validator.AccountValidator;
import com.sadengineer.budgetmaster.backend.service.CurrencyService;
import com.sadengineer.budgetmaster.backend.entity.Currency;
import com.sadengineer.budgetmaster.backend.constants.ModelConstants;
import com.sadengineer.budgetmaster.formatters.CurrencyAmountFormatter;

import java.util.List;
import java.util.ArrayList;

/**
 * Activity для создания/изменения счета
 */
public class AccountsEditActivity extends BaseEditActivity<Account> {
    
    private static final String TAG = "AccountsEditActivity";

    /** Имя пользователя по умолчанию */
    /** TODO: передлать на получение имени пользователя из SharedPreferences */
    private String userName = "default_user";
    
    private EditText accountNameEdit;
    private EditText accountBalanceEdit;
    private Spinner accountCurrencySpinner;
    private Spinner accountTypeSpinner;
    private CheckBox accountClosedCheckbox;
    
    // Сервисы для работы с данными
    private AccountService accountService = new AccountService(this, userName);
    private CurrencyService currencyService = new CurrencyService(this, userName);
    private AccountValidator accountValidator = new AccountValidator();
    private CurrencyAmountFormatter formatter = new CurrencyAmountFormatter();
    
    // Поля для хранения данных счета
    private Account currentAccount;
    private boolean isEditMode = false;
    private List<Currency> currencies = new ArrayList<>();
    private int sourceTab = 0; // Вкладка, с которой был вызван переход

    // Константы 
    /** Тип счета по умолчанию 1 */
    private final int DEFAULT_ACCOUNT_TYPE = ModelConstants.DEFAULT_ACCOUNT_TYPE;
    /** Баланс счета по умолчанию 0 */
    private final long DEFAULT_ACCOUNT_BALANCE = ModelConstants.DEFAULT_ACCOUNT_BALANCE;
    /** Статус счета по умолчанию 0 */
    private final int DEFAULT_ACCOUNT_STATUS_OPEN = ModelConstants.DEFAULT_ACCOUNT_STATUS_OPEN;
    /** ID валюты по умолчанию 1 */
    private final int DEFAULT_CURRENCY_ID = ModelConstants.DEFAULT_CURRENCY_ID;


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

        // Инициализация навигации
        initializeNavigation();
        setupStandardToolbar();
        // Инициализация общих действий экрана редактирования: save/cancel могут отсутствовать
        setupCommonEditActions(R.id.position_change_button);

        // Настраиваем спиннеры
        setupSpinners();
        
        // Получаем данные из Intent и заполняем поля
        loadAccountData();
        
    }
    
    /**
     * Настраивает спиннеры для валют и типов счетов
     */
    private void setupSpinners() {
        // Настройка спиннера валют
        //TODO: передлать на синхронный запрос к DAO и не использовать LiveData
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
                
                // Устанавливаем выбранную валюту ПОСЛЕ загрузки валют
                if (isEditMode && currentAccount != null) {
                    // В режиме редактирования устанавливаем валюту счета
                    setSelectedCurrencyId(currentAccount.getCurrencyId());
                } else {
                    // В режиме создания устанавливаем валюту по умолчанию
                    setSelectedCurrencyId(DEFAULT_CURRENCY_ID);
                }
            }
        });
        
        // Настройка спиннера типов счетов
        String[] accountTypes = {
            getString(R.string.tab_current), 
            getString(R.string.tab_savings), 
            getString(R.string.tab_transfers)};
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
            currentAccount = (Account) getIntent().getSerializableExtra("item");
            // Получаем информацию о вкладке
            sourceTab = getIntent().getIntExtra("source_tab", 0);
            if (currentAccount != null) {
                // Режим редактирования
                isEditMode = true;
                Log.d(TAG, "Открыто редактирование счёта. Вкладка: " + sourceTab);
                // Наполняем поля данными счета
                fillData();
                // Устанавливаем заголовок для режима редактирования
                setToolbarTitle(R.string.toolbar_title_account_edit, R.dimen.toolbar_text_account_edit);
                
            } else {
                // Режим создания нового счета
                isEditMode = false;
                Log.d(TAG, "Открыто создание нового счёта. Вкладка: " + sourceTab);
                // Устанавливаем дефолтные данные
                setDefaultData();
                // Устанавливаем заголовок для режима создания
                setToolbarTitle(
                    R.string.toolbar_title_account_add, 
                    R.dimen.toolbar_text_account_add);
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка загрузки данных счета: " + e.getMessage(), e);
            isEditMode = false;
            // Устанавливаем заголовок для режима создания по умолчанию
            setToolbarTitle(R.string.toolbar_title_account_add, R.dimen.toolbar_text_account_add);
            // Устанавливаем дефолтные данные
            setDefaultData();
        }
    }

    /** 
     * Устанавливает дефолтные данные вызове окна на создание нового счета или при ошибке загрузки данных счета 
     */
     private void setDefaultData() {
        accountBalanceEdit.setText(formatter.format(DEFAULT_ACCOUNT_BALANCE));
        accountTypeSpinner.setSelection(DEFAULT_ACCOUNT_TYPE - 1);
        // Валюта будет установлена после загрузки валют в setupSpinners()
    }

    /** 
     * Наполняет поля окна данными редактиируемого счета
     */
    private void fillData() {
         // Заполняем поля данными счета
         accountNameEdit.setText(currentAccount.getTitle());
         // Показываем сумму в рублях (копейки -> рубли)
         double amount = currentAccount.getAmount() / 100.0;
         accountBalanceEdit.setText(formatter.format(amount));
         
         // Устанавливаем тип счета
         int accountType = currentAccount.getType();
         int[] accountTypePositions = {0, 0, 1, 2}; // [0, текущий, сберегательный, кредитный]
         int position = (accountType >= 0 && accountType < accountTypePositions.length) 
             ? accountTypePositions[accountType] : 0;
         accountTypeSpinner.setSelection(position);
         
         // Устанавливаем статус закрытия
         accountClosedCheckbox.setChecked(currentAccount.getClosed() == 1);
         
        // Валюта будет установлена после загрузки валют в setupSpinners()
    }
    
    /**
     * Переопределяем настройку кнопки "назад" для перехода к списку счетов
     */
    @Override
    protected void setupBackButton(int backButtonId) {
        ImageButton back = findViewById(backButtonId);
        if (back != null) {
            back.setOnClickListener(v -> {
                Log.d(TAG, "Нажата кнопка 'Назад'");
                returnToAccounts();
            });
        }
    }
    
    /**
     * Валидирует название счета
     * @param editText поле ввода названия счета
     * @return true если название счета валидно, false если нет
     */
    private boolean validateAccountName(EditText editText) {
        //TODO: сделать проверку уникальности имени при создании счета
        String accountName = editText.getText().toString().trim();
        try {
            accountValidator.validateTitle(accountName);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Ошибка валидации названия счета: " + e.getMessage(), e);
            editText.setError(e.getMessage());
            editText.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Валидирует баланс счета
     * @param editText поле ввода баланса счета
     * @return true если баланс счета валиден, false если нет
     */
    private boolean validateAccountBalance(EditText editText) {
        String balanceText = editText.getText().toString().trim();
        if (!TextUtils.isEmpty(balanceText)) {
            try {
                // Конвертируем рубли в копейки
                String text = accountBalanceEdit.getText().toString().trim();
                Log.d(TAG, "Введено значение суммы счета: " + text);
                double amount = formatter.parseSafe(text);
                Log.d(TAG, "Конвертированное значение суммы счета: " + amount);
                long balance = Math.round(amount * 100);
                Log.d(TAG, "Конвертированное значение суммы счета в копейки: " + balance);
                accountValidator.validateAmount(balance);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Ошибка парсинга баланса: " + balanceText, e);
                Log.e(TAG, e.getMessage());
                editText.setError(e.getMessage());
                editText.requestFocus();
                return false;
            }
        }
        return true;
    }

    

    /**
     * Сохраняет данные из полей окна в счет и сохраняет его в базу данных
     * @return true если сохранение прошло успешно, false если была ошибка валидации
     */
    private boolean saveAccount() {
        // Валидация названия счета
        if (!validateAccountName(accountNameEdit)) return false;
        // Валидация баланса
        if (!validateAccountBalance(accountBalanceEdit)) return false;
        //валидация валюты не требуется, так как валюта выбирается из списка
        //валидация типа счета не требуется, так как тип счета выбирается из списка
        //валидация статуса закрытия не требуется, так как статус закрытия выбирается из чекбокса

        String accountName = accountNameEdit.getText().toString().trim();
        String text = accountBalanceEdit.getText().toString().trim();
        Log.d(TAG, "Введено значение суммы счета: " + text);
        double amount = formatter.parseSafe(text);
        Log.d(TAG, "Конвертированное значение суммы счета: " + amount);
        long balance = Math.round(amount * 100);
        Log.d(TAG, "Конвертированное значение суммы счета: " + balance);
        int accountType = getAccountTypeFromSpinner();
        int selectedCurrencyID = getSelectedCurrencyId();
        int isClosed = accountClosedCheckbox.isChecked() ? 1 : 0; // 0=open, 1=closed
        
        // Создаем счет
        try {
            if (isEditMode && currentAccount  != null) {
                Log.d(TAG, "Редактирование счёта: ID=" + currentAccount.getId());

                // Режим редактирования
                // Обновляем данные счета через сервис
                currentAccount.setTitle(accountName);
                currentAccount.setAmount(balance);
                currentAccount.setType(accountType);
                currentAccount.setCurrencyId(selectedCurrencyID);
                currentAccount.setClosed(isClosed);
                if (currentAccount.isDeleted()) {
                    Log.d(TAG, "Действие: restore");
                    accountService.restore(currentAccount);
                } else {
                    Log.d(TAG, "Действие: update");
                    accountService.update(currentAccount);
                }
                Log.d(TAG, "Запрос на обновление счета отправлен");
            } else {
                // Режим создания нового счета
                Log.d(TAG, "Создание нового счёта: " + accountName);
                accountService.createWithoutValidation(accountName, selectedCurrencyID, balance, accountType, isClosed);
                Log.d(TAG, "Запрос на создание счета: " + accountName + " отправлен");
            }
            // Возвращаемся к списку счетов
            returnToAccounts();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Критическая ошибка при сохранении счета: " + e.getMessage(), e);
            return false;
        }
    }

    // Реализация абстрактного метода базового класса. Здесь можно переиспользовать текущую логику сохранения.
    @Override
    protected boolean validateAndSave() {
        // Выполняем сохранение существующей логикой
        return saveAccount();
    }
    
    /**
     * Получает ID выбранной валюты
     */
    private Integer getSelectedCurrencyId() {
        int position = accountCurrencySpinner.getSelectedItemPosition();
        if (position >= 0 && position < currencies.size()) {
            return currencies.get(position).getId();
        }
        return DEFAULT_CURRENCY_ID; // По умолчанию RUB
    }
    
    /**
     * Устанавливает выбранную валюту по ID
     */
    private void setSelectedCurrencyId(int currencyId) {
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
        int[] accountTypes = {1, 2, 3}; // [текущий, сберегательный, кредитный]
        return (position >= 0 && position < accountTypes.length) 
            ? accountTypes[position] : DEFAULT_ACCOUNT_TYPE; // По умолчанию текущий
    }

    /**
     * Возвращается к списку счетов
     */
    private void returnToAccounts() {
        Log.d(TAG, "Переход к окну списка счетов, вкладка " + sourceTab);
        returnTo(AccountsActivity.class, "selected_tab", sourceTab);
    }
}