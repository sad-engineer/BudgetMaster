package com.sadengineer.budgetmaster.income;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.backend.entity.Operation;
import com.sadengineer.budgetmaster.backend.service.OperationService;
import com.sadengineer.budgetmaster.backend.filters.OperationTypeFilter;
import com.sadengineer.budgetmaster.base.BaseListFragment;
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;
import com.sadengineer.budgetmaster.operations.OperationEditActivity;
import com.sadengineer.budgetmaster.calculators.OperationCalculatorViewModel;
import com.sadengineer.budgetmaster.calculators.OperationCalculatorConfig;
import com.sadengineer.budgetmaster.backend.filters.OperationPeriod;
import com.sadengineer.budgetmaster.formatters.CurrencyAmountFormatter;
import com.sadengineer.budgetmaster.settings.AppSettings;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Фрагмент для отображения всех операций доходов
 */
public class IncomeAllFragment extends BaseListFragment<Operation, IncomeAdapter, IncomeSharedViewModel, OperationService> {
    
    private static final String TAG = "IncomeAllFragment";
    
    // UI элементы
    private ImageButton btnPrevMonth;
    private ImageButton btnNextMonth;
    private TextView tvMonthYear;
    private TextView tvTotalAmount;
    
    // Калькулятор операций
    private OperationCalculatorViewModel operationCalculator;
    private CurrencyAmountFormatter formatter;
    private AppSettings appSettings;
    
    // Текущий месяц/год
    private LocalDate currentMonth;
    private DateTimeFormatter monthYearFormatter;
    
    @Override
    public View onCreateView(@NonNull android.view.LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инициализация ДО вызова super.onCreateView()
        currentMonth = LocalDate.now().withDayOfMonth(1);
        monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale.getDefault());
        formatter = new CurrencyAmountFormatter();
        appSettings = new AppSettings(requireContext());
        
        View view = super.onCreateView(inflater, container, savedInstanceState);
        
        // Инициализация UI элементов
        initializeUIElements(view);
        
        // Инициализация калькулятора операций
        initializeOperationCalculator();
        
        // Настройка обработчиков событий
        setupEventHandlers();
        
        // Обновление отображения
        updateMonthDisplay();
        
        return view;
    }
    
    /**
     * Инициализация UI элементов
     */
    private void initializeUIElements(View view) {
        btnPrevMonth = view.findViewById(R.id.btn_prev_month);
        btnNextMonth = view.findViewById(R.id.btn_next_month);
        tvMonthYear = view.findViewById(R.id.tv_month_year);
        tvTotalAmount = view.findViewById(R.id.tv_total_amount);
    }
    
    /**
     * Инициализация калькулятора операций
     */
    private void initializeOperationCalculator() {
        // Калькулятор уже инициализирован в performDataLoading()
        // Здесь только настраиваем конфигурацию если калькулятор существует
        if (operationCalculator != null) {
            OperationCalculatorConfig config = new OperationCalculatorConfig();
            config.setPeriod(OperationPeriod.MONTH);
            config.setBaseDate(currentMonth);
            config.setOperationType(OperationTypeFilter.INCOME);
            config.setEntityFilter(EntityFilter.ACTIVE);
            config.setCurrencyId(appSettings.getDefaultCurrencyId());
            
            operationCalculator.setConfig(config);
        }
    }
    
    /**
     * Настройка обработчиков событий
     */
    private void setupEventHandlers() {
        btnPrevMonth.setOnClickListener(v -> {
            currentMonth = currentMonth.minusMonths(1);
            updateMonthDisplay();
            clearData(); // Сначала обнуляем данные
            loadData(); // Затем загружаем новые данные
        });
        
        btnNextMonth.setOnClickListener(v -> {
            currentMonth = currentMonth.plusMonths(1);
            updateMonthDisplay();
            clearData(); // Сначала обнуляем данные
            loadData(); // Затем загружаем новые данные
        });
    }
    
    /**
     * Обновление отображения месяца/года
     */
    private void updateMonthDisplay() {
        tvMonthYear.setText(currentMonth.format(monthYearFormatter));
    }
    
    /**
     * Очистка данных перед загрузкой новых
     */
    private void clearData() {
        Log.d(TAG, "Очищаем данные перед загрузкой нового месяца");
        
        // Очищаем список операций
        if (adapter != null) {
            adapter.setIncomes(new java.util.ArrayList<>());
        }
        
        // Обнуляем сумму
        if (tvTotalAmount != null) {
            tvTotalAmount.setText("0.00");
        }
    }
    
    /**
     * Возвращает layout для фрагмента
     */
    @Override
    protected int getLayoutResourceId() {
        return R.layout.fragment_income_all;
    }

    /**
     * Возвращает id recyclerView для фрагмента
     */
    @Override
    protected int getRecyclerViewId() {
        return R.id.income_all_recycler;
    }

    /**
     * Возвращает класс ViewModel для фрагмента
     */
    @Override
    protected Class<IncomeSharedViewModel> getViewModelClass() {
        return IncomeSharedViewModel.class;
    }

    /**
     * Возвращает класс сервиса для фрагмента
     */
    @Override
    protected Class<OperationService> getServiceClass() {
        return OperationService.class;
    }

    /**
     * Возвращает параметры для загрузки данных
     */
    @Override
    protected Object getLoadParameters() {
        return "income"; // Тип операций доходов
    }

    /**
     * Возвращает source_tab для фрагмента
     */
    @Override
    protected int getSourceTab() {
        return 0; // 0 = Все операции
    }

    /**
     * Выполняет загрузку данных
     */
    @Override
    protected void performDataLoading() {
        Log.d(TAG, "performDataLoading() вызван");
        Log.d(TAG, "currentMonth: " + currentMonth);
        Log.d(TAG, "appSettings: " + (appSettings != null ? "инициализирован" : "null"));
        Log.d(TAG, "operationCalculator: " + (operationCalculator != null ? "инициализирован" : "null"));
        
        if (currentMonth == null || appSettings == null) {
            Log.e(TAG, "Критическая ошибка: currentMonth или appSettings не инициализированы!");
            return;
        }
        
        // Инициализируем калькулятор если он еще не инициализирован
        if (operationCalculator == null) {
            Log.d(TAG, "Инициализируем operationCalculator в performDataLoading()");
            operationCalculator = new OperationCalculatorViewModel(requireActivity().getApplication());
            operationCalculator.initialize();
            
            // Устанавливаем наблюдатель за результатом
            operationCalculator.getResultAmount().observe(getViewLifecycleOwner(), new Observer<Long>() {
                @Override
                public void onChanged(Long totalAmount) {
                    if (totalAmount != null && tvTotalAmount != null) {
                        Log.d(TAG, "Обновляем сумму: " + totalAmount);
                        tvTotalAmount.setText(formatter.formatFromCents(totalAmount));
                    }
                }
            });
        }
        
        // Обновляем конфигурацию калькулятора
        OperationCalculatorConfig config = new OperationCalculatorConfig();
        config.setPeriod(OperationPeriod.MONTH);
        config.setBaseDate(currentMonth);
        config.setOperationType(OperationTypeFilter.INCOME);
        config.setEntityFilter(EntityFilter.ACTIVE);
        config.setCurrencyId(appSettings.getDefaultCurrencyId());
        
        operationCalculator.setConfig(config);
        
        // Загружаем операции за текущий месяц
        OperationService service = getServiceInstance();
        if (service != null) {
            LocalDate startDate = currentMonth.withDayOfMonth(1);
            LocalDate endDate = currentMonth.withDayOfMonth(currentMonth.lengthOfMonth());
            
            Log.d(TAG, "Загружаем операции с " + startDate + " по " + endDate);
            
            service.getByTypeAndDateRange(
                OperationTypeFilter.INCOME.getIndex(),
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59),
                EntityFilter.ACTIVE
            ).observe(getViewLifecycleOwner(), this::handleDataLoaded);
        } else {
            Log.e(TAG, "OperationService не создан!");
        }
    }

    /**
     * Устанавливает данные в адаптер
     */
    @Override
    protected void setAdapterData(List<Operation> items) {
        Log.d(TAG, "setAdapterData() вызван с " + (items != null ? items.size() : 0) + " операциями");
        adapter.setIncomes(items);
    }

    /**
     * Возвращает класс активности для редактирования
     */
    @Override
    protected Class<?> getEditActivityClass() {
        return OperationEditActivity.class;
    }

    /**
     * Наблюдает за режимом выбора
     */
    @Override
    protected void observeSelectionMode() {
        viewModel.getSelectionMode().observe(getViewLifecycleOwner(), enabled -> {
            if (adapter != null) {
                adapter.setSelectionMode(Boolean.TRUE.equals(enabled));
            }
        });
    }

    /**
     * Настраивает адаптер
     */
    @Override
    protected void setupAdapter() {
        adapter = new IncomeAdapter(new IncomeAdapter.OnIncomeClickListener() {
            @Override
            public void onIncomeClick(Operation income) {
                Log.d(TAG, "Переход к окну редактирования операции дохода");
                goToEdit(income);
            }
        }, requireContext());
        
        adapter.setLongClickListener(new IncomeAdapter.OnIncomeLongClickListener() {
            @Override
            public void onIncomeLongClick(Operation income) {
                Log.d(TAG, "Длительное нажатие на операцию дохода: " + income.getDescription());
                showDeleteConfirmationDialog(income);
            }
        });
        
        recyclerView.setAdapter(adapter);

        adapter.setOnSelectedIncomesChanged(selected -> {
            viewModel.setSelectedIncomes(selected);
        });
    }

    /**
     * Выполняет удаление
     */
    @Override
    protected void performDelete(OperationService service, Operation item) {
        // TODO: Реализовать метод delete в OperationService
        // service.delete(false, item);
    }

    /**
     * Переходит на экран редактирования операции дохода
     */
    @Override
    protected void goToEdit(Operation item) {
        Log.d(TAG, "Переход к окну редактирования операции дохода ID: " + item.getId());
        Intent intent = new Intent(getActivity(), OperationEditActivity.class);
        intent.putExtra("operation_type", OperationTypeFilter.INCOME.getIndex());
        intent.putExtra("operation", item);
        intent.putExtra("source_tab", getSourceTab());
        startActivity(intent);
    }
    
    /**
     * Возвращает заголовок элемента для логирования
     */
    @Override
    protected String getItemTitle(Operation item) {
        return "ID: " + item.getId() + ", description: " + item.getDescription() + 
               ", amount: " + item.getAmount() + ", type: " + item.getType() + 
               ", isDeleted: " + item.isDeleted();
    }
} 