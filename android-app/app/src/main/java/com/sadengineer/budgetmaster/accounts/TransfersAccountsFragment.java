package com.sadengineer.budgetmaster.accounts;

import android.util.Log;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.backend.database.BudgetMasterDatabase;
import com.sadengineer.budgetmaster.backend.entity.Account;
import com.sadengineer.budgetmaster.backend.service.AccountService;
import com.sadengineer.budgetmaster.base.BaseListFragment;
import com.sadengineer.budgetmaster.backend.constants.ModelConstants;
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;

import java.util.List;

/**
 * Фрагмент для отображения счетов переводов (тип 3)
 */
public class TransfersAccountsFragment extends BaseListFragment<Account, AccountsAdapter, AccountsSharedViewModel, AccountService> {

    private final int ACCOUNT_TYPE = ModelConstants.ACCOUNT_TYPE_CREDIT; // Используем CREDIT для переводов

    /**
     * Возвращает layout для фрагмента
     */
    @Override
    protected int getLayoutResourceId() {
        return R.layout.fragment_transfers_accounts;
    }

    /**
     * Возвращает id recyclerView для фрагмента
     */
    @Override
    protected int getRecyclerViewId() {
        return R.id.accounts_transfers_recycler;
    }

    /**
     * Возвращает класс ViewModel для фрагмента
     */
    @Override
    protected Class<AccountsSharedViewModel> getViewModelClass() {
        return AccountsSharedViewModel.class;
    }

    /**
     * Возвращает класс сервиса для фрагмента
     */
    @Override
    protected Class<AccountService> getServiceClass() {
        return AccountService.class;
    }

    /**
     * Возвращает параметры для загрузки данных
     */
    @Override
    protected Object getLoadParameters() {
        return ACCOUNT_TYPE; // Тип счетов переводов
    }

    /**
     * Возвращает source_tab для фрагмента
     */
    @Override
    protected int getSourceTab() {
        return 2; // 2 = Переводы
    }

    /**
     * Выполняет загрузку данных
     */
    @Override
    protected void performDataLoading() {
        // Для переводов загружаем только активные счета
        // Используем сервис из базового класса
        AccountService service = getServiceInstance();
        if (service != null) {
            service.getAllByType(ACCOUNT_TYPE, EntityFilter.ALL).observe(getViewLifecycleOwner(), this::handleDataLoaded);
        }
    }

    /**
     * Устанавливает данные в адаптер
     */
    @Override
    protected void setAdapterData(List<Account> items) {
        adapter.setAccounts(items);
    }

    /**
     * Возвращает класс активности для редактирования
     */
    @Override
    protected Class<?> getEditActivityClass() {
        return AccountsEditActivity.class;
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
        adapter = new AccountsAdapter(new AccountsAdapter.OnAccountClickListener() {
            @Override
            public void onAccountClick(Account account) {
                Log.d(TAG, "Переход к окну редактирования счёта");
                goToEdit(account);
            }
        }, requireContext());
        
        adapter.setLongClickListener(new AccountsAdapter.OnAccountLongClickListener() {
            @Override
            public void onAccountLongClick(Account account) {
                Log.d(TAG, "Длительное нажатие на счёт");
                showDeleteConfirmationDialog(account);
            }
        });
        
        recyclerView.setAdapter(adapter);

        adapter.setOnSelectedAccountsChanged(selected -> {
            viewModel.setSelectedAccounts(selected);
        });
    }

    /**
     * Выполняет удаление
     */
    @Override
    protected void performDelete(AccountService service, Account item) {
        service.delete(item, false);
    }

    /**
     * Возвращает заголовок элемента для логирования
     */
    @Override
    protected String getItemTitle(Account item) {
        return "ID: " + item.getId() + ", title: " + item.getTitle() + 
               ", position: " + item.getPosition() + ", deleteTime: " + item.getDeleteTime() + 
               ", deletedBy: " + item.getDeletedBy() + ", isDeleted: " + item.isDeleted();
    }
} 