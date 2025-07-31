package com.sadengineer.budgetmaster.currencies;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.backend.entity.Currency;

import java.util.ArrayList;
import java.util.List;

/**
 * Адаптер для отображения валют в RecyclerView
 */
public class CurrencyAdapter extends RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHolder> {
    
    private static final String TAG = "CurrencyAdapter";
    
    private List<Currency> currencies = new ArrayList<>();
    private OnCurrencyClickListener listener;
    
    /**
     * Конструктор адаптера
     * @param listener - слушатель кликов на элементах списка   
     */
    public CurrencyAdapter(OnCurrencyClickListener listener) {
        this.listener = listener;
    }    
    
    /**
     * Интерфейс для обработки кликов на элементах списка
     * @param currency - выбранная валюта
     */
    public interface OnCurrencyClickListener {
        void onCurrencyClick(Currency currency);
    }

    /**
     * Создает ViewHolder для элемента валюты
     * @param parent - родительский ViewGroup
     * @param viewType - тип ViewHolder
     * @return ViewHolder для элемента валюты
     */
    @NonNull
    @Override
    public CurrencyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_currency, parent, false);
        return new CurrencyViewHolder(view);
    }
    
    /**
     * Привязывает данные к ViewHolder
     * @param holder - ViewHolder для элемента валюты
     * @param position - позиция элемента в списке
     */
    @Override
    public void onBindViewHolder(@NonNull CurrencyViewHolder holder, int position) {
        Currency currency = currencies.get(position);
        holder.bind(currency);

    }
    
    /**
     * Возвращает количество элементов в списке
     * @return количество элементов в списке
     */
    @Override
    public int getItemCount() {
        return currencies.size();
    }
    
    /**
     * Обновляет список валют
     * @param currencies - список валют
     */
    public void setCurrencies(List<Currency> currencies) {
        this.currencies = currencies != null ? currencies : new ArrayList<>();
        Log.d(TAG, "🔄 Обновляем список валют: " + this.currencies.size() + " элементов");
        notifyDataSetChanged();
    }
    
    /**
     * ViewHolder для элемента валюты
     */
    class CurrencyViewHolder extends RecyclerView.ViewHolder {
        private TextView positionText;
        private TextView titleText;
        private TextView idText;
        
        public CurrencyViewHolder(@NonNull View itemView) {
            super(itemView);
            positionText = itemView.findViewById(R.id.currency_position);
            titleText = itemView.findViewById(R.id.currency_title);
            idText = itemView.findViewById(R.id.currency_id);
            
            // Обработчик клика
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onCurrencyClick(currencies.get(position));
                }
            });
        }
        
        public void bind(Currency currency) {
            if (currency == null) {
                Log.w(TAG, "⚠️ Currency is null");
                return;
            }
            
            // Безопасная установка текста с проверками
            try {
                positionText.setText(String.valueOf(currency.getPosition()));
                
                String title = currency.getTitle();
                if (title != null && !title.trim().isEmpty()) {
                    titleText.setText(title);
                } else {
                    titleText.setText("Без названия");
                }
                
                String idText = "ID: " + currency.getId();
                this.idText.setText(idText);
            } catch (Exception e) {
                Log.e(TAG, "❌ Ошибка установки текста: " + e.getMessage(), e);
            }
        }
    }
} 