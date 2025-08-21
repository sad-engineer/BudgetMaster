package com.sadengineer.budgetmaster.budget;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.animations.StandartViewHolder;
import com.sadengineer.budgetmaster.backend.entity.Budget;
import com.sadengineer.budgetmaster.backend.entity.Category;
import com.sadengineer.budgetmaster.backend.entity.Currency;
import com.sadengineer.budgetmaster.settings.SettingsManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Адаптер для отображения бюджетов в RecyclerView
 */
public class BudgetAdapter extends RecyclerView.Adapter<StandartViewHolder> {
    
    private static final String TAG = "BudgetAdapter";
    
    private List<Budget> budgets = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();
    private List<Currency> currencies = new ArrayList<>();
    private OnBudgetClickListener clickListener;
    private OnBudgetLongClickListener longClickListener;
    private boolean isSelectionMode = false;
    private List<Integer> selectedBudgets = new ArrayList<>();
    private OnSelectionChangedListener selectionListener;
    
    /**
     * Интерфейс для обработки кликов по бюджету
     */
    public interface OnBudgetClickListener {
        void onBudgetClick(Budget budget);
    }
    
    /**
     * Интерфейс для обработки длительных кликов по бюджету
     */
    public interface OnBudgetLongClickListener {
        void onBudgetLongClick(Budget budget);
    }
    
    /**
     * Интерфейс для уведомления об изменении выбора
     */
    public interface OnSelectionChangedListener {
        void onSelectionChanged(int selectedCount);
    }
    
    /**
     * Создает ViewHolder для элемента списка
     */
    @NonNull
    @Override
    public StandartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        
        // Инициализируем менеджер настроек
        SettingsManager.init(parent.getContext());
        
        StandartViewHolder holder = new StandartViewHolder(
            inflater.inflate(R.layout.item_account, parent, false)
        );
        
        // Устанавливаем слушатели
        holder.setItemClickListener(itemId -> {
            if (isSelectionMode) {
                toggleSelection(itemId);
            } else if (clickListener != null) {
                Budget budget = findBudgetById(itemId);
                if (budget != null) {
                    clickListener.onBudgetClick(budget);
                }
            }
        });
        
        holder.setItemLongClickListener(itemId -> {
            if (longClickListener != null) {
                Budget budget = findBudgetById(itemId);
                if (budget != null) {
                    longClickListener.onBudgetLongClick(budget);
                }
            }
        });
        
        return holder;
    }
    
    /**
     * Привязывает данные к ViewHolder
     */
    @Override
    public void onBindViewHolder(@NonNull StandartViewHolder holder, int position) {
        if (position < budgets.size()) {
            Budget budget = budgets.get(position);
            Category category = findCategoryById(budget.getCategoryId());
            Currency currency = findCurrencyById(budget.getCurrencyId());
            
            String title = category != null ? category.getTitle() : "Неизвестная категория";
            String shortName = currency != null ? currency.getShortName() : "RUB";
            
            Log.d(TAG, "onBindViewHolder: бюджет ID=" + budget.getId() + 
                      ", сумма=" + budget.getAmount() + 
                      ", категория=" + title + 
                      ", валюта=" + shortName);
            
            holder.bind(
                budget.getPosition(),
                title,
                budget.getId(),
                budget.getAmount(),
                shortName,
                isSelectionMode,
                selectedBudgets.contains(budget.getId()),
                SettingsManager.isShowPosition(),
                SettingsManager.isShowId()
            );
        }
    }
    
    /**
     * Возвращает количество элементов в списке бюджетов
     */
    @Override
    public int getItemCount() {
        return budgets.size();
    }
    
    /**
     * Устанавливает список бюджетов
     */
    public void setBudgets(List<Budget> budgets) {
        this.budgets = budgets != null ? budgets : new ArrayList<>();
        notifyDataSetChanged();
        Log.d(TAG, "Установлено бюджетов: " + this.budgets.size());
    }
    
    /**
     * Устанавливает список категорий
     */
    public void setCategories(List<Category> categories) {
        this.categories = categories != null ? categories : new ArrayList<>();
        Log.d(TAG, "Установлено категорий: " + this.categories.size());
    }
    
    /**
     * Устанавливает список валют
     */
    public void setCurrencies(List<Currency> currencies) {
        this.currencies = currencies != null ? currencies : new ArrayList<>();
        Log.d(TAG, "Установлено валют: " + this.currencies.size());
    }
    
    /**
     * Устанавливает слушатель кликов
     */
    public void setClickListener(OnBudgetClickListener listener) {
        this.clickListener = listener;
    }
    
    /**
     * Устанавливает слушатель длительных кликов
     */
    public void setLongClickListener(OnBudgetLongClickListener listener) {
        this.longClickListener = listener;
    }
    
    /**
     * Устанавливает слушатель изменений выбора
     */
    public void setSelectionListener(OnSelectionChangedListener listener) {
        this.selectionListener = listener;
    }
    
    /**
     * Устанавливает режим выбора
     */
    public void setSelectionMode(boolean enabled) {
        this.isSelectionMode = enabled;
        if (!enabled) {
            selectedBudgets.clear();
        }
        notifyDataSetChanged();
        Log.d(TAG, "Режим выбора: " + (enabled ? "включен" : "выключен"));
    }
    
    /**
     * Переключает выбор бюджета
     */
    private void toggleSelection(int budgetId) {
        if (selectedBudgets.contains(budgetId)) {
            selectedBudgets.remove(Integer.valueOf(budgetId));
        } else {
            selectedBudgets.add(budgetId);
        }
        
        notifyDataSetChanged();
        
        if (selectionListener != null) {
            selectionListener.onSelectionChanged(selectedBudgets.size());
        }
        
        Log.d(TAG, "Выбрано бюджетов: " + selectedBudgets.size());
    }
    
    /**
     * Возвращает выбранные бюджеты
     */
    public List<Budget> getSelectedBudgets() {
        List<Budget> selected = new ArrayList<>();
        for (Integer id : selectedBudgets) {
            Budget budget = findBudgetById(id);
            if (budget != null) {
                selected.add(budget);
            }
        }
        return selected;
    }
    
    /**
     * Находит бюджет по ID
     */
    private Budget findBudgetById(int id) {
        for (Budget budget : budgets) {
            if (budget.getId() == id) {
                return budget;
            }
        }
        return null;
    }
    
    /**
     * Находит категорию по ID
     */
    private Category findCategoryById(int id) {
        for (Category category : categories) {
            if (category.getId() == id) {
                return category;
            }
        }
        return null;
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
