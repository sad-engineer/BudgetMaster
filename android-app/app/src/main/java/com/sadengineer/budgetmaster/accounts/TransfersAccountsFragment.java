package com.sadengineer.budgetmaster.accounts;

import android.content.Intent;
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

public class TransfersAccountsFragment extends Fragment {
    private static final String TAG = "TransfersAccountsFragment";
    private RecyclerView recyclerView;
    private AccountsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transfers_accounts, container, false);
        
        // Настраиваем RecyclerView
        recyclerView = view.findViewById(R.id.accounts_transfers_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Создаем адаптер
        adapter = new AccountsAdapter(new AccountsAdapter.OnAccountClickListener() {
            @Override
            public void onAccountClick(Account account) {
                Log.d(TAG, "👆 Выбран счет переводов: " + account.getTitle());
                // Переходим на экран редактирования счета
                goToAccountEdit(account);
            }
        });
        
        // Настраиваем обработчик изменения выбора
        adapter.setSelectionListener(new AccountsAdapter.OnSelectionChangedListener() {
            @Override
            public void onSelectionChanged(int selectedCount) {
                Log.d(TAG, "🔄 Изменение выбора счетов переводов: " + selectedCount + " выбрано");
            }
        });
        
        recyclerView.setAdapter(adapter);
        
        // Загружаем счета типа 3 (переводы)
        loadTransfersAccounts();
        
        return view;
    }
    
    /**
     * Загружает счета переводов (тип 3)
     */
    private void loadTransfersAccounts() {
        Log.d(TAG, "🔄 Загружаем счета переводов...");
        
        try {
            BudgetMasterDatabase database = BudgetMasterDatabase.getDatabase(requireContext());
            
            // Загружаем счета типа 3 (переводы)
            database.accountDao().getAllByType("3").observe(getViewLifecycleOwner(), accounts -> {
                Log.d(TAG, "✅ Загружено счетов переводов: " + (accounts != null ? accounts.size() : 0));
                
                if (accounts != null && !accounts.isEmpty()) {
                    adapter.setAccounts(accounts);
                    Log.d(TAG, "✅ Счета переводов отображены в списке");
                } else {
                    Log.w(TAG, "⚠️ Счета переводов не найдены в базе данных");
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Ошибка загрузки счетов переводов: " + e.getMessage(), e);
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
    
    /**
     * Переходит на экран редактирования счета
     * @param account - выбранный счет
     */
    private void goToAccountEdit(Account account) {
        Log.d(TAG, "🔄 Переходим к окну редактирования счета");
        Intent intent = new Intent(getActivity(), AccountsEditActivity.class);
        intent.putExtra("account", account);
        intent.putExtra("source_tab", 2); // 2 = Переводы
        startActivity(intent);
    }
} 