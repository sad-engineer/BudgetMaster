package com.sadengineer.budgetmaster.accounts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.backend.entity.Account;
import com.sadengineer.budgetmaster.animations.StandartViewHolder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Адаптер для отображения счетов в RecyclerView
 */
public class AccountsAdapter extends RecyclerView.Adapter<StandartViewHolder> {
    private static final String TAG = "AccountsAdapter";

    private List<Account> accounts = new ArrayList<>();
    private OnAccountClickListener listener;
    private boolean isSelectionMode = false;
    private Set<Integer> selectedAccounts = new HashSet<>();
    
    public interface OnAccountClickListener {
        void onAccountClick(Account account);
    }
    
    public interface OnSelectionChangedListener {
        void onSelectionChanged(int selectedCount);
    }
    
    private OnSelectionChangedListener selectionListener;
    
    public AccountsAdapter(OnAccountClickListener listener) {
        this.listener = listener;
    }
    
    public void setSelectionListener(OnSelectionChangedListener listener) {
        this.selectionListener = listener;
    }
    
    @NonNull
    @Override
    public StandartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_account, parent, false);
        StandartViewHolder holder = new StandartViewHolder(view);
        
        // Настраиваем обработчики для универсального ViewHolder
        holder.setItemClickListener(itemId -> {
            if (listener != null) {
                Account account = findAccountById(itemId);
                if (account != null) {
                    listener.onAccountClick(account);
                }
            }
        });
        
        holder.setSelectionListener(selectedCount -> {
            if (selectionListener != null) {
                selectionListener.onSelectionChanged(selectedCount);
            }
        });
        
        return holder;
    }
    
    @Override
    public void onBindViewHolder(@NonNull StandartViewHolder holder, int position) {
        Account account = accounts.get(position);
        holder.bind(account.getPosition(), account.getTitle(), account.getId(), 
                   account.getAmount(), isSelectionMode, selectedAccounts);
    }

    /**
     * Возвращает количество элементов в списке
     */
    @Override
    public int getItemCount() {
        return accounts.size();
    }
    
    /**
     * Обновляет список счетов
     */
    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts != null ? accounts : new ArrayList<>();
        android.util.Log.d(TAG, "🔄 Обновляем список счетов: " + this.accounts.size() + " элементов");
        notifyDataSetChanged();
    }
    
    /**
     * Включает/выключает режим выбора
     */
    public void setSelectionMode(boolean enabled) {
        this.isSelectionMode = enabled;
        if (!enabled) {
            selectedAccounts.clear();
        }
        notifyDataSetChanged();
        if (selectionListener != null) {
            selectionListener.onSelectionChanged(selectedAccounts.size());
        }
    }
    
    /**
     * Получает выбранные счета
     */
    public List<Account> getSelectedAccounts() {
        List<Account> selected = new ArrayList<>();
        for (Integer id : selectedAccounts) {
            Account account = findAccountById(id);
            if (account != null) {
                selected.add(account);
            }
        }
        return selected;
    }
    
    /**
     * Очищает выбор
     */
    public void clearSelection() {
        selectedAccounts.clear();
        notifyDataSetChanged();
        if (selectionListener != null) {
            selectionListener.onSelectionChanged(0);
        }
    }
    
    /**
     * Находит счет по ID
     */
    private Account findAccountById(int id) {
        for (Account account : accounts) {
            if (account.getId() == id) {
                return account;
            }
        }
        return null;
    }
} 