package com.sadengineer.budgetmaster.income;

import android.content.Intent;
import android.util.Log;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.backend.entity.Operation;
import com.sadengineer.budgetmaster.backend.service.OperationService;
import com.sadengineer.budgetmaster.backend.filters.OperationTypeFilter;
import com.sadengineer.budgetmaster.base.BaseListFragment;
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;
import com.sadengineer.budgetmaster.operations.OperationEditActivity;

import java.util.List;

/**
 * Фрагмент для отображения всех операций доходов
 */
public class IncomeAllFragment extends BaseListFragment<Operation, IncomeAdapter, IncomeSharedViewModel, OperationService> {
    
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
        // Используем сервис из базового класса
        OperationService service = getServiceInstance();
        if (service != null) {
            service.getAllByType(OperationTypeFilter.INCOME.getIndex(), EntityFilter.ALL).observe(getViewLifecycleOwner(), this::handleDataLoaded);
        }
    }

    /**
     * Устанавливает данные в адаптер
     */
    @Override
    protected void setAdapterData(List<Operation> items) {
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