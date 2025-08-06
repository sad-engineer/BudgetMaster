package com.sadengineer.budgetmaster.accounts;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.backend.database.BudgetMasterDatabase;
import com.sadengineer.budgetmaster.backend.entity.Account;

import java.util.List;
import java.util.ArrayList;

public class SavingsAccountsFragment extends Fragment {
    private static final String TAG = "SavingsAccountsFragment";
    private RecyclerView recyclerView;
    private AccountsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_savings_accounts, container, false);
        
        // Настраиваем RecyclerView
        recyclerView = view.findViewById(R.id.accounts_savings_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Создаем адаптер
        adapter = new AccountsAdapter(new AccountsAdapter.OnAccountClickListener() {
            @Override
            public void onAccountClick(Account account) {
                Log.d(TAG, "👆 Выбран сберегательный счет: " + account.getTitle());
                // TODO: Обработка клика по счету
            }
        });
        
        // Настраиваем обработчик изменения выбора
        adapter.setSelectionListener(new AccountsAdapter.OnSelectionChangedListener() {
            @Override
            public void onSelectionChanged(int selectedCount) {
                Log.d(TAG, "🔄 Изменение выбора сберегательных счетов: " + selectedCount + " выбрано");
            }
        });
        
        recyclerView.setAdapter(adapter);
        
        // Загружаем счета типа 2 (сберегательные)
        loadSavingsAccounts();
        
        return view;
    }
    
    /**
     * Загружает сберегательные счета (тип 2)
     */
    private void loadSavingsAccounts() {
        Log.d(TAG, "🔄 Загружаем сберегательные счета...");
        
        try {
            BudgetMasterDatabase database = BudgetMasterDatabase.getDatabase(requireContext());
            
            // Загружаем счета типа 2 (сберегательные)
            database.accountDao().getAllByType("2").observe(getViewLifecycleOwner(), accounts -> {
                Log.d(TAG, "✅ Загружено сберегательных счетов: " + (accounts != null ? accounts.size() : 0));
                
                if (accounts != null && !accounts.isEmpty()) {
                    adapter.setAccounts(accounts);
                    Log.d(TAG, "✅ Сберегательные счета отображены в списке");
                } else {
                    Log.w(TAG, "⚠️ Сберегательные счета не найдены в базе данных");
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Ошибка загрузки сберегательных счетов: " + e.getMessage(), e);
        }
    }
    
    /**
     * Устанавливает режим выбора
     */
    public void setSelectionMode(boolean enabled) {
        if (adapter != null) {
            adapter.setSelectionMode(enabled);
        }
    }
    
    /**
     * Получает выбранные счета
     */
    public List<Account> getSelectedAccounts() {
        if (adapter != null) {
            return adapter.getSelectedAccounts();
        }
        return new ArrayList<>();
    }
} 