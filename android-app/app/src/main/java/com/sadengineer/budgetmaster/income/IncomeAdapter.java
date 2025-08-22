package com.sadengineer.budgetmaster.income;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.backend.entity.Operation;
import com.sadengineer.budgetmaster.backend.entity.Category;
import com.sadengineer.budgetmaster.backend.service.CategoryService;
import com.sadengineer.budgetmaster.animations.StandartViewHolder;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Адаптер для отображения операций доходов
 */
public class IncomeAdapter extends RecyclerView.Adapter<StandartViewHolder> {
    
    private static final String TAG = "IncomeAdapter";
    
    private List<Operation> incomes = new ArrayList<>();
    private List<Operation> selectedIncomes = new ArrayList<>();
    private boolean isSelectionMode = false;
    private OnIncomeClickListener clickListener;
    private OnIncomeLongClickListener longClickListener;
    private OnSelectedIncomesChangedListener selectedListener;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private CategoryService categoryService;
    private Map<Integer, String> categoryCache = new HashMap<>();
    
    public interface OnIncomeClickListener {
        void onIncomeClick(Operation income);
    }
    
    public interface OnIncomeLongClickListener {
        void onIncomeLongClick(Operation income);
    }
    
    public interface OnSelectedIncomesChangedListener {
        void onSelectedIncomesChanged(List<Operation> selectedIncomes);
    }
    
    public IncomeAdapter(OnIncomeClickListener clickListener, Context context) {
        this.clickListener = clickListener;
        this.categoryService = new CategoryService(context, "adapter");
    }
    
    @NonNull
    @Override
    public StandartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_income, parent, false);
        StandartViewHolder holder = new StandartViewHolder(view);
        
        // Настраиваем обработчики
        holder.setItemClickListener(itemId -> {
            if (clickListener != null) {
                Operation income = findIncomeById(itemId);
                if (income != null) {
                    clickListener.onIncomeClick(income);
                }
            }
        });
        
        holder.setItemLongClickListener(itemId -> {
            if (longClickListener != null) {
                Operation income = findIncomeById(itemId);
                if (income != null) {
                    longClickListener.onIncomeLongClick(income);
                }
            }
        });
        
        holder.setItemSelectionListener((itemId, isSelected) -> {
            Operation income = findIncomeById(itemId);
            if (income != null) {
                if (isSelected) {
                    if (!selectedIncomes.contains(income)) {
                        selectedIncomes.add(income);
                    }
                } else {
                    selectedIncomes.remove(income);
                }
                if (selectedListener != null) {
                    selectedListener.onSelectedIncomesChanged(selectedIncomes);
                }
            }
        });
        
        return holder;
    }
    
    @Override
    public void onBindViewHolder(@NonNull StandartViewHolder holder, int position) {
        Operation income = incomes.get(position);
        if (income != null) {
            // Получаем название категории
            String categoryName = getCategoryName(income.getCategoryId());
            String currencyShortName = "₽";
            String dateStr = income.getOperationDate() != null ? 
                income.getOperationDate().format(dateFormatter) : "01.01.2024";
            
            holder.bindOperation(position + 1, categoryName, income.getDescription(), 
                income.getId(), income.getAmount(), currencyShortName, dateStr, 
                isSelectionMode, selectedIncomes.contains(income));
        }
    }
    
    @Override
    public int getItemCount() {
        return incomes.size();
    }
    
    public void setIncomes(List<Operation> incomes) {
        this.incomes = incomes != null ? incomes : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void setSelectionMode(boolean isSelectionMode) {
        this.isSelectionMode = isSelectionMode;
        if (!isSelectionMode) {
            selectedIncomes.clear();
            if (selectedListener != null) {
                selectedListener.onSelectedIncomesChanged(selectedIncomes);
            }
        }
        notifyDataSetChanged();
    }
    
    public void setLongClickListener(OnIncomeLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }
    
    public void setOnSelectedIncomesChanged(OnSelectedIncomesChangedListener listener) {
        this.selectedListener = listener;
    }
    
    /**
     * Находит операцию по ID
     */
    private Operation findIncomeById(int id) {
        for (Operation income : incomes) {
            if (income.getId() == id) {
                return income;
            }
        }
        return null;
    }
    
    /**
     * Получает название категории по ID
     */
    private String getCategoryName(int categoryId) {
        // Проверяем кэш
        if (categoryCache.containsKey(categoryId)) {
            return categoryCache.get(categoryId);
        }
        
        // Если нет в кэше, возвращаем заглушку и загружаем асинхронно
        String placeholder = "Категория " + categoryId;
        categoryCache.put(categoryId, placeholder);
        
        // Загружаем категорию асинхронно
        categoryService.getById(categoryId).observeForever(category -> {
            if (category != null) {
                categoryCache.put(categoryId, category.getTitle());
                // Уведомляем адаптер об изменении данных
                notifyDataSetChanged();
            }
        });
        
        return placeholder;
    }
}
