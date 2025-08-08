package com.sadengineer.budgetmaster.accounts;

import android.util.Log;
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
    private OnAccountLongClickListener longClickListener;
    private boolean isSelectionMode = false;
    private Set<Integer> selectedAccounts = new HashSet<>();
    private OnSelectedAccountsChanged externalSelectedAccountsChanged;
    
    public interface OnAccountClickListener {
        void onAccountClick(Account account);
    }
    
    public interface OnAccountLongClickListener {
        void onAccountLongClick(Account account);
    }

    // Для передачи полного списка выбранных аккаунтов наружу (в VM через фрагмент)
    public interface OnSelectedAccountsChanged {
        void onSelectedAccountsChanged(List<Account> selectedAccounts);
    }
    
    public AccountsAdapter(OnAccountClickListener listener) {
        this.listener = listener;
    }
    
    public void setLongClickListener(OnAccountLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public void setOnSelectedAccountsChanged(OnSelectedAccountsChanged listener) {
        this.externalSelectedAccountsChanged = listener;
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
        
        holder.setItemLongClickListener(itemId -> {
            if (longClickListener != null) {
                Account account = findAccountById(itemId);
                if (account != null) {
                    longClickListener.onAccountLongClick(account);
                }
            }
        });

        // Настраиваем обработчик изменения выбора конкретного элемента
        holder.setItemSelectionListener((itemId, isSelected) -> {
            if (isSelected) {
                selectedAccounts.add(itemId);
            } else {
                selectedAccounts.remove(itemId);
            }
            
            // Сообщаем наружу полный набор выбранных
            if (externalSelectedAccountsChanged != null) {
                externalSelectedAccountsChanged.onSelectedAccountsChanged(getSelectedAccounts());
            }
        });
        
        return holder;
    }
    
    @Override
    public void onBindViewHolder(@NonNull StandartViewHolder holder, int position) {
        Account account = accounts.get(position);
        holder.resetToInitialState();

        // Определяем, выбран ли текущий элемент
        boolean isSelected = selectedAccounts.contains(account.getId());

        Log.d(TAG, "🔄 Привязываем данные к ViewHolder: " + account.getTitle() + " (позиция " + account.getPosition() + ")" +
        "ID: " + account.getId() + ", сумма: " + account.getAmount() + ", режим выбора: " + isSelectionMode + ", выбран: " + isSelected); 
        holder.bind(account.getPosition(), account.getTitle(), account.getId(), 
                   account.getAmount(), isSelectionMode, isSelected);
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
        Log.d(TAG, "🔄 Обновляем список счетов: " + this.accounts.size() + " элементов");
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
        // Сообщаем наружу полный набор выбранных
        if (externalSelectedAccountsChanged != null) {
            externalSelectedAccountsChanged.onSelectedAccountsChanged(getSelectedAccounts());
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
        if (externalSelectedAccountsChanged != null) {
            externalSelectedAccountsChanged.onSelectedAccountsChanged(getSelectedAccounts());
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