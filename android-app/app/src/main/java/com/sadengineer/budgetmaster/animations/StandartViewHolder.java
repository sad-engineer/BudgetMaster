package com.sadengineer.budgetmaster.animations;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.util.Log;
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
    private TextView sumText;
    
    private boolean isSelectionMode = false;
    private Set<Integer> selectedIds;
    private OnItemClickListener itemClickListener;
    private OnItemLongClickListener itemLongClickListener;
    private OnSelectionChangedListener selectionListener;
    
    public interface OnItemClickListener {
        void onItemClick(int itemId);
    }
    
    public interface OnItemLongClickListener {
        void onItemLongClick(int itemId);
    }

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int selectedCount);
    }
    
    /**
     * Конструктор ViewHolder
     */
    public StandartViewHolder(@NonNull View itemView) {
        super(itemView);
        
        // Ищем чекбокс по разным возможным ID
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
        // Пробуем найти чекбокс по разным ID
        CheckBox foundCheckbox = itemView.findViewById(R.id.currency_checkbox);
        if (foundCheckbox != null) {
            return foundCheckbox;
        }
        
        foundCheckbox = itemView.findViewById(R.id.account_checkbox);
        if (foundCheckbox != null) {
            return foundCheckbox;
        }
        
        // Можно добавить другие типы чекбоксов здесь
        // foundCheckbox = itemView.findViewById(R.id.income_checkbox);
        // if (foundCheckbox != null) {
        //     return foundCheckbox;
        // }
        
        // Если чекбокс не найден, возвращаем null
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
                    return true; // Показываем, что обработали длительное нажатие
                }
            }
            return false;
        });
        
        // Обработчик клика на чекбокс (если он существует)
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
        if (idText == null) {
            return -1;
        }
        
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
        bind(position, title, id, 0, isSelectionMode, selectedIds);
    }
    
    public void bind(int position, String title, int id, int sum, boolean isSelectionMode, Set<Integer> selectedIds) {
        this.isSelectionMode = isSelectionMode;
        this.selectedIds = selectedIds;
        
        // Устанавливаем значения только если элементы найдены
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
            // Форматируем сумму как валюту (копейки -> рубли)
            double rubles = sum / 100.0;
            sumText.setText(String.format("%.2f RUB", rubles));
        }
        
        // Устанавливаем полупрозрачность для счетов с позицией 0 (удаленные счета)
        if (position == 0) {
            itemView.setAlpha(0.5f); // Полупрозрачность 50%
            android.util.Log.d(TAG, "🔄 Устанавливаем полупрозрачность для счета с позицией 0: " + title);
        } else {
            itemView.setAlpha(1.0f); // Полная непрозрачность
        }
        
        // Настройка видимости чекбокса и смещения текста с анимацией
        if (isSelectionMode) {
            // При включении режима: смещение сразу, чекбокс через задержку
            animateTextPadding(true, 0);
            
            // Задержка для появления чекбокса
            if (checkbox != null) {
                checkbox.postDelayed(() -> {
                    animateCheckboxVisibility(true);
                }, CHECKBOX_ANIMATION_DELAY);
                
                checkbox.setChecked(selectedIds.contains(id));
            }
            
        } else {
            // При выключении режима: скрытие чекбокса сразу, смещение через задержку
            if (checkbox != null) {
                animateCheckboxVisibility(false);
                checkbox.setChecked(false);
            }
            
            // Задержка для смещения текста
            if (positionText != null) {
                positionText.postDelayed(() -> {
                    animateTextPadding(false, 0);
                }, TEXT_PADDING_ANIMATION_DELAY);
            }
        }
    }
    
    /**
     * Анимирует появление/исчезновение чекбокса
     */
    private void animateCheckboxVisibility(boolean show) {
        if (checkbox == null) {
            return;
        }
        
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
        if (positionText == null) {
            return;
        }
        // рассчитываем размер отступа текста
        int targetPadding = addPadding ? 
            (int) (TEXT_PADDING_DP * itemView.getContext().getResources().getDisplayMetrics().density) : 0;
        // получаем текущий отступ текста
        int currentPadding = positionText.getPaddingLeft();
        
        Log.d(TAG, "🔄 Анимация смещения текста: " + 
            currentPadding + " -> " + targetPadding + " (addPadding: " + addPadding + ", delay: " + delay + "ms)");
        
        // создаем анимацию смещения текста
        ValueAnimator paddingAnimator = ValueAnimator.ofInt(currentPadding, targetPadding);
        // устанавливаем длительность анимации
        paddingAnimator.setDuration(TEXT_PADDING_ANIMATION_DURATION);
        // добавляем обработчик обновления анимации
        paddingAnimator.addUpdateListener(animation -> {
            // получаем текущее значение анимации
            int animatedValue = (Integer) animation.getAnimatedValue();
            // устанавливаем новый отступ текста
            positionText.setPadding(animatedValue, positionText.getPaddingTop(), 
                                 positionText.getPaddingRight(), positionText.getPaddingBottom());
            
            Log.d(TAG, "📏 Текущий отступ: " + animatedValue);
        });
        // устанавливаем задержку анимации
        if (delay > 0) {
            paddingAnimator.setStartDelay(delay);
        }
        // запускаем анимацию
        paddingAnimator.start();
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
     * Устанавливает обработчик изменения выбора
     */
    public void setSelectionListener(OnSelectionChangedListener listener) {
        this.selectionListener = listener;
    }
} 