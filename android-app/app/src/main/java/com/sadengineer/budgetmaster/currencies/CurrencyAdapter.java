package com.sadengineer.budgetmaster.currencies;

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
    
    private List<Currency> currencies = new ArrayList<>();
    private OnCurrencyClickListener listener;
    
    public interface OnCurrencyClickListener {
        void onCurrencyClick(Currency currency);
    }
    
    public CurrencyAdapter(OnCurrencyClickListener listener) {
        this.listener = listener;
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
            positionText.setText(String.valueOf(currency.getPosition()));
            titleText.setText(currency.getTitle());
            idText.setText("ID: " + currency.getId());
        }
    }
} 