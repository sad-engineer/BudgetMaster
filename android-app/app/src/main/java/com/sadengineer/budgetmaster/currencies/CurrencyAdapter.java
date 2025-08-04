package com.sadengineer.budgetmaster.currencies;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.backend.entity.Currency;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Адаптер для отображения валют в RecyclerView
 */
public class CurrencyAdapter extends RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHolder> {
    
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
    public CurrencyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_currency, parent, false);
        return new CurrencyViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CurrencyViewHolder holder, int position) {
        Currency currency = currencies.get(position);
        holder.bind(currency);
    }
    
    @Override
    public int getItemCount() {
        return currencies.size();
    }
    
    /**
     * Обновляет список валют
     */
    public void setCurrencies(List<Currency> currencies) {
        this.currencies = currencies != null ? currencies : new ArrayList<>();
        android.util.Log.d("CurrencyAdapter", "🔄 Обновляем список валют: " + this.currencies.size() + " элементов");
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
            for (Currency currency : currencies) {
                if (currency.getId() == id) {
                    selected.add(currency);
                    break;
                }
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
     * ViewHolder для элемента валюты
     */
    class CurrencyViewHolder extends RecyclerView.ViewHolder {
        private CheckBox checkbox;
        private TextView positionText;
        private TextView titleText;
        private TextView idText;
        
        public CurrencyViewHolder(@NonNull View itemView) {
            super(itemView);
            checkbox = itemView.findViewById(R.id.currency_checkbox);
            positionText = itemView.findViewById(R.id.currency_position);
            titleText = itemView.findViewById(R.id.currency_title);
            idText = itemView.findViewById(R.id.currency_id);
            
            // Обработчик клика на весь элемент
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Currency currency = currencies.get(position);
                    
                    if (isSelectionMode) {
                        // В режиме выбора - переключаем чекбокс
                        toggleSelection(currency.getId());
                    } else if (listener != null) {
                        // В обычном режиме - вызываем клик
                        listener.onCurrencyClick(currency);
                    }
                }
            });
            
            // Обработчик клика на чекбокс
            checkbox.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Currency currency = currencies.get(position);
                    toggleSelection(currency.getId());
                }
            });
        }
        
        private void toggleSelection(int currencyId) {
            if (selectedCurrencies.contains(currencyId)) {
                selectedCurrencies.remove(currencyId);
            } else {
                selectedCurrencies.add(currencyId);
            }
            notifyDataSetChanged();
            if (selectionListener != null) {
                selectionListener.onSelectionChanged(selectedCurrencies.size());
            }
        }
        
        public void bind(Currency currency) {
            positionText.setText(String.valueOf(currency.getPosition()));
            titleText.setText(currency.getTitle());
            idText.setText("ID: " + currency.getId());
            
            // Настройка видимости чекбокса и смещения текста с анимацией
            if (isSelectionMode) {
                // При включении режима: смещение сразу, чекбокс через 300ms
                animateTextPadding(true, 0); // Смещение сразу
                
                // Задержка для появления чекбокса
                checkbox.postDelayed(() -> {
                    animateCheckboxVisibility(true);
                }, 300);
                
                checkbox.setChecked(selectedCurrencies.contains(currency.getId()));
                
            } else {
                // При выключении режима: скрытие чекбокса сразу, смещение через 300ms
                animateCheckboxVisibility(false); // Скрытие сразу
                
                // Задержка для смещения текста
                positionText.postDelayed(() -> {
                    animateTextPadding(false, 0);
                }, 300);
                
                checkbox.setChecked(false);
            }
        }
        
        /**
         * Анимирует появление/исчезновение чекбокса
         */
        private void animateCheckboxVisibility(boolean show) {
            if (show) {
                checkbox.setVisibility(View.VISIBLE);
                checkbox.setAlpha(0f);
                
                ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(checkbox, "alpha", 0f, 1f);
                alphaAnimator.setDuration(300);
                alphaAnimator.start();
                
            } else {
                ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(checkbox, "alpha", 1f, 0f);
                alphaAnimator.setDuration(200);
                alphaAnimator.start();
                
                // Скрываем чекбокс после анимации
                checkbox.postDelayed(() -> checkbox.setVisibility(View.GONE), 200);
            }
        }
        
        /**
         * Анимирует смещение текста
         */
        private void animateTextPadding(boolean addPadding, int delay) {
            int targetPadding = addPadding ? 
                (int) (50 * itemView.getContext().getResources().getDisplayMetrics().density) : 0;
            
            int currentPadding = positionText.getPaddingLeft();
            
            android.util.Log.d("CurrencyAdapter", "🔄 Анимация смещения текста: " + 
                currentPadding + " -> " + targetPadding + " (addPadding: " + addPadding + ", delay: " + delay + "ms)");
            
            ValueAnimator paddingAnimator = ValueAnimator.ofInt(currentPadding, targetPadding);
            paddingAnimator.setDuration(300);
            paddingAnimator.addUpdateListener(animation -> {
                int animatedValue = (Integer) animation.getAnimatedValue();
                positionText.setPadding(animatedValue, positionText.getPaddingTop(), 
                                     positionText.getPaddingRight(), positionText.getPaddingBottom());
                
                android.util.Log.d("CurrencyAdapter", "📏 Текущий отступ: " + animatedValue);
            });
            
            if (delay > 0) {
                paddingAnimator.setStartDelay(delay);
            }
            paddingAnimator.start();
        }
    }
} 