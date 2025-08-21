package com.sadengineer.budgetmaster.accounts;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.backend.entity.Account;
import com.sadengineer.budgetmaster.backend.entity.Currency;
import com.sadengineer.budgetmaster.backend.service.CurrencyService;
import com.sadengineer.budgetmaster.animations.StandartViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Адаптер для отображения счетов в RecyclerView
 */
public class AccountsAdapter extends RecyclerView.Adapter<StandartViewHolder> {
    private static final String TAG = "AccountsAdapter";

    /** Имя пользователя по умолчанию */
    /** TODO: передлать на получение имени пользователя из SharedPreferences */
    private String userName = "default_user";

    private List<Account> accounts = new ArrayList<>();
    private OnAccountClickListener listener;
    private OnAccountLongClickListener longClickListener;
    private boolean isSelectionMode = false;
    private Set<Integer> selectedAccounts = new HashSet<>();
    private OnSelectedAccountsChanged externalSelectedAccountsChanged;
    private CurrencyService currencyService;
    private Map<Integer, String> currencyCache = new HashMap<>();
    
    public interface OnAccountClickListener {
        void onAccountClick(Account account);
    }
    
    public interface OnAccountLongClickListener {
        void onAccountLongClick(Account account);
    }

    /**
     * Интерфейс для передачи полного списка выбранных аккаунтов наружу
     */
    public interface OnSelectedAccountsChanged {
        void onSelectedAccountsChanged(List<Account> selectedAccounts);
    }
    
    /**
     * Конструктор адаптера
     * @param listener обработчик клика на счет
     * @param context контекст приложения
     */
    public AccountsAdapter(OnAccountClickListener listener, Context context) {
        this.listener = listener;
        this.currencyService = new CurrencyService(context, userName);
    }
    
    /**
     * Устанавливает обработчик длинного клика на счет
     * @param longClickListener обработчик длинного клика
     */
    public void setLongClickListener(OnAccountLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    /**
     * Устанавливает обработчик изменения выбора
     * @param listener обработчик изменения выбора
     */
    public void setOnSelectedAccountsChanged(OnSelectedAccountsChanged listener) {
        this.externalSelectedAccountsChanged = listener;
    }
    
    /**
     * Создает ViewHolder для элемента списка
     * @param parent родительский ViewGroup
     * @param viewType тип элемента
     * @return ViewHolder для элемента списка
     */
    @NonNull
    @Override
    public StandartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_account, parent, false);
        StandartViewHolder holder = new StandartViewHolder(view);
        
        // Настраиваем обработчики для универсального ViewHolder
        // Обработчик клика на счет
        holder.setItemClickListener(itemId -> {
            if (listener != null) {
                Account account = findAccountById(itemId);
                if (account != null) {
                    listener.onAccountClick(account);
                }
            }
        });
        
        // Обработчик длинного клика на счет
        holder.setItemLongClickListener(itemId -> {
            if (longClickListener != null) {
                Account account = findAccountById(itemId);
                if (account != null) {
                    longClickListener.onAccountLongClick(account);
                }
            }
        });

        // Обработчик изменения выбора конкретного элемента
        holder.setItemSelectionListener((itemId, isSelected) -> {
            // Проверяем, что счет не удален перед добавлением в выбор
            Account account = findAccountById(itemId);
            if (account != null && !account.isDeleted()) {
                if (isSelected) {
                    selectedAccounts.add(itemId);
                } else {
                    selectedAccounts.remove(itemId);
                }
                // Сообщаем наружу полный набор выбранных счетов
                if (externalSelectedAccountsChanged != null) {
                    externalSelectedAccountsChanged.onSelectedAccountsChanged(getSelectedAccounts());
                }
            } else {
                Log.w(TAG, "Попытка выбора удалённого счёта: ID=" + itemId);
            }
        });        
        return holder;
    }
    
    /**
     * Привязывает данные к ViewHolder
     * @param holder ViewHolder для элемента списка
     * @param position позиция элемента в списке
     */
    @Override
    public void onBindViewHolder(@NonNull StandartViewHolder holder, int position) {
        Account account = accounts.get(position);
        holder.resetToInitialState();
        // Определяем, выбран ли текущий элемент (только для неудаленных счетов)
        boolean isSelected = !account.isDeleted() && selectedAccounts.contains(account.getId()); 
        
        // Получаем короткое имя валюты из кэша
        String currencyShortName = currencyCache.getOrDefault(account.getCurrencyId(), "RUB");
        Log.d(TAG, "Счет ID=" + account.getId() + ", currencyId=" + account.getCurrencyId() + ", валюта: " + currencyShortName);
        
        holder.bind(
            account.getPosition(), account.getTitle(), account.getId(), account.getAmount(), currencyShortName, isSelectionMode, isSelected);
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
     * @param accounts список счетов
     */
    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts != null ? accounts : new ArrayList<>();
        loadCurrencyCache();
        notifyDataSetChanged();
    }
    
    /**
     * Загружает валюты в кэш
     */
    private void loadCurrencyCache() {
        currencyCache.clear();
        try {
            // Получаем все валюты асинхронно
            currencyService.getAll().observeForever(currencies -> {
                if (currencies != null) {
                    for (Currency currency : currencies) {
                        if (currency.getShortName() != null && !currency.getShortName().isEmpty()) {
                            currencyCache.put(currency.getId(), currency.getShortName());
                        }
                    }
                    Log.d(TAG, "Кэш валют загружен: " + currencyCache.size() + " валют");
                    // Обновляем UI после загрузки кэша
                    notifyDataSetChanged();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Ошибка загрузки кэша валют: " + e.getMessage(), e);
        }
    }
    
    /**
     * Обновляет кэш валют
     */
    public void refreshCurrencyCache() {
        loadCurrencyCache();
    }
    
    /**
     * Включает/выключает режим выбора
     * @param enabled true - включить режим выбора, false - выключить
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
     * @return список выбранных счетов
     */
    public List<Account> getSelectedAccounts() {
        List<Account> selected = new ArrayList<>();
        for (Integer id : selectedAccounts) {
            Account account = findAccountById(id);
            if (account != null && !account.isDeleted()) { // Исключаем уже удаленные счета
                selected.add(account);
            }
        }
        return selected;
    }
    
    /**
     * Очищает выбор
     * @return список выбранных счетов
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
     * @param id ID счета
     * @return счет или null, если счет не найден
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