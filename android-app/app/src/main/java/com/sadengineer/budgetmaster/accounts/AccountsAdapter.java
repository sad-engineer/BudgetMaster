package com.sadengineer.budgetmaster.accounts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.backend.entity.Account;
import com.sadengineer.budgetmaster.animations.StandartViewHolder;
import com.sadengineer.budgetmaster.settings.SettingsManager;
import com.sadengineer.budgetmaster.interfaces.ISelectionAdapter;
import com.sadengineer.budgetmaster.utils.LogManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Адаптер для отображения счетов в RecyclerView
 */
public class AccountsAdapter extends RecyclerView.Adapter<StandartViewHolder> implements ISelectionAdapter<Account> {
    private static final String TAG = "AccountsAdapter";


    private List<Account> mAccounts = new ArrayList<>();
    private OnAccountClickListener mListener;
    private OnAccountLongClickListener mLongClickListener;
    private boolean mIsSelectionMode = false;
    private Set<Integer> mSelectedAccounts = new HashSet<>();
    private OnSelectedAccountsChanged mExternalSelectedAccountsChanged;
    
    /** Общая сумма всех счетов для карточки "Итого" */
    private long mTotalAmount = 0L;
    
    /** Интерфейс для получения валюты */
    public interface CurrencyProvider {
        String getCurrencyShortName(int currencyId);
    }
    
    private CurrencyProvider mCurrencyProvider;
    
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
        void onSelectedAccountsChanged(List<Account> mSelectedAccounts);
    }
    
    /**
     * Конструктор адаптера
     * @param mListener обработчик клика на счет
     * @param currencyProvider поставщик валют
     */
    public AccountsAdapter(OnAccountClickListener mListener, CurrencyProvider currencyProvider) {
        this.mListener = mListener;
        this.mCurrencyProvider = currencyProvider;
    }
    
    /**
     * Устанавливает обработчик длинного клика на счет
     * @param mLongClickListener обработчик длинного клика
     */
    public void setLongClickListener(OnAccountLongClickListener mLongClickListener) {
        this.mLongClickListener = mLongClickListener;
    }

    /**
     * Устанавливает обработчик изменения выбора
     * @param mListener обработчик изменения выбора
     */
    public void setOnSelectedAccountsChanged(OnSelectedAccountsChanged mListener) {
        this.mExternalSelectedAccountsChanged = mListener;
    }
    
    /**
     * Возвращает тип элемента для позиции
     */
    @Override
    public int getItemViewType(int position) {
        // Позиция 0 - карточка "Итого", остальные - обычные счета
        return position == 0 ? 0 : 1;
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
        
        // Инициализируем менеджер настроек
        SettingsManager.init(parent.getContext());
        
        StandartViewHolder holder = new StandartViewHolder(view);
        
        // Настраиваем обработчики для универсального ViewHolder
        // Обработчик клика на счет
        holder.setItemClickListener(itemId -> {
            // Карточка "Итого" не кликабельна
            if (itemId == -1) {
                return;
            }
            
            if (mListener != null) {
                Account account = findAccountById(itemId);
                if (account != null) {
                    mListener.onAccountClick(account);
                }
            }
        });
        
        // Обработчик длинного клика на счет
        holder.setItemLongClickListener(itemId -> {
            // Карточка "Итого" не кликабельна
            if (itemId == -1) {
                return;
            }
            
            if (mLongClickListener != null) {
                Account account = findAccountById(itemId);
                if (account != null) {
                    mLongClickListener.onAccountLongClick(account);
                }
            }
        });

        // Обработчик изменения выбора конкретного элемента
        holder.setItemSelectionListener((itemId, isSelected) -> {
            // Проверяем, что счет не удален перед добавлением в выбор
            Account account = findAccountById(itemId);
            if (account != null && !account.isDeleted()) {
                if (isSelected) {
                    mSelectedAccounts.add(itemId);
                } else {
                    mSelectedAccounts.remove(itemId);
                }
                // Сообщаем наружу полный набор выбранных счетов
                if (mExternalSelectedAccountsChanged != null) {
                    mExternalSelectedAccountsChanged.onSelectedAccountsChanged(getSelectedItems());
                }
            } else {
                LogManager.w(TAG, "Попытка выбора удалённого счёта: ID=" + itemId);
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
        if (position == 0) {
            // Карточка "Итого"
            holder.bind(
                0, // позиция
                "Итого", // заголовок
                -1, // специальный ID для карточки "Итого"
                mTotalAmount, // общая сумма
                "₽", // используем рубли как основную валюту
                false, // режим выбора отключен для итоговой карточки
                false, // не выбрана
                false, // не показываем позицию
                false  // не показываем ID
            );
            
            LogManager.d(TAG, "onBindViewHolder: карточка 'Итого' с суммой: " + mTotalAmount);
        } else if (position > 0 && position <= mAccounts.size()) {
            // Обычные счета (смещаем позицию на -1)
            Account account = mAccounts.get(position - 1);
            holder.resetToInitialState();
            // Определяем, выбран ли текущий элемент (только для неудаленных счетов)
            boolean isSelected = !account.isDeleted() && mSelectedAccounts.contains(account.getId()); 
            
            // Получаем короткое имя валюты через провайдера
            String currencyShortName = mCurrencyProvider != null ? 
                mCurrencyProvider.getCurrencyShortName(account.getCurrencyId()) : "RUB";
            LogManager.d(TAG, "Счет ID=" + account.getId() + ", currencyId=" + account.getCurrencyId() + ", валюта: " + currencyShortName);
            
            // Используем новый форматтер для отображения сумм в копейках
            holder.bind(
                account.getPosition(), account.getTitle(), account.getId(), account.getAmount(), currencyShortName, mIsSelectionMode, isSelected,
                SettingsManager.isShowPosition(), SettingsManager.isShowId());
        }
    }

    /**
     * Возвращает количество элементов в списке
     */
    @Override
    public int getItemCount() {
        // +1 для карточки "Итого"
        return mAccounts.size() + 1;
    }
    
    /**
     * Обновляет список счетов
     * @param mAccounts список счетов
     */
    public void setItems(List<Account> mAccounts) {
        this.mAccounts = mAccounts != null ? mAccounts : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    
    /**
     * Обновляет общую сумму для карточки "Итого"
     * @param mTotalAmount общая сумма всех счетов
     */
    public void setTotalAmount(long mTotalAmount) {
        this.mTotalAmount = mTotalAmount;
        // Обновляем только первую позицию (карточка "Итого")
        notifyItemChanged(0);
        LogManager.d(TAG, "Обновлена общая сумма счетов: " + mTotalAmount);
    }
        
    /**
     * Устанавливает провайдера валют
     */
    public void setCurrencyProvider(CurrencyProvider currencyProvider) {
        this.mCurrencyProvider = currencyProvider;
    }
    
    /**
     * Включает/выключает режим выбора
     * @param enabled true - включить режим выбора, false - выключить
     */
    public void setSelectionMode(boolean enabled) {
        this.mIsSelectionMode = enabled;
        if (!enabled) {
            mSelectedAccounts.clear();
        }
        notifyDataSetChanged();
        // Сообщаем наружу полный набор выбранных
        if (mExternalSelectedAccountsChanged != null) {
            mExternalSelectedAccountsChanged.onSelectedAccountsChanged(getSelectedItems());
        }
    }
    
    /**
     * Получает выбранные счета
     * @return список выбранных счетов
     */
    public List<Account> getSelectedItems() {
        List<Account> selected = new ArrayList<>();
        for (Integer id : mSelectedAccounts) {
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
        mSelectedAccounts.clear();
        notifyDataSetChanged();
        if (mExternalSelectedAccountsChanged != null) {
            mExternalSelectedAccountsChanged.onSelectedAccountsChanged(getSelectedItems());
        }
    }
    
    /**
     * Находит счет по ID
     * @param id ID счета
     * @return счет или null, если счет не найден
     */
    private Account findAccountById(int id) {
        for (Account account : mAccounts) {
            if (account.getId() == id) {
                return account;
            }
        }
        return null;
    }
} 