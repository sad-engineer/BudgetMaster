package com.sadengineer.budgetmaster.currencies;

import android.util.Log;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.backend.entity.Currency;
import com.sadengineer.budgetmaster.backend.service.CurrencyService;
import com.sadengineer.budgetmaster.base.BaseListFragment;

import java.util.List;
import java.util.ArrayList;

/**
 * Фрагмент для отображения списка валют
 */
public class CurrenciesListFragment extends BaseListFragment<Currency, CurrencyAdapter, CurrenciesSharedViewModel, CurrencyService> {
    
    /**
     * Возвращает layout для фрагмента
     */
    @Override
    protected int getLayoutResourceId() {
        return R.layout.fragment_currencies_list;
    }

    /**
     * Возвращает id recyclerView для фрагмента
     */
    @Override
    protected int getRecyclerViewId() {
        return R.id.currencies_recycler;
    }

    /**
     * Возвращает класс ViewModel для фрагмента
     */
    @Override
    protected Class<CurrenciesSharedViewModel> getViewModelClass() {
        return CurrenciesSharedViewModel.class;
    }

    /**
     * Возвращает класс сервиса для фрагмента
     */
    @Override
    protected Class<CurrencyService> getServiceClass() {
        return CurrencyService.class;
    }

    /**
     * Возвращает параметры для загрузки данных
     */
    @Override
    protected Object getLoadParameters() {
        return null; // Загружаем все валюты
    }

    /**
     * Возвращает source_tab для фрагмента
     */
    @Override
    protected int getSourceTab() {
        return 0; // У валют нет вкладок
    }

    /**
     * Выполняет загрузку данных
     */
    @Override
    protected void performDataLoading() {
        CurrencyService service = getServiceInstance();
        service.getAll().observe(getViewLifecycleOwner(), this::handleDataLoaded);
    }

    /**
     * Устанавливает данные в адаптер
     */
    @Override
    protected void setAdapterData(List<Currency> items) {
        adapter.setCurrencies(items);
    }

    /**
     * Возвращает класс активности для редактирования
     */
    @Override
    protected Class<?> getEditActivityClass() {
        return CurrencyEditActivity.class;
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
        adapter = new CurrencyAdapter();
        
        adapter.setClickListener(currency -> {
            Log.d(TAG, "Клик по валюте: " + currency.getId());
            goToEdit(currency);
        });
        
        adapter.setLongClickListener(currency -> {
            Log.d(TAG, "Длительный клик по валюте: " + currency.getId());
            showDeleteConfirmationDialog(currency);
        });
        
        adapter.setSelectionListener(selectedCount -> {
            Log.d(TAG, "Выбрано валют: " + selectedCount);
            // Уведомляем Activity о количестве выбранных элементов
            if (getActivity() instanceof CurrenciesActivity) {
                ((CurrenciesActivity) getActivity()).updateSelectionCount(selectedCount);
            }
            // Обновляем выбранные валюты в ViewModel
            if (viewModel instanceof CurrenciesSharedViewModel) {
                ((CurrenciesSharedViewModel) viewModel).setSelectedCurrencies(adapter.getSelectedCurrencies());
            }
        });
        
        recyclerView.setAdapter(adapter);
    }

    /**
     * Выполняет удаление валюты
     */
    @Override
    protected void performDelete(CurrencyService service, Currency item) {
        service.delete(false, item); // false = hard delete
    }

    /**
     * Возвращает заголовок элемента для диалога удаления
     */
    @Override
    protected String getItemTitle(Currency item) {
        return "ID: " + item.getId() + ", title: " + item.getTitle();
    }
    
    /**
     * Возвращает выбранные валюты
     */
    public List<Currency> getSelectedCurrencies() {
        if (adapter != null) {
            return adapter.getSelectedCurrencies();
        }
        return new ArrayList<>();
    }
}
