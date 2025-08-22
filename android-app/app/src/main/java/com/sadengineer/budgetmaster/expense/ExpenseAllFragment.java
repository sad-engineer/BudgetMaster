package com.sadengineer.budgetmaster.expense;

import android.util.Log;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.backend.entity.Operation;
import com.sadengineer.budgetmaster.backend.service.OperationService;
import com.sadengineer.budgetmaster.base.BaseListFragment;

import java.util.List;

/**
 * Фрагмент для отображения всех операций расходов
 */
public class ExpenseAllFragment extends BaseListFragment<Operation, ExpenseAdapter, ExpenseSharedViewModel, OperationService> {
    
    /**
     * Возвращает layout для фрагмента
     */
    @Override
    protected int getLayoutResourceId() {
        return R.layout.fragment_expense_all;
    }

    /**
     * Возвращает id recyclerView для фрагмента
     */
    @Override
    protected int getRecyclerViewId() {
        return R.id.expense_all_recycler;
    }

    /**
     * Возвращает класс ViewModel для фрагмента
     */
    @Override
    protected Class<ExpenseSharedViewModel> getViewModelClass() {
        return ExpenseSharedViewModel.class;
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
        return "expense"; // Тип операций расходов
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
            service.getOperationsByType("expense").observe(getViewLifecycleOwner(), this::handleDataLoaded);
        }
    }

    /**
     * Устанавливает данные в адаптер
     */
    @Override
    protected void setAdapterData(List<Operation> items) {
        adapter.setExpenses(items);
    }

    /**
     * Возвращает класс активности для редактирования
     */
    @Override
    protected Class<?> getEditActivityClass() {
        // TODO: Создать ExpenseEditActivity
        return null;
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
        adapter = new ExpenseAdapter(new ExpenseAdapter.OnExpenseClickListener() {
            @Override
            public void onExpenseClick(Operation expense) {
                Log.d(TAG, "Переход к окну редактирования операции расхода");
                // TODO: Реализовать переход к редактированию
                // goToEdit(expense);
            }
        }, requireContext());
        
        adapter.setLongClickListener(new ExpenseAdapter.OnExpenseLongClickListener() {
            @Override
            public void onExpenseLongClick(Operation expense) {
                Log.d(TAG, "Длительное нажатие на операцию расхода: " + expense.getDescription());
                showDeleteConfirmationDialog(expense);
            }
        });
        
        recyclerView.setAdapter(adapter);

        adapter.setOnSelectedExpensesChanged(selected -> {
            viewModel.setSelectedExpenses(selected);
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
     * Возвращает заголовок элемента для логирования
     */
    @Override
    protected String getItemTitle(Operation item) {
        return "ID: " + item.getId() + ", description: " + item.getDescription() + 
               ", amount: " + item.getAmount() + ", type: " + item.getType() + 
               ", isDeleted: " + item.isDeleted();
    }
} 