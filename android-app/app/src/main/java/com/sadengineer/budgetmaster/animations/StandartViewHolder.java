package com.sadengineer.budgetmaster.animations;

import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sadengineer.budgetmaster.R;

/**
 * Универсальный ViewHolder для элементов с возможностью выбора
 */
public class StandartViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "StandartViewHolder";
    
    // Размер отступа текста
    private static final int TEXT_PADDING_DP = 50;

    // UI элементы
    private CheckBox checkbox;   
    private TextView positionText;
    private TextView titleText;
    private TextView idText;
    private TextView sumText;
    
    // Состояние
    private boolean isSelectionMode = false;
    private boolean isSelected = false;
    private int boundItemId = -1;
    
    // Обработчики
    private OnItemClickListener itemClickListener;
    private OnItemLongClickListener itemLongClickListener;
    private OnItemSelectionChangedListener itemSelectionListener;
    
    public interface OnItemClickListener {
        void onItemClick(int itemId);
    }
    
    public interface OnItemLongClickListener {
        void onItemLongClick(int itemId);
    }

    public interface OnItemSelectionChangedListener {
        void onItemSelectionChanged(int itemId, boolean isSelected);
    }
    
    /**
     * Конструктор ViewHolder
     */
    public StandartViewHolder(@NonNull View itemView) {
        super(itemView);
        
        // Ищем UI элементы
        checkbox = findCheckbox(itemView);
        positionText = findPositionText(itemView);
        titleText = findTitleText(itemView);
        idText = findIdText(itemView);
        sumText = findSumText(itemView);
        
        setupClickListeners();
    }
    
    /**
     * Ищет чекбокс по разным возможным ID
     */
    private CheckBox findCheckbox(View itemView) {
        CheckBox foundCheckbox = itemView.findViewById(R.id.currency_checkbox);
        if (foundCheckbox != null) {
            return foundCheckbox;
        }
        
        foundCheckbox = itemView.findViewById(R.id.account_checkbox);
        if (foundCheckbox != null) {
            return foundCheckbox;
        }
        
        return null;
    }
    
    /**
     * Ищет TextView для позиции по разным возможным ID
     */
    private TextView findPositionText(View itemView) {
        TextView foundText = itemView.findViewById(R.id.currency_position);
        if (foundText != null) {
            return foundText;
        }
        
        foundText = itemView.findViewById(R.id.account_position);
        if (foundText != null) {
            return foundText;
        }
        
        return null;
    }
    
    /**
     * Ищет TextView для заголовка по разным возможным ID
     */
    private TextView findTitleText(View itemView) {
        TextView foundText = itemView.findViewById(R.id.currency_title);
        if (foundText != null) {
            return foundText;
        }
        
        foundText = itemView.findViewById(R.id.account_title);
        if (foundText != null) {
            return foundText;
        }
        
        return null;
    }
    
    /**
     * Ищет TextView для ID по разным возможным ID
     */
    private TextView findIdText(View itemView) {
        TextView foundText = itemView.findViewById(R.id.currency_id);
        if (foundText != null) {
            return foundText;
        }
        
        foundText = itemView.findViewById(R.id.account_id);
        if (foundText != null) {
            return foundText;
        }
        
        return null;
    }
    
    /**
     * Ищет TextView для суммы по разным возможным ID
     */
    private TextView findSumText(View itemView) {
        TextView foundText = itemView.findViewById(R.id.account_sum);
        if (foundText != null) {
            return foundText;
        }
        
        return null;
    }
    
    /**
     * Настраивает обработчики кликов
     */
    private void setupClickListeners() {
        // Обработчик клика на весь элемент
        itemView.setOnClickListener(v -> {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && itemClickListener != null) {
                if (isSelectionMode) {
                    // В режиме выбора - переключаем чекбокс
                    int itemId = getCurrentItemId();
                    if (itemId != -1) {
                        toggleSelection(itemId);
                    }
                } else {
                    // В обычном режиме - вызываем клик
                    itemClickListener.onItemClick(getCurrentItemId());
                }
            }
        });
        
        // Обработчик длительного нажатия на весь элемент
        itemView.setOnLongClickListener(v -> {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && itemLongClickListener != null) {
                int itemId = getCurrentItemId();
                if (itemId != -1) {
                    itemLongClickListener.onItemLongClick(itemId);
                    return true;
                }
            }
            return false;
        });
        
        // Обработчик клика на чекбокс
        if (checkbox != null) {
            checkbox.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    int itemId = getCurrentItemId();
                    if (itemId != -1) {
                        toggleSelection(itemId);
                    }
                }
            });
        }
    }
    
    /**
     * Получает ID текущего элемента
     */
    private int getCurrentItemId() {
        return boundItemId;
    }
    
    /**
     * Переключает выбор элемента
     */
    private void toggleSelection(int itemId) {
        boolean newSelectionState = !isSelected;
        isSelected = newSelectionState;
        
        if (itemSelectionListener != null) {
            itemSelectionListener.onItemSelectionChanged(itemId, newSelectionState);
        }
        
        if (checkbox != null) {
            checkbox.setChecked(isSelected);
        }
    }
    
    /**
     * Привязывает данные к элементу
     */
    public void bind(int position, String title, int id, boolean isSelectionMode, boolean isSelected) {
        bind(position, title, id, 0, isSelectionMode, isSelected);
    }
    
    public void bind(int position, String title, int id, int sum, boolean isSelectionMode, boolean isSelected) {
        this.boundItemId = id;
        this.isSelectionMode = isSelectionMode;
        this.isSelected = isSelected;
        
        Log.d(TAG, "🔄 bind() для элемента " + id + " (позиция " + position + "): " + 
            "isSelectionMode=" + isSelectionMode + ", isSelected=" + isSelected);
        
        // Устанавливаем значения
        if (positionText != null) {
            positionText.setText(String.valueOf(position));
        }
        if (titleText != null) {
            titleText.setText(title);
        }
        if (idText != null) {
            idText.setText("ID: " + id);
        }
        if (sumText != null) {
            double rubles = sum / 100.0;
            sumText.setText(String.format("%.2f RUB", rubles));
        }
        
        // Полупрозрачность для элементов с позицией 0
        if (position == 0) {
            itemView.setAlpha(0.5f);
        } else {
            itemView.setAlpha(1.0f);
        }
        
        // Настройка чекбокса и отступа текста
        if (isSelectionMode) {
            // Показываем чекбокс и смещаем текст
            checkbox.setVisibility(View.VISIBLE);
            checkbox.setChecked(isSelected);
            
            if (positionText != null) {
                int padding = (int) (TEXT_PADDING_DP * itemView.getContext().getResources().getDisplayMetrics().density);
                positionText.setPadding(padding, positionText.getPaddingTop(), 
                                     positionText.getPaddingRight(), positionText.getPaddingBottom());
            }
        } else {
            // Скрываем чекбокс и возвращаем текст
            checkbox.setVisibility(View.GONE);
            checkbox.setChecked(false);
            
            if (positionText != null) {
                positionText.setPadding(0, positionText.getPaddingTop(), 
                                     positionText.getPaddingRight(), positionText.getPaddingBottom());
            }
        }
    }
    
    /**
     * Устанавливает обработчик клика на элемент
     */
    public void setItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }
    
    /**
     * Устанавливает обработчик длительного клика на элемент
     */
    public void setItemLongClickListener(OnItemLongClickListener listener) {
        this.itemLongClickListener = listener;
    }

    /**
     * Устанавливает обработчик изменения выбора конкретного элемента
     */
    public void setItemSelectionListener(OnItemSelectionChangedListener listener) {
        this.itemSelectionListener = listener;
    }

    /**
     * Сбрасывает ViewHolder в начальное состояние
     */
    public void resetToInitialState() {
        // Сбрасываем UI элементы
        checkbox.setVisibility(View.GONE);
        checkbox.setChecked(false);
        
        if (positionText != null) {
            positionText.setPadding(0, positionText.getPaddingTop(), 
                                 positionText.getPaddingRight(), positionText.getPaddingBottom());
        }
        
        // Сбрасываем внутренние переменные
        this.isSelectionMode = false;
        this.isSelected = false;
        this.boundItemId = -1;
    }
} 