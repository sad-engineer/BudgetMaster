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
    private final int DEFAULT_ACCOUNT_BALANCE = ModelConstants.DEFAULT_ACCOUNT_BALANCE;
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
        
        // Настройка обработчика поля ввода суммы
        setupAmountFieldHandler();
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
     * Сохраняет данные из полей окна в счет и сохраняет его в базу данных
     */
    private void saveAccount() {
        int balance = DEFAULT_ACCOUNT_BALANCE;

        // Валидация названия счета
        String accountName = accountNameEdit.getText().toString().trim();
        try {
            accountValidator.validateTitle(accountName);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Ошибка валидации названия счета: " + e.getMessage(), e);
            accountNameEdit.setError(e.getMessage());
            accountNameEdit.requestFocus();
            return;
        }

        // Валидация баланса
        String balanceText = accountBalanceEdit.getText().toString().trim();
        if (!TextUtils.isEmpty(balanceText)) {
            try {
                // Конвертируем рубли в копейки
                double amount = formatter.parseSafe(balanceText);
                accountValidator.validateBalance(amount);
                balance = (int) (amount * 100);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Ошибка парсинга баланса: " + balanceText, e);
                // Показываем ошибку в поле ввода
                accountBalanceEdit.setError("Неверный формат суммы. Используйте числа и запятую или точку (например: 1500,50)");
                accountBalanceEdit.requestFocus();
                return; // Прерываем сохранение
            }
        }

        // Валидация ти счета
        int accountType = getAccountTypeFromSpinner();
        try {
            accountValidator.validateType(accountType);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Ошибка валидации типа счета: " + e.getMessage(), e);
            ((TextView) accountTypeSpinner.getSelectedView()).setError(e.getMessage());
            ((TextView) accountTypeSpinner.getSelectedView()).requestFocus();
            return;
        }

        // Получаем статус закрытия
        int isClosed = accountClosedCheckbox.isChecked() ? 1 : 0; // 0=open, 1=closed
        
        // Валидация ID валюты
        Integer selectedCurrencyID = getSelectedCurrencyId();
        try {
            accountValidator.validateCurrencyID(selectedCurrencyID);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Ошибка валидации ID валюты: " + e.getMessage(), e);
            ((TextView) accountCurrencySpinner.getSelectedView()).setError(e.getMessage());
            ((TextView) accountCurrencySpinner.getSelectedView()).requestFocus();
            return;
        }

        // Создаем счет
        try {
            if (isEditMode && currentAccount  != null) {
                // Режим редактирования
                // Обновляем данные счета через сервис
                currentAccount.setTitle(accountName);
                currentAccount.setAmount(balance);
                currentAccount.setType(accountType);
                currentAccount.setCurrencyId(selectedCurrencyID);
                currentAccount.setClosed(isClosed);
                if (currentAccount.isDeleted()) {
                    Log.d(TAG, "Сохранение счёта: ID=" + currentAccount.getId() + " в режиме редактирования, действие: restore");
                    accountService.restore(currentAccount);
                } else {
                    Log.d(TAG, "Сохранение счёта: ID=" + currentAccount.getId() + " в режиме редактирования, действие: update");
                    accountService.update(currentAccount);
                }
                Log.d(TAG, "Запрос на обновление счета: ID=" + currentAccount.getId() + " в режиме редактирования отправлен");
            } else {
                // Режим создания нового счета
                Log.d(TAG, "Запрос на создание нового счета: " + accountName + " отправлен");
                // Проверяем существование счета 
                //TODO: передлать на синхронный запрос к DAO и не использовать LiveData
                Account existingAccount = accountService.getByTitle(accountName).getValue();
                if (existingAccount != null) {
                    Log.i(TAG, "Создание нового счета: " + accountName + " отменено: счёт уже существует");
                    return;
                }
                // Если счет не существует, то создаем его
                accountService.create(accountName, selectedCurrencyID, balance, accountType, isClosed);
                Log.d(TAG, "Запрос на создание счета: " + accountName + " отправлен");
            }
            // Возвращаемся к списку счетов
            returnToAccounts();
        } catch (Exception e) {
            Log.e(TAG, "Критическая ошибка при сохранении счета: " + e.getMessage(), e);
        }
    }

    // Реализация абстрактного метода базового класса. Здесь можно переиспользовать текущую логику сохранения.
    @Override
    protected boolean validateAndSave() {
        // Выполняем сохранение существующей логикой
        saveAccount();
        // Если дошли сюда, считаем, что сохранение прошло (ошибки обработаны внутри saveAccount())
        return true;
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