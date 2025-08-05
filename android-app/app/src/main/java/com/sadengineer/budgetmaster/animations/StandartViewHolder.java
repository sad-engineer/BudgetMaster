package com.sadengineer.budgetmaster.animations;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sadengineer.budgetmaster.R;

import java.util.Set;

/**
 * Универсальный ViewHolder для элементов с возможностью выбора
 */
public class StandartViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "StandartViewHolder";
    
    // Константы для анимаций
    // задержка для анимации чекбокса
    private static final int CHECKBOX_ANIMATION_DELAY = 300;
    // задержка для анимации смещения текста
    private static final int TEXT_PADDING_ANIMATION_DELAY = 300;
    // длительность анимации появления чекбокса
    private static final int CHECKBOX_FADE_IN_DURATION = 300;
    // длительность анимации исчезновения чекбокса
    private static final int CHECKBOX_FADE_OUT_DURATION = 200;
    // длительность анимации смещения текста
    private static final int TEXT_PADDING_ANIMATION_DURATION = 300;
    // размер отступа текста
    private static final int TEXT_PADDING_DP = 50;

    // чекбокс
    private CheckBox checkbox;
    private TextView positionText;
    private TextView titleText;
    private TextView idText;
    
    private boolean isSelectionMode = false;
    private Set<Integer> selectedIds;
    private OnItemClickListener itemClickListener;
    private OnSelectionChangedListener selectionListener;
    
    public interface OnItemClickListener {
        void onItemClick(int itemId);
    }
    
    public interface OnSelectionChangedListener {
        void onSelectionChanged(int selectedCount);
    }
    
    /**
     * Конструктор ViewHolder
     */
    public StandartViewHolder(@NonNull View itemView) {
        super(itemView);
        checkbox = itemView.findViewById(R.id.currency_checkbox);
        positionText = itemView.findViewById(R.id.currency_position);
        titleText = itemView.findViewById(R.id.currency_title);
        idText = itemView.findViewById(R.id.currency_id);
        
        setupClickListeners();
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
        
        // Обработчик клика на чекбокс
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
    
    /**
     * Получает ID текущего элемента
     */
    private int getCurrentItemId() {
        String idTextValue = idText.getText().toString();
        if (idTextValue.startsWith("ID: ")) {
            try {
                return Integer.parseInt(idTextValue.substring(4));
            } catch (NumberFormatException e) {
                android.util.Log.e(TAG, "Ошибка парсинга ID: " + idTextValue);
                return -1;
            }
        }
        return -1;
    }
    
    /**
     * Переключает выбор элемента
     */
    private void toggleSelection(int itemId) {
        if (selectedIds.contains(itemId)) {
            selectedIds.remove(itemId);
        } else {
            selectedIds.add(itemId);
        }
        if (selectionListener != null) {
            selectionListener.onSelectionChanged(selectedIds.size());
        }
    }
    
    /**
     * Привязывает данные к элементу
     */
    public void bind(int position, String title, int id, boolean isSelectionMode, Set<Integer> selectedIds) {
        this.isSelectionMode = isSelectionMode;
        this.selectedIds = selectedIds;
        
        positionText.setText(String.valueOf(position));
        titleText.setText(title);
        idText.setText("ID: " + id);
        
        // Настройка видимости чекбокса и смещения текста с анимацией
        if (isSelectionMode) {
            // При включении режима: смещение сразу, чекбокс через задержку
            animateTextPadding(true, 0);
            
            // Задержка для появления чекбокса
            checkbox.postDelayed(() -> {
                animateCheckboxVisibility(true);
            }, CHECKBOX_ANIMATION_DELAY);
            
            checkbox.setChecked(selectedIds.contains(id));
            
        } else {
            // При выключении режима: скрытие чекбокса сразу, смещение через задержку
            animateCheckboxVisibility(false);
            
            // Задержка для смещения текста
            positionText.postDelayed(() -> {
                animateTextPadding(false, 0);
            }, TEXT_PADDING_ANIMATION_DELAY);
            
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
            alphaAnimator.setDuration(CHECKBOX_FADE_IN_DURATION);
            alphaAnimator.start();
            
        } else {
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(checkbox, "alpha", 1f, 0f);
            alphaAnimator.setDuration(CHECKBOX_FADE_OUT_DURATION);
            alphaAnimator.start();
            
            // Скрываем чекбокс после анимации
            checkbox.postDelayed(() -> checkbox.setVisibility(View.GONE), CHECKBOX_FADE_OUT_DURATION);
        }
    }
    
    /**
     * Анимирует смещение текста
     */
    private void animateTextPadding(boolean addPadding, int delay) {
        int targetPadding = addPadding ? 
            (int) (TEXT_PADDING_DP * itemView.getContext().getResources().getDisplayMetrics().density) : 0;
        
        int currentPadding = positionText.getPaddingLeft();
        
        android.util.Log.d(TAG, "🔄 Анимация смещения текста: " + 
            currentPadding + " -> " + targetPadding + " (addPadding: " + addPadding + ", delay: " + delay + "ms)");
        
        ValueAnimator paddingAnimator = ValueAnimator.ofInt(currentPadding, targetPadding);
        paddingAnimator.setDuration(TEXT_PADDING_ANIMATION_DURATION);
        paddingAnimator.addUpdateListener(animation -> {
            int animatedValue = (Integer) animation.getAnimatedValue();
            positionText.setPadding(animatedValue, positionText.getPaddingTop(), 
                                 positionText.getPaddingRight(), positionText.getPaddingBottom());
            
            android.util.Log.d(TAG, "📏 Текущий отступ: " + animatedValue);
        });
        
        if (delay > 0) {
            paddingAnimator.setStartDelay(delay);
        }
        paddingAnimator.start();
    }
    
    /**
     * Устанавливает обработчик клика на элемент
     */
    public void setItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }
    
    /**
     * Устанавливает обработчик изменения выбора
     */
    public void setSelectionListener(OnSelectionChangedListener listener) {
        this.selectionListener = listener;
    }
} 