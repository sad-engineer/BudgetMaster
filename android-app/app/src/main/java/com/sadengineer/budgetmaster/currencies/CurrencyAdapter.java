package com.sadengineer.budgetmaster.currencies;

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
    private OnCurrencyClickListener listener;
    private boolean isSelectionMode = false;
    private Set<Integer> selectedCurrencies = new HashSet<>();
    
    public interface OnCurrencyClickListener {
        void onCurrencyClick(Currency currency);
    }
    
    public interface OnSelectionChangedListener {
        void onSelectionChanged(int selectedCount);
    }
    
    private OnSelectionChangedListener selectionListener;
    
    public CurrencyAdapter(OnCurrencyClickListener listener) {
        this.listener = listener;
    }
    
    public void setSelectionListener(OnSelectionChangedListener listener) {
        this.selectionListener = listener;
    }
    
    @NonNull
    @Override
    public StandartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_currency, parent, false);
        StandartViewHolder holder = new StandartViewHolder(view);
        
        // Настраиваем обработчики для универсального ViewHolder
        holder.setItemClickListener(itemId -> {
            if (listener != null) {
                Currency currency = findCurrencyById(itemId);
                if (currency != null) {
                    listener.onCurrencyClick(currency);
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
        Currency currency = currencies.get(position);
        holder.bind(currency.getPosition(), currency.getTitle(), currency.getId(), 
                   isSelectionMode, selectedCurrencies);
    }

    /**
     * Возвращает количество элементов в списке
     */
    @Override
    public int getItemCount() {
        return currencies.size();
    }
    
    /**
     * Обновляет список валют
     */
    public void setCurrencies(List<Currency> currencies) {
        this.currencies = currencies != null ? currencies : new ArrayList<>();
        android.util.Log.d(TAG, "🔄 Обновляем список валют: " + this.currencies.size() + " элементов");
        notifyDataSetChanged();
    }
    
    /**
     * Включает/выключает режим выбора
     */
    public void setSelectionMode(boolean enabled) {
        this.isSelectionMode = enabled;
        if (!enabled) {
            selectedCurrencies.clear();
        }
        notifyDataSetChanged();
        if (selectionListener != null) {
            selectionListener.onSelectionChanged(selectedCurrencies.size());
        }
    }
    
    /**
     * Получает выбранные валюты
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
     * Очищает выбор
     */
    public void clearSelection() {
        selectedCurrencies.clear();
        notifyDataSetChanged();
        if (selectionListener != null) {
            selectionListener.onSelectionChanged(0);
        }
    }
    
    /**
     * Находит валюту по ID
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