package com.sadengineer.budgetmaster.accounts;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.CheckBox;
import android.widget.ArrayAdapter;

import androidx.lifecycle.ViewModelProvider;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.base.BaseEditActivity;
import com.sadengineer.budgetmaster.backend.entity.Account;
import com.sadengineer.budgetmaster.backend.validator.AccountValidator;
import com.sadengineer.budgetmaster.backend.entity.Currency;
import com.sadengineer.budgetmaster.backend.constants.ModelConstants;
import com.sadengineer.budgetmaster.formatters.CurrencyAmountFormatter;
import com.sadengineer.budgetmaster.utils.LogManager;

import java.util.List;
import java.util.ArrayList;

/**
 * Activity для создания/изменения счета
 */
public class AccountsEditActivity extends BaseEditActivity<Account> {
    
    private static final String TAG = "AccountsEditActivity";

    private EditText mAccountNameEdit;
    private EditText mAccountBalanceEdit;
    private Spinner mAccountCurrencySpinner;
    private Spinner mAccountTypeSpinner;
    private CheckBox mAccountClosedCheckbox;
    
    // ViewModel для работы с данными
    private AccountsSharedViewModel mViewModel;
    private AccountValidator mAccountValidator = new AccountValidator();
    private CurrencyAmountFormatter mFormatter = new CurrencyAmountFormatter();
    
    // Поля для хранения данных счета
    private Account mCurrentAccount;
    private boolean mIsEditMode = false;
    private List<Currency> mCurrencies = new ArrayList<>();
    private int mSourceTab = 0; // Вкладка, с которой был вызван переход

    // Константы 
    /** Тип счета по умолчанию 1 */
    private final int DEFAULT_ACCOUNT_TYPE = ModelConstants.DEFAULT_ACCOUNT_TYPE;
    /** Баланс счета по умолчанию 0 */
    private final long DEFAULT_ACCOUNT_BALANCE = ModelConstants.DEFAULT_ACCOUNT_BALANCE;
    /** Статус счета по умолчанию 0 */
    private final int DEFAULT_ACCOUNT_STATUS_OPEN = ModelConstants.DEFAULT_ACCOUNT_STATUS_OPEN;
    /** ID валюты по умолчанию 1 */
    private final int DEFAULT_ACCOUNT_CURRENCY_ID = ModelConstants.DEFAULT_ACCOUNT_CURRENCY_ID;


    /**
     * Метод вызывается при создании Activity
     * @param savedInstanceState - сохраненное состояние Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_edit);

        // Инициализация всех View элементов
        mAccountNameEdit = findViewById(R.id.account_name_edit_text);
        mAccountBalanceEdit = findViewById(R.id.account_balance_edit_text);
        mAccountCurrencySpinner = findViewById(R.id.account_currency_spinner);
        mAccountTypeSpinner = findViewById(R.id.account_type_spinner);
        mAccountClosedCheckbox = findViewById(R.id.account_closed_checkbox);

        // Инициализация ViewModel
        mViewModel = new ViewModelProvider(this).get(AccountsSharedViewModel.class);
        
        // Инициализация навигации
        initializeNavigation();
        // Инициализация общих действий экрана редактирования: save/cancel могут отсутствовать
        setupCommonEditActions(R.id.position_change_button);
        
        // Настройка кнопки "Назад"
        setupBackButton(R.id.back_button);

        // Настраиваем спиннеры
        setupSpinners();
        
        // Настраиваем наблюдение за ViewModel
        setupViewModelObservers();
        
        // Получаем данные из Intent и заполняем поля
        loadAccountData();
        
    }
    
    /**
     * Настраивает спиннеры для валют и типов счетов
     */
    private void setupSpinners() {
        // Загружаем валюты через ViewModel
        mViewModel.loadCurrencies();
        
        // Настройка спиннера типов счетов
        String[] accountTypes = {
            getString(R.string.tab_current), 
            getString(R.string.tab_savings), 
            getString(R.string.tab_transfers)};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, accountTypes);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAccountTypeSpinner.setAdapter(typeAdapter);

        // Добавляем обработчики для убирания фокуса с поля суммы
        mAccountCurrencySpinner.setOnTouchListener((v, event) -> {
            mAccountBalanceEdit.clearFocus();
            return false; // Позволяем спиннеру обрабатывать событие дальше
        });
    
        mAccountTypeSpinner.setOnTouchListener((v, event) -> {
            mAccountBalanceEdit.clearFocus();
            return false; // Позволяем спиннеру обрабатывать событие дальше
        });
    }
    
    /**
     * Настраивает наблюдение за ViewModel
     */
    private void setupViewModelObservers() {
        // Наблюдаем за валютой
        mViewModel.getCurrencies().observe(this, currencies -> {
            if (currencies != null && !currencies.isEmpty()) {
                setupCurrencySpinner(currencies);
            }
        });
        
        // Наблюдаем за текущим счетом
        mViewModel.getCurrentAccount().observe(this, account -> {
            if (account != null) {
                mCurrentAccount = account;
                fillData();
            }
        });
        
        // Наблюдаем за состоянием сохранения
        mViewModel.getSaving().observe(this, isSaving -> {
            if (isSaving != null && isSaving) {
                // Показать индикатор загрузки
                LogManager.d(TAG, "Сохранение счета...");
            }
        });
        
        // Наблюдаем за результатом сохранения
        mViewModel.getAccountSaved().observe(this, isSaved -> {
            if (isSaved != null && isSaved) {
                LogManager.d(TAG, "Счет успешно сохранен");
                returnToAccounts();
            }
        });
    }
    
    /**
     * Настраивает спиннер валют
     */
    private void setupCurrencySpinner(List<Currency> currencies) {
        // Создаем массив названий валют
        String[] currencyTitles = new String[currencies.size()];
        for (int i = 0; i < currencies.size(); i++) {
            currencyTitles[i] = currencies.get(i).getTitle();
        }
        
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, currencyTitles);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAccountCurrencySpinner.setAdapter(currencyAdapter);
        
        // Сохраняем список валют для получения ID
        this.mCurrencies = currencies;
        
        // Устанавливаем выбранную валюту ПОСЛЕ загрузки валют
        if (mIsEditMode && mCurrentAccount != null) {
            // В режиме редактирования устанавливаем валюту счета
            setSelectedCurrencyId(mCurrentAccount.getCurrencyId());
        } else {
            // В режиме создания устанавливаем валюту по умолчанию
            setSelectedCurrencyId(DEFAULT_ACCOUNT_CURRENCY_ID);
        }
    }
    
    /**
     * Загружает данные счета из Intent и заполняет поля
     */
    @SuppressWarnings("deprecation") 
    private void loadAccountData() {
        try {
            // Получаем счет из Intent
            mCurrentAccount = (Account) getIntent().getSerializableExtra("item");
            // Получаем информацию о вкладке
            mSourceTab = getIntent().getIntExtra("source_tab", 0);
            if (mCurrentAccount != null) {
                // Режим редактирования
                mIsEditMode = true;
                LogManager.d(TAG, "Открыто редактирование счёта. Вкладка: " + mSourceTab);
                // Загружаем счет через ViewModel
                mViewModel.loadAccountForEdit(mCurrentAccount.getId());
                // Устанавливаем заголовок для режима редактирования
                setToolbarTitle(R.string.toolbar_title_account_edit, R.dimen.toolbar_text_account_edit);
                
            } else {
                // Режим создания нового счета
                mIsEditMode = false;
                LogManager.d(TAG, "Открыто создание нового счёта. Вкладка: " + mSourceTab);
                // Устанавливаем дефолтные данные
                setDefaultData();
                // Устанавливаем заголовок для режима создания
                setToolbarTitle(
                    R.string.toolbar_title_account_add, 
                    R.dimen.toolbar_text_account_add);
            }
        } catch (Exception e) {
            LogManager.e(TAG, "Ошибка загрузки данных счета: " + e.getMessage(), e);
            mIsEditMode = false;
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
        // Показываем сумму в рублях (копейки -> рубли)
        mAccountBalanceEdit.setText(mFormatter.formatFromCents(DEFAULT_ACCOUNT_BALANCE));
        mAccountTypeSpinner.setSelection(DEFAULT_ACCOUNT_TYPE - 1);
        // Валюта будет установлена после загрузки валют в setupSpinners()
    }

    /** 
     * Наполняет поля окна данными редактиируемого счета
     */
    private void fillData() {
         // Заполняем поля данными счета
         mAccountNameEdit.setText(mCurrentAccount.getTitle());
         // Показываем сумму в рублях (копейки -> рубли)
         mAccountBalanceEdit.setText(mFormatter.formatFromCents(mCurrentAccount.getAmount()));
         
         // Устанавливаем тип счета
         int accountType = mCurrentAccount.getType();
         int[] accountTypePositions = {0, 0, 1, 2}; // [0, текущий, сберегательный, кредитный]
         int position = (accountType >= 0 && accountType < accountTypePositions.length) 
             ? accountTypePositions[accountType] : 0;
         mAccountTypeSpinner.setSelection(position);
         
         // Устанавливаем статус закрытия
         mAccountClosedCheckbox.setChecked(mCurrentAccount.getClosed() == 1);
         
        // Валюта будет установлена после загрузки валют в setupSpinners()
    }
    
    /**
     * Переопределяем настройку кнопки "назад" для перехода к списку счетов
     */
    protected void setupBackButton(int backButtonId) {
        ImageButton back = findViewById(backButtonId);
        if (back != null) {
            back.setOnClickListener(v -> {
                LogManager.d(TAG, "Нажата кнопка 'Назад'");
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
        String accountName = editText.getText().toString().trim();
        try {
            mAccountValidator.validateTitle(accountName);
        } catch (IllegalArgumentException e) {
            LogManager.e(TAG, "Ошибка валидации названия счета: " + e.getMessage(), e);
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
                String text = mAccountBalanceEdit.getText().toString().trim();
                LogManager.d(TAG, "Введено значение суммы счета: " + text);
                double amount = mFormatter.parseSafe(text);
                LogManager.d(TAG, "Конвертированное значение суммы счета: " + amount);
                long balance = Math.round(amount * 100);
                LogManager.d(TAG, "Конвертированное значение суммы счета в копейки: " + balance);
                mAccountValidator.validateAmount(balance);
            } catch (IllegalArgumentException e) {
                LogManager.e(TAG, "Ошибка парсинга баланса: " + balanceText, e);
                LogManager.e(TAG, e.getMessage());
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
        if (!validateAccountName(mAccountNameEdit)) return false;
        // Валидация баланса
        if (!validateAccountBalance(mAccountBalanceEdit)) return false;
        //валидация валюты не требуется, так как валюта выбирается из списка
        //валидация типа счета не требуется, так как тип счета выбирается из списка
        //валидация статуса закрытия не требуется, так как статус закрытия выбирается из чекбокса

        String accountName = mAccountNameEdit.getText().toString().trim();
        String text = mAccountBalanceEdit.getText().toString().trim();
        LogManager.d(TAG, "Введено значение суммы счета: " + text);
        double amount = mFormatter.parseSafe(text);
        LogManager.d(TAG, "Конвертированное значение суммы счета: " + amount);
        long balance = Math.round(amount * 100);
        LogManager.d(TAG, "Конвертированное значение суммы счета: " + balance);
        int accountType = getAccountTypeFromSpinner();
        int selectedCurrencyID = getSelectedCurrencyId();
        int isClosed = mAccountClosedCheckbox.isChecked() ? 1 : 0; // 0=open, 1=closed
        
        // Создаем счет через ViewModel
        try {
            if (mIsEditMode && mCurrentAccount != null) {
                LogManager.d(TAG, "Редактирование счёта: ID=" + mCurrentAccount.getId());
                // Режим редактирования - обновляем данные счета
                mCurrentAccount.setTitle(accountName);
                mCurrentAccount.setAmount(balance);
                mCurrentAccount.setType(accountType);
                mCurrentAccount.setCurrencyId(selectedCurrencyID);
                mCurrentAccount.setClosed(isClosed);
                // Сохраняем через ViewModel
                mViewModel.saveAccount(mCurrentAccount);
            } else {
                // Режим создания нового счета
                LogManager.d(TAG, "Создание нового счёта: " + accountName);
                Account newAccount = new Account();
                newAccount.setTitle(accountName);
                newAccount.setAmount(balance);
                newAccount.setType(accountType);
                newAccount.setCurrencyId(selectedCurrencyID);
                newAccount.setClosed(isClosed);
                // Сохраняем через ViewModel
                mViewModel.saveAccount(newAccount);
            }
            return true;
        } catch (Exception e) {
            LogManager.e(TAG, "Критическая ошибка при сохранении счета: " + e.getMessage(), e);
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
        int position = mAccountCurrencySpinner.getSelectedItemPosition();
        if (position >= 0 && position < mCurrencies.size()) {
            return mCurrencies.get(position).getId();
        }
        return DEFAULT_ACCOUNT_CURRENCY_ID; // По умолчанию RUB
    }
    
    /**
     * Устанавливает выбранную валюту по ID
     */
    private void setSelectedCurrencyId(int currencyId) {
        for (int i = 0; i < mCurrencies.size(); i++) {
            if (mCurrencies.get(i).getId() == currencyId) {
                mAccountCurrencySpinner.setSelection(i);
                break;
            }
        }
    }
    
    /**
     * Получает тип счета из спиннера
     */
    private int getAccountTypeFromSpinner() {
        int position = mAccountTypeSpinner.getSelectedItemPosition();
        int[] accountTypes = {1, 2, 3}; // [текущий, сберегательный, кредитный]
        return (position >= 0 && position < accountTypes.length) 
            ? accountTypes[position] : DEFAULT_ACCOUNT_TYPE; // По умолчанию текущий
    }

    /**
     * Возвращается к списку счетов
     */
    private void returnToAccounts() {
        LogManager.d(TAG, "Переход к окну списка счетов, вкладка " + mSourceTab);
        // Очищаем данные редактирования в ViewModel
        mViewModel.clearEditData();
        String[] params = {"selected_tab", String.valueOf(mSourceTab)};
        returnTo(AccountsActivity.class, true, params);
    }
}