package com.sadengineer.budgetmaster.operations;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
 
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import androidx.core.content.ContextCompat;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.base.BaseEditActivity;
import com.sadengineer.budgetmaster.backend.service.OperationService;
import com.sadengineer.budgetmaster.backend.service.CategoryService;
import com.sadengineer.budgetmaster.backend.service.AccountService;
import com.sadengineer.budgetmaster.backend.entity.Operation;
import com.sadengineer.budgetmaster.backend.entity.Category;
import com.sadengineer.budgetmaster.backend.entity.Account;
import com.sadengineer.budgetmaster.backend.validator.OperationValidator;
import com.sadengineer.budgetmaster.backend.filters.OperationTypeFilter;
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;
import com.sadengineer.budgetmaster.formatters.CurrencyAmountFormatter;
import com.sadengineer.budgetmaster.backend.constants.ModelConstants;
import com.sadengineer.budgetmaster.utils.LogManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Activity для создания/изменения операций (доходы/расходы)
 */
public class OperationEditActivity extends BaseEditActivity<Operation> {
    
    private static final String TAG = "OperationEditActivity";

    /** Имя пользователя по умолчанию */
    /** TODO: передлать на получение имени пользователя из SharedPreferences */
    private String userName = "default_user";
    
    // View элементы
    private Spinner categorySpinner;
    private Spinner accountSpinner;
    private EditText amountEdit;
    private EditText dateEdit;
    private EditText commentEdit;
    
    // Сервисы для работы с данными
    private OperationService operationService;
    private CategoryService categoryService;
    private AccountService accountService;
    private OperationValidator validator = new OperationValidator();
    private CurrencyAmountFormatter formatter = new CurrencyAmountFormatter();
    
    // Поля для хранения данных операции
    private Operation currentOperation;
    private boolean isEditMode = false;
    private int operationType = OperationTypeFilter.INCOME.getIndex(); // По умолчанию доход
    private int sourceTab = 0; // Вкладка, с которой был вызван переход
    private List<Category> categories = new ArrayList<>();
    private List<Account> accounts = new ArrayList<>();
    private LocalDateTime selectedDate = LocalDateTime.now();
    private ModelConstants constants = new ModelConstants();
    
    // Переменная для хранения ссылки на GridLayout календаря
    private GridLayout currentCalendarGrid = null;
    
    // Переменная для хранения выбранной даты в календаре (отдельно от отображаемого месяца)
    private LocalDateTime selectedDateInCalendar = null;

    // Константы
    private long DEFAULT_AMOUNT = ModelConstants.DEFAULT_AMOUNT;
    
    // Форматтер для даты
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    
    /**
     * Метод вызывается при создании Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operation_edit);

        // Инициализация всех View элементов
        categorySpinner = findViewById(R.id.operation_category_spinner);
        accountSpinner = findViewById(R.id.operation_account_spinner);
        amountEdit = findViewById(R.id.operation_amount_edit_text);
        dateEdit = findViewById(R.id.operation_date_edit_text);
        commentEdit = findViewById(R.id.operation_comment_edit_text);

        // Инициализация навигации
        initializeNavigation();
        
        // Инициализация общих действий экрана редактирования
        setupCommonEditActions(R.id.position_change_button);

        // Настройка кнопки "Назад"
        setupBackButton(R.id.back_button);

        // Инициализация сервисов
        operationService = new OperationService(this, userName);
        categoryService = new CategoryService(this, userName);
        accountService = new AccountService(this, userName);
        
        // Получаем данные из Intent и заполняем поля
        loadOperationData();
        
        // Настраиваем спиннеры
        setupSpinners();
        
        // Настраиваем обработчики событий
        setupEventHandlers();
    }
    
    /**
     * Настраивает спиннеры для категорий и счетов
     */
    private void setupSpinners() {
        // Загружаем категории в зависимости от типа операции
        loadCategories();
        
        // Загружаем все активные счета
        loadAccounts();
    }
    
    /**
     * Загружает категории в зависимости от типа операции
     */
    private void loadCategories() {
        categoryService.getAllByOperationType(operationType, EntityFilter.ACTIVE).observe(this, categories -> {
            if (categories != null && !categories.isEmpty()) {
                this.categories = categories;
                
                // Создаем массив названий категорий
                String[] categoryTitles = new String[categories.size()];
                for (int i = 0; i < categories.size(); i++) {
                    categoryTitles[i] = categories.get(i).getTitle();
                }
                
                ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, 
                    android.R.layout.simple_spinner_item, categoryTitles);
                categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categorySpinner.setAdapter(categoryAdapter);
                
                LogManager.d(TAG, "Спиннер категорий настроен: " + categories.size() + " категорий");
            }
        });
    }
    
    /**
     * Загружает все активные счета
     */
    private void loadAccounts() {
        accountService.getAll(EntityFilter.ACTIVE).observe(this, accounts -> {
            if (accounts != null && !accounts.isEmpty()) {
                this.accounts = accounts;
                
                // Создаем массив названий счетов
                String[] accountTitles = new String[accounts.size()];
                for (int i = 0; i < accounts.size(); i++) {
                    accountTitles[i] = accounts.get(i).getTitle();
                }
                
                ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(this, 
                    android.R.layout.simple_spinner_item, accountTitles);
                accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                accountSpinner.setAdapter(accountAdapter);
                
                LogManager.d(TAG, "Спиннер счетов настроен: " + accounts.size() + " счетов");
                
                // Если есть операция для редактирования, устанавливаем выбранный счет
                if (currentOperation != null && isEditMode) {
                    setSelectedAccount(currentOperation.getAccountId());
                }
            }
        });
    }
    
    /**
     * Настраивает обработчики событий
     */
    private void setupEventHandlers() {
        // Обработчик клика на поле даты - вызываем виджет выбора даты
        dateEdit.setOnClickListener(v -> {
            // Скрываем клавиатуру и показываем виджет выбора даты
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(dateEdit.getWindowToken(), 0);
            }
            showDatePickerDialog();
            // Устанавливаем курсор в конец текста для возможности ручного редактирования
            dateEdit.setSelection(dateEdit.getText().length());
        });
        
        // Обработчик фокуса на поле даты - устанавливаем курсор в конец и показываем виджет
        dateEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // Устанавливаем курсор в конец текста при получении фокуса
                dateEdit.setSelection(dateEdit.getText().length());
                // Показываем виджет выбора даты при получении фокуса
                showDatePickerDialog();
            }
        });
        
        // Обработчик изменения текста в поле даты для ручного ввода
        dateEdit.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Ничего не делаем
            }
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Ничего не делаем
            }
            
            @Override
            public void afterTextChanged(android.text.Editable s) {
                // Пытаемся распарсить введенную дату
                String dateText = s.toString().trim();
                if (!dateText.isEmpty()) {
                    try {
                        LocalDateTime parsedDate = LocalDateTime.parse(dateText, dateFormatter);
                        selectedDate = parsedDate;
                        LogManager.d(TAG, "Дата введена вручную: " + dateText);
                    } catch (Exception e) {
                        LogManager.d(TAG, "Не удалось распарсить дату: " + dateText);
                        // Не очищаем поле, позволяем пользователю исправить
                    }
                }
            }
        });
        
        // Обработчик изменения счета для автоматического определения валюты
        accountSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                // Валюта определяется автоматически из выбранного счета
                LogManager.d(TAG, "Выбран счет: " + (position >= 0 && position < accounts.size() ? accounts.get(position).getTitle() : "неизвестно"));
            }
            
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Ничего не делаем
            }
        });
    }
    
    /**
     * Показывает диалог выбора даты
     */
    private void showDatePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_date_picker, null);
        
        // Инициализируем элементы диалога
        TextView titleText = dialogView.findViewById(R.id.date_picker_title);
        TextView monthYearText = dialogView.findViewById(R.id.tv_month_year);
        ImageButton prevButton = dialogView.findViewById(R.id.btn_prev_month);
        ImageButton nextButton = dialogView.findViewById(R.id.btn_next_month);
        GridLayout calendarGrid = dialogView.findViewById(R.id.calendar_grid);
        TextView cancelButton = dialogView.findViewById(R.id.btn_cancel);
        TextView okButton = dialogView.findViewById(R.id.btn_ok);
        
        // Инициализируем выбранную дату в календаре текущей выбранной датой
        selectedDateInCalendar = selectedDate;
        
        // Устанавливаем текущую дату
        updateDatePickerTitle(titleText, selectedDate);
        updateMonthYearText(monthYearText, selectedDate);
        
        // Сохраняем ссылку на GridLayout и создаем календарь
        currentCalendarGrid = calendarGrid;
        createCalendarGrid(calendarGrid, selectedDate, titleText);
        
        // Обработчики кнопок навигации
        prevButton.setOnClickListener(v -> {
            LocalDateTime newDisplayDate = selectedDate.minusMonths(1);
            updateMonthYearText(monthYearText, newDisplayDate);
            createCalendarGrid(calendarGrid, newDisplayDate, titleText);
        });
        
        nextButton.setOnClickListener(v -> {
            LocalDateTime newDisplayDate = selectedDate.plusMonths(1);
            updateMonthYearText(monthYearText, newDisplayDate);
            createCalendarGrid(calendarGrid, newDisplayDate, titleText);
        });
        
        // Создаем диалог
        AlertDialog dialog = builder.setView(dialogView).create();
        
        // Обработчики кнопок
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        okButton.setOnClickListener(v -> {
            updateDateField();
            dialog.dismiss();
            // Устанавливаем фокус на поле даты для возможности ручного редактирования
            dateEdit.requestFocus();
            // Устанавливаем курсор в конец текста
            dateEdit.setSelection(dateEdit.getText().length());
        });

        dialog.show();
    }
    
    /**
     * Обновляет заголовок диалога с днем недели и датой
     */
    private void updateDatePickerTitle(TextView titleText, LocalDateTime date) {
        String formattedDate = date.format(dateFormatter);
        String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
        titleText.setText(dayOfWeek + " " + formattedDate);
    }
    
    /**
     * Обновляет текст месяца и года
     */
    private void updateMonthYearText(TextView monthYearText, LocalDateTime date) {
        String monthName = date.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
        int year = date.getYear();
        monthYearText.setText(monthName + " " + year);
    }
    
    /**
     * Создает сетку календаря
     */
    private void createCalendarGrid(GridLayout grid, LocalDateTime date, TextView titleText) {
        grid.removeAllViews();
        
        LocalDateTime firstDayOfMonth = date.withDayOfMonth(1);
        int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue(); // 1 = понедельник, 7 = воскресенье
        int daysInMonth = date.toLocalDate().lengthOfMonth();
        
        // Добавляем дни предыдущего месяца в начале
        LocalDateTime prevMonth = date.minusMonths(1);
        int daysInPrevMonth = prevMonth.toLocalDate().lengthOfMonth();
        for (int i = dayOfWeek - 1; i > 0; i--) {
            int day = daysInPrevMonth - i + 1;
            addPreviousMonthDayCell(grid, day, prevMonth.withDayOfMonth(day), titleText);
        }
        
        // Добавляем дни текущего месяца
        for (int day = 1; day <= daysInMonth; day++) {
            addDayCell(grid, day, date.withDayOfMonth(day), titleText);
        }
        
        // Добавляем дни следующего месяца в конце
        LocalDateTime nextMonth = date.plusMonths(1);
        int totalCells = 42; // 6 строк * 7 столбцов
        int remainingCells = totalCells - (dayOfWeek - 1) - daysInMonth;
        for (int day = 1; day <= remainingCells; day++) {
            addNextMonthDayCell(grid, day, nextMonth.withDayOfMonth(day), titleText);
        }
    }
    
    /**
     * Добавляет ячейку дня предыдущего месяца
     */
    private void addPreviousMonthDayCell(GridLayout grid, int day, LocalDateTime date, TextView titleText) {
        TextView dayView = new TextView(this);
        dayView.setText(String.valueOf(day));
        dayView.setTextSize(14);
        dayView.setTextColor(ContextCompat.getColor(this, R.color.date_picker_content_texts));
        dayView.setAlpha(0.3f); // Прозрачность для дней предыдущего месяца
        dayView.setPadding(8, 8, 8, 8);
        dayView.setGravity(android.view.Gravity.CENTER);
        dayView.setTag(date); // Сохраняем дату в теге
        
        // Выделяем текущую выбранную дату
        if (date.toLocalDate().equals(selectedDateInCalendar != null ? selectedDateInCalendar.toLocalDate() : selectedDate.toLocalDate())) {
            dayView.setBackgroundColor(ContextCompat.getColor(this, R.color.date_picker_selected_day_background));
            dayView.setTextColor(ContextCompat.getColor(this, R.color.date_picker_content_titles));
            dayView.setAlpha(1.0f); // Полная непрозрачность для выбранной даты
        }
        
        // Обработчик клика
        dayView.setOnClickListener(v -> {
            selectedDate = date;
            selectedDateInCalendar = date;
            updateDateField();
            // Обновляем заголовок с датой и днем недели
            updateDatePickerTitle(titleText, date);
            // Обновляем выделение в календаре
            updateCalendarSelection();
            // Устанавливаем курсор в конец текста для возможности ручного редактирования
            dateEdit.setSelection(dateEdit.getText().length());
        });
        
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        dayView.setLayoutParams(params);
        
        grid.addView(dayView);
    }
    
    /**
     * Добавляет ячейку дня следующего месяца
     */
    private void addNextMonthDayCell(GridLayout grid, int day, LocalDateTime date, TextView titleText) {
        TextView dayView = new TextView(this);
        dayView.setText(String.valueOf(day));
        dayView.setTextSize(14);
        dayView.setPadding(8, 8, 8, 8);
        dayView.setGravity(android.view.Gravity.CENTER);
        dayView.setTag(date); // Сохраняем дату в теге
        
        // Выделяем текущую выбранную дату
        if (date.toLocalDate().equals(selectedDateInCalendar != null ? selectedDateInCalendar.toLocalDate() : selectedDate.toLocalDate())) {
            dayView.setBackgroundColor(ContextCompat.getColor(this, R.color.date_picker_selected_day_background));
            dayView.setTextColor(ContextCompat.getColor(this, R.color.date_picker_content_titles));
            dayView.setAlpha(1.0f); // Полная непрозрачность для выбранной даты
        } else {
            dayView.setTextColor(ContextCompat.getColor(this, R.color.date_picker_content_texts));
            dayView.setAlpha(0.3f); // Прозрачность для дней следующего месяца
        }
        
        // Обработчик клика
        dayView.setOnClickListener(v -> {
            selectedDate = date;
            selectedDateInCalendar = date;
            updateDateField();
            // Обновляем заголовок с датой и днем недели
            updateDatePickerTitle(titleText, date);
            // Обновляем выделение в календаре
            updateCalendarSelection();
            // Устанавливаем курсор в конец текста для возможности ручного редактирования
            dateEdit.setSelection(dateEdit.getText().length());
        });
        
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        dayView.setLayoutParams(params);
        
        grid.addView(dayView);
    }
    
    /**
     * Добавляет ячейку дня
     */
    private void addDayCell(GridLayout grid, int day, LocalDateTime date, TextView titleText) {
        TextView dayView = new TextView(this);
        dayView.setText(String.valueOf(day));
        dayView.setTextSize(14);
        dayView.setTextColor(ContextCompat.getColor(this, R.color.date_picker_content_texts));
        dayView.setPadding(8, 8, 8, 8);
        dayView.setGravity(android.view.Gravity.CENTER);
        dayView.setTag(date); // Сохраняем дату в теге
        
        // Выделяем текущую выбранную дату
        if (date.toLocalDate().equals(selectedDateInCalendar != null ? selectedDateInCalendar.toLocalDate() : selectedDate.toLocalDate())) {
            dayView.setBackgroundColor(ContextCompat.getColor(this, R.color.date_picker_selected_day_background));
            dayView.setTextColor(ContextCompat.getColor(this, R.color.date_picker_content_titles));
            dayView.setAlpha(1.0f); // Полная непрозрачность для выбранной даты
        }
        
        // Обработчик клика
        dayView.setOnClickListener(v -> {
            selectedDate = date;
            selectedDateInCalendar = date;
            updateDateField();
            // Обновляем заголовок с датой и днем недели
            updateDatePickerTitle(titleText, date);
            // Обновляем выделение в календаре
            updateCalendarSelection();
            // Устанавливаем курсор в конец текста для возможности ручного редактирования
            dateEdit.setSelection(dateEdit.getText().length());
        });
        
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        dayView.setLayoutParams(params);
        
        grid.addView(dayView);
    }
    
    /**
     * Обновляет поле даты
     */
    private void updateDateField() {
        String formattedDate = selectedDate.format(dateFormatter);
        dateEdit.setText(formattedDate);
        LogManager.d(TAG, "Выбрана дата: " + formattedDate);
    }
    
    /**
     * Обновляет выделение в календаре
     */
    private void updateCalendarSelection() {
        if (currentCalendarGrid == null) {
            return;
        }
        
        // Проходим по всем дочерним элементам GridLayout
        for (int i = 0; i < currentCalendarGrid.getChildCount(); i++) {
            View child = currentCalendarGrid.getChildAt(i);
            if (child instanceof TextView) {
                TextView dayView = (TextView) child;
                
                // Получаем дату из тега (если есть)
                Object tag = dayView.getTag();
                if (tag instanceof LocalDateTime) {
                    LocalDateTime date = (LocalDateTime) tag;
                    
                    if (date.toLocalDate().equals(selectedDateInCalendar != null ? selectedDateInCalendar.toLocalDate() : selectedDate.toLocalDate())) {
                        // Выделяем выбранную дату
                        dayView.setBackgroundColor(ContextCompat.getColor(this, R.color.date_picker_selected_day_background));
                        dayView.setTextColor(ContextCompat.getColor(this, R.color.date_picker_content_titles));
                        dayView.setAlpha(1.0f);
                    } else {
                        // Сбрасываем выделение
                        dayView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                        
                        // Определяем тип дня и устанавливаем соответствующие цвета
                        if (date.getMonth() == selectedDate.getMonth() && date.getYear() == selectedDate.getYear()) {
                            // День текущего месяца
                            dayView.setTextColor(ContextCompat.getColor(this, R.color.date_picker_content_texts));
                            dayView.setAlpha(1.0f);
                        } else {
                            // День предыдущего или следующего месяца
                            dayView.setTextColor(ContextCompat.getColor(this, R.color.date_picker_content_texts));
                            dayView.setAlpha(0.3f);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Загружает данные операции из Intent
     */
    @SuppressWarnings("deprecation") 
    private void loadOperationData() {
        Intent intent = getIntent();
        if (intent != null) {
            // Получаем тип операции из Intent
            operationType = intent.getIntExtra("operation_type", OperationTypeFilter.INCOME.getIndex());
            LogManager.d(TAG, "Загружен тип операции из Intent: " + operationType);
            
            // Получаем информацию о вкладке
            sourceTab = intent.getIntExtra("source_tab", 0);
            LogManager.d(TAG, "Загружена вкладка из Intent: " + sourceTab);
            
            // Получаем операцию для редактирования
            currentOperation = (Operation) intent.getSerializableExtra("operation");
            
            if (currentOperation != null) {
                isEditMode = true;
                LogManager.d(TAG, "Режим редактирования операции ID: " + currentOperation.getId());
                
                // Устанавливаем заголовок для режима редактирования
                setToolbarTitle(R.string.toolbar_title_operation_edit, R.dimen.toolbar_text);
                
                // Загружаем актуальные данные из базы
                operationService.getById(currentOperation.getId()).observe(this, loadedOperation -> {
                    if (loadedOperation != null) {
                        currentOperation = loadedOperation;
                        fillOperationData();
                        LogManager.d(TAG, "Данные операции загружены из базы");
                    } else {
                        LogManager.e(TAG, "Операция с ID " + currentOperation.getId() + " не найдена");
                        finish();
                    }
                });
            } else {
                isEditMode = false;
                LogManager.d(TAG, "Режим создания новой операции");
                
                // Устанавливаем заголовок для режима создания
                setToolbarTitle(R.string.toolbar_title_operation_add, R.dimen.toolbar_text);
                
                // Устанавливаем дефолтные данные
                setDefaultData();
            }
        } else {
            LogManager.e(TAG, "Intent равен null");
            finish();
        }
    }
    
    /**
     * Устанавливает дефолтные данные для новой операции
     */
    private void setDefaultData() {
        // Устанавливаем текущую дату
        selectedDate = LocalDateTime.now();
        updateDateField();
        
        // Устанавливаем сумму по умолчанию (0.00)
        amountEdit.setText(formatter.formatFromCents(ModelConstants.DEFAULT_AMOUNT));
        
        // Очищаем комментарий
        commentEdit.setText("");
    }
    
    /**
     * Заполняет поля данными операции
     */
    private void fillOperationData() {
        if (currentOperation != null) {
            LogManager.d(TAG, "fillOperationData: загружаем операцию ID=" + currentOperation.getId() + 
                      ", сумма в копейках=" + currentOperation.getAmount());
            
            // Устанавливаем сумму
            amountEdit.setText(formatter.formatFromCents(currentOperation.getAmount()));
            
            // Устанавливаем дату
            selectedDate = currentOperation.getOperationDate();
            updateDateField();
            
            // Устанавливаем комментарий
            commentEdit.setText(currentOperation.getDescription());
            
            // Устанавливаем выбранную категорию
            setSelectedCategory(currentOperation.getCategoryId());
            
            // Счет будет установлен в loadAccounts() после загрузки счетов
            
            LogManager.d(TAG, "Данные операции загружены в поля");
        } else {
            LogManager.w(TAG, "fillOperationData: currentOperation равен null");
        }
    }
    
    /**
     * Устанавливает выбранную категорию по ID
     */
    private void setSelectedCategory(int categoryId) {
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getId() == categoryId) {
                categorySpinner.setSelection(i);
                break;
            }
        }
    }
    
    /**
     * Устанавливает выбранный счет по ID
     */
    private void setSelectedAccount(int accountId) {
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getId() == accountId) {
                accountSpinner.setSelection(i);
                break;
            }
        }
    }
    
    /**
     * Реализация абстрактного метода для валидации и сохранения
     */
    @Override
    protected boolean validateAndSave() {
        return saveOperation();
    }
    
    /**
     * Сохраняет операцию
     */
    private boolean saveOperation() {
        LogManager.d(TAG, "Сохранение операции...");
        
        // Валидация категории
        if (categorySpinner.getSelectedItemPosition() == -1 || categorySpinner.getSelectedItemPosition() >= categories.size()) {
            LogManager.e(TAG, "Не выбрана категория");
            return false;
        }
        
        // Валидация счета
        if (accountSpinner.getSelectedItemPosition() == -1 || accountSpinner.getSelectedItemPosition() >= accounts.size()) {
            LogManager.e(TAG, "Не выбран счет");
            return false;
        }
        
        // Валидация суммы
        String amountText = amountEdit.getText().toString().trim();
        if (TextUtils.isEmpty(amountText)) {
            LogManager.e(TAG, "Не введена сумма");
            return false;
        }
        
        long amount;
        try {
            double amountDouble = formatter.parseSafe(amountText);
            amount = Math.round(amountDouble * 100); // Конвертируем в копейки
            validator.validateAmount(amount);
        } catch (IllegalArgumentException e) {
            LogManager.e(TAG, "Ошибка валидации суммы: " + e.getMessage(), e);
            amountEdit.setError(e.getMessage());
            amountEdit.requestFocus();
            return false;
        }
        
        // Валидация даты
        if (selectedDate == null) {
            LogManager.e(TAG, "Не выбрана дата");
            return false;
        }
        
        try {
            // Получаем выбранные данные
            Category selectedCategory = categories.get(categorySpinner.getSelectedItemPosition());
            Account selectedAccount = accounts.get(accountSpinner.getSelectedItemPosition());
            String comment = commentEdit.getText().toString().trim();
            
            if (isEditMode) {
                // Обновляем существующую операцию
                return updateOperation(amount, selectedCategory.getId(), selectedAccount.getId(), comment);
            } else {
                // Создаем новую операцию
                return createOperation(amount, selectedCategory.getId(), selectedAccount.getId(), comment);
            }
            
        } catch (Exception e) {
            LogManager.e(TAG, "Ошибка при сохранении операции: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Создает новую операцию
     */
    private boolean createOperation(long amount, int categoryId, int accountId, String comment) {
        try {
            LogManager.d(TAG, "Создание новой операции: тип=" + operationType + 
                      ", сумма=" + amount + " копеек (" + (amount / 100.0) + " рублей)" +
                      ", категория=" + categoryId + ", счет=" + accountId);
            
            //TODO: Получение ID валюты из выбранного счета
            // метод сервиса для получения валюты счета
            int currencyId = getCurrencyIdFromAccount(accountId);
            
            operationService.createWithoutValidation(operationType, selectedDate, amount, comment, categoryId, accountId, currencyId);
            
            LogManager.d(TAG, "Операция создана успешно");
            returnToPrevious();
            return true;
        } catch (Exception e) {
            LogManager.e(TAG, "Ошибка при создании операции: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Обновляет существующую операцию
     */
    private boolean updateOperation(long amount, int categoryId, int accountId, String comment) {
        if (currentOperation == null) {
            LogManager.e(TAG, "currentOperation равен null, невозможно обновить операцию");
            return false;
        }
        
        try {
            LogManager.d(TAG, "Обновление операции ID: " + currentOperation.getId() + 
                      ", новая сумма: " + amount + " копеек (" + (amount / 100.0) + " рублей)" +
                      ", новая категория: " + categoryId + ", новый счет: " + accountId);
            
            currentOperation.setAmount(amount);
            currentOperation.setCategoryId(categoryId);
            currentOperation.setAccountId(accountId);
            currentOperation.setOperationDate(selectedDate);
            currentOperation.setDescription(comment);
            // Валюта определяется автоматически из счета
            currentOperation.setCurrencyId(getCurrencyIdFromAccount(accountId));
            
            operationService.update(currentOperation);
            
            LogManager.d(TAG, "Операция обновлена успешно");
            returnToPrevious();
            return true;
        } catch (Exception e) {
            LogManager.e(TAG, "Ошибка при обновлении операции: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Получает ID валюты из счета
     */
    private int getCurrencyIdFromAccount(int accountId) {
        //TODO: Оптимизация получения валюты счета
        // метод сервиса для получения валюты счета по ID
        // заглушка для обработки окном
        for (Account account : accounts) {
            if (account.getId() == accountId) {
                return account.getCurrencyId();
            }
        }
        // Возвращаем валюту по умолчанию (RUB)
        return 1;
    }
    
    /**
     * Переопределяем настройку кнопки "Назад" для возврата к правильному окну
     */
    protected void setupBackButton(int backButtonId) {
        ImageButton back = findViewById(backButtonId);
        if (back != null) {
            back.setOnClickListener(v -> {
                // Возвращаемся к предыдущему экрану без сохранения
                if (operationType == OperationTypeFilter.INCOME.getIndex()) {
                    returnToIncomeActivity();
                } else {
                    returnToExpenseActivity();
                }
            });
        }
    }

    private void returnToIncomeActivity() {
        LogManager.d(TAG, "Переход к окну списка доходов, вкладка " + sourceTab);
        String[] params = {"selected_tab", String.valueOf(sourceTab)};
        returnTo(com.sadengineer.budgetmaster.income.IncomeActivity.class, true, params);
    }

    private void returnToExpenseActivity() {
        LogManager.d(TAG, "Переход к окну списка расходов, вкладка " + sourceTab);
        String[] params = {"selected_tab", String.valueOf(sourceTab)};
        returnTo(com.sadengineer.budgetmaster.expense.ExpenseActivity.class, true, params);
    }

    /**
     * Возвращается к предыдущему экрану
     */
    private void returnToPrevious() {
        LogManager.d(TAG, "Переход к окну списка операций, вкладка " + sourceTab);
        // Возвращаемся к соответствующему экрану операций в зависимости от типа операции
        if (operationType == OperationTypeFilter.INCOME.getIndex()) {
            String[] params = {"selected_tab", String.valueOf(sourceTab)};
            returnTo(com.sadengineer.budgetmaster.income.IncomeActivity.class, true, params);
        } else {
            String[] params = {"selected_tab", String.valueOf(sourceTab)};
            returnTo(com.sadengineer.budgetmaster.expense.ExpenseActivity.class, true, params);
        }
    }
}
