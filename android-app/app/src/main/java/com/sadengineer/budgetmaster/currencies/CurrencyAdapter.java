package com.sadengineer.budgetmaster.currencies;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.backend.entity.Currency;
import com.sadengineer.budgetmaster.animations.StandartViewHolder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Адаптер для отображения валют в RecyclerView
 */
public class CurrencyAdapter extends RecyclerView.Adapter<StandartViewHolder> {
    private static final String TAG = "CurrencyAdapter";

    private List<Currency> currencies = new ArrayList<>();
    private OnCurrencyClickListener clickListener;
    private OnCurrencyLongClickListener longClickListener;
    private OnSelectionListener selectionListener;
    private boolean isSelectionMode = false;
    private Set<Integer> selectedCurrencies = new HashSet<>();
    
    /**
     * Интерфейс для обработки кликов на валюте
     */
    public interface OnCurrencyClickListener {
        void onCurrencyClick(Currency currency);
    }
    
    /**
     * Интерфейс для обработки длинных кликов на валюте
     */
    public interface OnCurrencyLongClickListener {
        void onCurrencyLongClick(Currency currency);
    }
    
    /**
     * Интерфейс для обработки изменений выбора
     */
    public interface OnSelectionListener {
        void onSelectionChanged(int selectedCount);
    }
    
    /**
     * Конструктор адаптера
     */
    public CurrencyAdapter() {
        // Пустой конструктор для совместимости с BaseListFragment
    }
    
    /**
     * Устанавливает обработчик кликов
     */
    public void setClickListener(OnCurrencyClickListener clickListener) {
        this.clickListener = clickListener;
    }
    
    /**
     * Устанавливает обработчик длинных кликов
     */
    public void setLongClickListener(OnCurrencyLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }
    
    /**
     * Устанавливает обработчик изменений выбора
     */
    public void setSelectionListener(OnSelectionListener selectionListener) {
        this.selectionListener = selectionListener;
    }
    
    /**
     * Создает ViewHolder для элемента списка
     */
    @NonNull
    @Override
    public StandartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_currency, parent, false);
        StandartViewHolder holder = new StandartViewHolder(view);
        
        // Настраиваем обработчики для универсального ViewHolder
        holder.setItemClickListener(itemId -> {
            if (clickListener != null) {
                Currency currency = findCurrencyById(itemId);
                if (currency != null) {
                    clickListener.onCurrencyClick(currency);
                }
            }
        });
        
        holder.setItemLongClickListener(itemId -> {
            if (longClickListener != null) {
                Currency currency = findCurrencyById(itemId);
                if (currency != null) {
                    longClickListener.onCurrencyLongClick(currency);
                }
            }
        });

        // Настраиваем обработчик изменения выбора конкретного элемента
        holder.setItemSelectionListener((itemId, isSelected) -> {
            if (isSelected) {
                selectedCurrencies.add(itemId);
            } else {
                selectedCurrencies.remove(itemId);
            }
            
            // Уведомляем о изменении количества выбранных элементов
            if (selectionListener != null) {
                selectionListener.onSelectionChanged(selectedCurrencies.size());
            }
        });
        
        return holder;
    }
    
    /**
     * Привязывает данные к ViewHolder
     */
    @Override
    public void onBindViewHolder(@NonNull StandartViewHolder holder, int position) {
        Currency currency = currencies.get(position);
        holder.resetToInitialState();

        // Определяем, выбран ли текущий элемент
        boolean isSelected = selectedCurrencies.contains(currency.getId());

        Log.d(TAG, "🔄 Привязываем данные к ViewHolder: " + currency.getTitle() + " (позиция " + currency.getPosition() + ")" +
        "ID: " + currency.getId() + ", режим выбора: " + isSelectionMode + ", выбран: " + isSelected); 
        holder.bind(currency.getPosition(), currency.getTitle(), currency.getId(), 
                   isSelectionMode, isSelected);
    }

    /**
     * Возвращает количество элементов в списке валют
     */
    @Override
    public int getItemCount() {
        return currencies.size();
    }
    
    /**
     * Обновляет список валют и уведомляет адаптер о необходимости обновления
     */
    public void setCurrencies(List<Currency> currencies) {
        this.currencies = currencies != null ? currencies : new ArrayList<>();
        Log.d(TAG, "🔄 Обновляем список валют: " + this.currencies.size() + " элементов");
        notifyDataSetChanged();
    }
    
    /**
     * Включает/выключает режим выбора и очищает выбранные валюты
     */
    public void setSelectionMode(boolean enabled) {
        this.isSelectionMode = enabled;
        if (!enabled) {
            selectedCurrencies.clear();
        }
        notifyDataSetChanged();
    }
    
    /**
     * Получает список выбранных валют
     */
    public List<Currency> getSelectedCurrencies() {
        List<Currency> selected = new ArrayList<>();
        for (Integer id : selectedCurrencies) {
            Currency currency = findCurrencyById(id);
            if (currency != null) {
                selected.add(currency);
            }
        }
        return selected;
    }
    
    /**
     * Очищает выбор и уведомляет адаптер о необходимости обновления
     */
    public void clearSelection() {
        selectedCurrencies.clear();
        notifyDataSetChanged();
    }
    
    /**
     * Находит валюту по её ID
     */
    private Currency findCurrencyById(int id) {
        for (Currency currency : currencies) {
            if (currency.getId() == id) {
                return currency;
            }
        }
        return null;
    }
} 