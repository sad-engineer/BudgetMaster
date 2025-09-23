package com.sadengineer.budgetmaster.animations;
 
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.formatters.CurrencyAmountFormatter;
import com.sadengineer.budgetmaster.utils.LogManager;

/**
 * Универсальный ViewHolder для элементов с возможностью выбора
 */
public class StandartViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "StandartViewHolder";
    
    // Размер отступа текста
    private static final int TEXT_PADDING_DP = 50;

    // UI элементы
    private CheckBox checkbox;
    private ImageView checkboxIcon;   
    private TextView positionText;
    private TextView titleText;
    private TextView idText;
    private TextView sumText;
    private TextView shortNameText;
    private TextView dateText;
    private TextView descriptionText;
    
    // Состояние
    private boolean isSelectionMode = false;
    private boolean isSelected = false;
    private int boundItemId = -1;
    
    // Форматтер для сумм
    private CurrencyAmountFormatter formatter = new CurrencyAmountFormatter();
    
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
        checkboxIcon = findCheckboxIcon(itemView);
        positionText = findPositionText(itemView);
        titleText = findTitleText(itemView);
        idText = findIdText(itemView);
        sumText = findSumText(itemView);
        shortNameText = findShortNameText(itemView);
        dateText = findDateText(itemView);
        descriptionText = findDescriptionText(itemView);
        
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
        
        foundCheckbox = itemView.findViewById(R.id.category_checkbox);
        if (foundCheckbox != null) {
            return foundCheckbox;
        }
        
        foundCheckbox = itemView.findViewById(R.id.expense_checkbox);
        if (foundCheckbox != null) {
            return foundCheckbox;
        }
        
        foundCheckbox = itemView.findViewById(R.id.income_checkbox);
        if (foundCheckbox != null) {
            return foundCheckbox;
        }
        
        return null;
    }
    
    /**
     * Ищет иконку чекбокса по разным возможным ID
     */
    private ImageView findCheckboxIcon(View itemView) {
        ImageView foundIcon = itemView.findViewById(R.id.currency_checkbox_icon);
        if (foundIcon != null) {
            return foundIcon;
        }
        
        foundIcon = itemView.findViewById(R.id.account_checkbox_icon);
        if (foundIcon != null) {
            return foundIcon;
        }
        
        foundIcon = itemView.findViewById(R.id.category_checkbox_icon);
        if (foundIcon != null) {
            return foundIcon;
        }
        
        foundIcon = itemView.findViewById(R.id.expense_checkbox_icon);
        if (foundIcon != null) {
            return foundIcon;
        }
        
        foundIcon = itemView.findViewById(R.id.income_checkbox_icon);
        if (foundIcon != null) {
            return foundIcon;
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
        
        foundText = itemView.findViewById(R.id.category_position);
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
        
        foundText = itemView.findViewById(R.id.category_title);
        if (foundText != null) {
            return foundText;
        }
        
        foundText = itemView.findViewById(R.id.expense_category);
        if (foundText != null) {
            return foundText;
        }
        
        foundText = itemView.findViewById(R.id.income_category);
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
        
        foundText = itemView.findViewById(R.id.category_id);
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
        
        foundText = itemView.findViewById(R.id.expense_amount);
        if (foundText != null) {
            return foundText;
        }
        
        foundText = itemView.findViewById(R.id.income_amount);
        if (foundText != null) {
            return foundText;
        }
        
        return null;
    }
    
    /**
     * Ищет TextView для короткого имени валюты
     */
    private TextView findShortNameText(View itemView) {
        TextView foundText = itemView.findViewById(R.id.currency_short_name);
        if (foundText != null) {
            return foundText;
        }
        
        foundText = itemView.findViewById(R.id.expense_currency_short);
        if (foundText != null) {
            return foundText;
        }
        
        foundText = itemView.findViewById(R.id.income_currency_short);
        if (foundText != null) {
            return foundText;
        }
        
        return null;
    }
    
    /**
     * Ищет TextView для даты
     */
    private TextView findDateText(View itemView) {
        TextView foundText = itemView.findViewById(R.id.expense_date);
        if (foundText != null) {
            return foundText;
        }
        
        foundText = itemView.findViewById(R.id.income_date);
        if (foundText != null) {
            return foundText;
        }
        
        return null;
    }
    
    /**
     * Ищет TextView для описания
     */
    private TextView findDescriptionText(View itemView) {
        TextView foundText = itemView.findViewById(R.id.expense_description);
        if (foundText != null) {
            return foundText;
        }
        
        foundText = itemView.findViewById(R.id.income_description);
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
        
        // Обработчик клика на иконку чекбокса
        if (checkboxIcon != null) {
            checkboxIcon.setOnClickListener(v -> {
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
        
        if (checkboxIcon != null) {
            checkboxIcon.setImageResource(isSelected ? 
                R.drawable.ic_checkbox_checked : R.drawable.ic_checkbox_unchecked);
        } else if (checkbox != null) {
            checkbox.setChecked(isSelected);
        }
    }
    
    /**
     * Привязывает данные к элементу
     */
    public void bind(int position, String title, int id, boolean isSelectionMode, boolean isSelected) {
        bind(position, title, id, 0L, null, isSelectionMode, isSelected, true, true);
    }
    
    public void bind(int position, String title, int id, long sum, boolean isSelectionMode, boolean isSelected) {
        bind(position, title, id, sum, null, isSelectionMode, isSelected, true, true);
    }
    
    public void bind(int position, String title, int id, long sum, String shortName, boolean isSelectionMode, boolean isSelected) {
        bind(position, title, id, sum, shortName, isSelectionMode, isSelected, true, true);
    }
    
    public void bind(int position, String title, int id, long sum, String shortName, boolean isSelectionMode, boolean isSelected, boolean showPosition, boolean showId) {
        this.boundItemId = id;
        this.isSelectionMode = isSelectionMode;
        this.isSelected = isSelected;
        
        LogManager.d(TAG, "bind() для элемента " + id + " (позиция " + position + "): " + 
            "isSelectionMode=" + isSelectionMode + ", isSelected=" + isSelected + 
            ", showPosition=" + showPosition + ", showId=" + showId);
        
        // Устанавливаем значения с учетом настроек отображения
        if (positionText != null) {
            if (showPosition) {
                positionText.setText(String.valueOf(position));
                positionText.setVisibility(View.VISIBLE);
            } else {
                positionText.setVisibility(View.GONE);
            }
        }
        if (titleText != null) {
            titleText.setText(title);
        }
        if (idText != null) {
            if (showId) {
                idText.setText("ID: " + id);
                idText.setVisibility(View.VISIBLE);
            } else {
                idText.setVisibility(View.GONE);
            }
        }
        if (sumText != null) {
            double rubles = sum / 100.0;
            String currencySymbol = (shortName != null && !shortName.isEmpty()) ? shortName : "RUB";
            String formattedSum = formatter.formatCompact(rubles) + " " + currencySymbol;
            sumText.setText(formattedSum);
            LogManager.d(TAG, "Установлена сумма: " + formattedSum + " (исходная сумма в копейках: " + sum + ")");
        }
        if (shortNameText != null) {
            shortNameText.setText(shortName != null ? shortName : "");
        }
        
        // Полупрозрачность для элементов с позицией 0
        if (position == 0) {
            itemView.setAlpha(0.5f);
        } else {
            itemView.setAlpha(1.0f);
        }
        
        // Настройка чекбокса и отступа текста
        if (isSelectionMode) {
            // Показываем иконку чекбокса и смещаем текст
            if (checkboxIcon != null) {
                checkboxIcon.setVisibility(View.VISIBLE);
                checkboxIcon.setImageResource(isSelected ? 
                    R.drawable.ic_checkbox_checked : R.drawable.ic_checkbox_unchecked);
            } else {
                // Fallback к стандартному чекбоксу
                checkbox.setVisibility(View.VISIBLE);
                checkbox.setChecked(isSelected);
            }
            
            // Применяем отступ к позиции, если она видима, иначе к названию
            int padding = (int) (TEXT_PADDING_DP * itemView.getContext().getResources().getDisplayMetrics().density);
            
            if (positionText != null && showPosition) {
                // Если позиция видима, применяем отступ к ней
                positionText.setPadding(padding, positionText.getPaddingTop(), 
                                     positionText.getPaddingRight(), positionText.getPaddingBottom());
            } else if (titleText != null && !showPosition) {
                // Если позиция скрыта, применяем отступ к названию
                titleText.setPadding(padding, titleText.getPaddingTop(), 
                                   titleText.getPaddingRight(), titleText.getPaddingBottom());
            }
        } else {
            // Скрываем чекбокс и возвращаем текст
            if (checkboxIcon != null) {
                checkboxIcon.setVisibility(View.GONE);
            } else {
                checkbox.setVisibility(View.GONE);
                checkbox.setChecked(false);
            }
            
            // Сбрасываем отступы
            if (positionText != null) {
                positionText.setPadding(0, positionText.getPaddingTop(), 
                                     positionText.getPaddingRight(), positionText.getPaddingBottom());
            }
            if (titleText != null) {
                titleText.setPadding(0, titleText.getPaddingTop(), 
                                   titleText.getPaddingRight(), titleText.getPaddingBottom());
            }
        }
    }
    
    /**
     * Привязывает данные операции (доход/расход)
     */
    public void bindOperation(int position, String category, String description, int id, long sum, String shortName, String date, boolean isSelectionMode, boolean isSelected) {
        this.boundItemId = id;
        this.isSelectionMode = isSelectionMode;
        this.isSelected = isSelected;
        
        LogManager.d(TAG, "bindOperation() для операции " + id + " (позиция " + position + "): " + 
            "isSelectionMode=" + isSelectionMode + ", isSelected=" + isSelected);
        
        // Устанавливаем значения
        if (titleText != null) {
            titleText.setText(category);
        }
        if (descriptionText != null) {
            descriptionText.setText(description);
        }
        if (dateText != null) {
            dateText.setText(date);
        }
        if (sumText != null) {
            double rubles = sum / 100.0;
            String formattedSum = formatter.formatCompact(rubles);
            sumText.setText(formattedSum);
            LogManager.d(TAG, "Установлена сумма операции: " + formattedSum + " (исходная сумма в копейках: " + sum + ")");
        }
        if (shortNameText != null) {
            shortNameText.setText(shortName != null ? shortName : "₽");
        }
        
        // Полупрозрачность для элементов с позицией 0
        if (position == 0) {
            itemView.setAlpha(0.5f);
        } else {
            itemView.setAlpha(1.0f);
        }
        
        // Настройка чекбокса и отступа текста
        if (isSelectionMode) {
            // Показываем иконку чекбокса и смещаем текст
            if (checkboxIcon != null) {
                checkboxIcon.setVisibility(View.VISIBLE);
                checkboxIcon.setImageResource(isSelected ? 
                    R.drawable.ic_checkbox_checked : R.drawable.ic_checkbox_unchecked);
            } else {
                // Fallback к стандартному чекбоксу
                checkbox.setVisibility(View.VISIBLE);
                checkbox.setChecked(isSelected);
            }
            
            // Применяем отступ к дате
            int padding = (int) (TEXT_PADDING_DP * itemView.getContext().getResources().getDisplayMetrics().density);
            if (dateText != null) {
                dateText.setPadding(padding, dateText.getPaddingTop(), 
                                 dateText.getPaddingRight(), dateText.getPaddingBottom());
            }
        } else {
            // Скрываем чекбокс и возвращаем текст
            if (checkboxIcon != null) {
                checkboxIcon.setVisibility(View.GONE);
            } else {
                checkbox.setVisibility(View.GONE);
                checkbox.setChecked(false);
            }
            
            // Сбрасываем отступы
            if (dateText != null) {
                dateText.setPadding(0, dateText.getPaddingTop(), 
                                 dateText.getPaddingRight(), dateText.getPaddingBottom());
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
        if (checkboxIcon != null) {
            checkboxIcon.setVisibility(View.GONE);
        } else {
            checkbox.setVisibility(View.GONE);
            checkbox.setChecked(false);
        }
        
        // Сбрасываем отступы для всех текстовых полей
        if (positionText != null) {
            positionText.setPadding(0, positionText.getPaddingTop(), 
                                 positionText.getPaddingRight(), positionText.getPaddingBottom());
        }
        if (titleText != null) {
            titleText.setPadding(0, titleText.getPaddingTop(), 
                               titleText.getPaddingRight(), titleText.getPaddingBottom());
        }
        
        // Сбрасываем внутренние переменные
        this.isSelectionMode = false;
        this.isSelected = false;
        this.boundItemId = -1;
    }
} 