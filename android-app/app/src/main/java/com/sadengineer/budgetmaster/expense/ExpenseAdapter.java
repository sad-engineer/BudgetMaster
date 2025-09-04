package com.sadengineer.budgetmaster.expense;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.backend.entity.Operation;
import com.sadengineer.budgetmaster.backend.service.CategoryService;
import com.sadengineer.budgetmaster.animations.StandartViewHolder;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Адаптер для отображения операций расходов
 */
public class ExpenseAdapter extends RecyclerView.Adapter<StandartViewHolder> {
    
    private static final String TAG = "ExpenseAdapter";
    
    private List<Operation> expenses = new ArrayList<>();
    private List<Operation> selectedExpenses = new ArrayList<>();
    private boolean isSelectionMode = false;
    private OnExpenseClickListener clickListener;
    private OnExpenseLongClickListener longClickListener;
    private OnSelectedExpensesChangedListener selectedListener;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private CategoryService categoryService;
    private Map<Integer, String> categoryCache = new HashMap<>();
    
    public interface OnExpenseClickListener {
        void onExpenseClick(Operation expense);
    }
    
    public interface OnExpenseLongClickListener {
        void onExpenseLongClick(Operation expense);
    }
    
    public interface OnSelectedExpensesChangedListener {
        void onSelectedExpensesChanged(List<Operation> selectedExpenses);
    }
    
    public ExpenseAdapter(OnExpenseClickListener clickListener, Context context) {
        this.clickListener = clickListener;
        this.categoryService = new CategoryService(context, "adapter");
    }
    
    @NonNull
    @Override
    public StandartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        StandartViewHolder holder = new StandartViewHolder(view);
        
        // Настраиваем обработчики
        holder.setItemClickListener(itemId -> {
            if (clickListener != null) {
                Operation expense = findExpenseById(itemId);
                if (expense != null) {
                    clickListener.onExpenseClick(expense);
                }
            }
        });
        
        holder.setItemLongClickListener(itemId -> {
            if (longClickListener != null) {
                Operation expense = findExpenseById(itemId);
                if (expense != null) {
                    longClickListener.onExpenseLongClick(expense);
                }
            }
        });
        
        holder.setItemSelectionListener((itemId, isSelected) -> {
            Operation expense = findExpenseById(itemId);
            if (expense != null) {
                if (isSelected) {
                    if (!selectedExpenses.contains(expense)) {
                        selectedExpenses.add(expense);
                    }
                } else {
                    selectedExpenses.remove(expense);
                }
                if (selectedListener != null) {
                    selectedListener.onSelectedExpensesChanged(selectedExpenses);
                }
            }
        });
        
        return holder;
    }
    
    @Override
    public void onBindViewHolder(@NonNull StandartViewHolder holder, int position) {
        Operation expense = expenses.get(position);
        if (expense != null) {
            // Получаем название категории
            String categoryName = getCategoryName(expense.getCategoryId());
            String currencyShortName = "₽";
            String dateStr = expense.getOperationDate() != null ? 
                expense.getOperationDate().format(dateFormatter) : "01.01.2024";
            
            holder.bindOperation(position + 1, categoryName, expense.getDescription(), 
                expense.getId(), expense.getAmount(), currencyShortName, dateStr, 
                isSelectionMode, selectedExpenses.contains(expense));
        }
    }
    
    @Override
    public int getItemCount() {
        return expenses.size();
    }
    
    public void setExpenses(List<Operation> expenses) {
        this.expenses = expenses != null ? expenses : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void setSelectionMode(boolean isSelectionMode) {
        this.isSelectionMode = isSelectionMode;
        if (!isSelectionMode) {
            selectedExpenses.clear();
            if (selectedListener != null) {
                selectedListener.onSelectedExpensesChanged(selectedExpenses);
            }
        }
        notifyDataSetChanged();
    }
    
    public void setLongClickListener(OnExpenseLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }
    
    public void setOnSelectedExpensesChanged(OnSelectedExpensesChangedListener listener) {
        this.selectedListener = listener;
    }
    
    /**
     * Находит операцию по ID
     */
    private Operation findExpenseById(int id) {
        for (Operation expense : expenses) {
            if (expense.getId() == id) {
                return expense;
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
