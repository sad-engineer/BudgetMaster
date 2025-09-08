package com.sadengineer.budgetmaster.categories;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.backend.entity.Category;
import com.sadengineer.budgetmaster.animations.StandartViewHolder;
import com.sadengineer.budgetmaster.settings.SettingsManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Адаптер для отображения категорий в RecyclerView
 */
public class CategoriesAdapter extends RecyclerView.Adapter<StandartViewHolder> {
    private static final String TAG = "CategoriesAdapter";

    private List<Category> categories = new ArrayList<>();
    // Интерфейсы для обработки событий
    public interface OnCategoryClickListener {
        void onCategoryClick(int categoryId);
    }
    
    public interface OnCategoryLongClickListener {
        void onCategoryLongClick(int categoryId);
    }
    
    public interface OnSelectedCategoriesChanged {
        void onSelectedCategoriesChanged(int categoryId, boolean isSelected);
    }
    
    // Обработчики событий
    private OnCategoryClickListener itemClickListener;
    private OnCategoryLongClickListener itemLongClickListener;
    private OnSelectedCategoriesChanged itemSelectionListener;
    
    // Контекст для инициализации SettingsManager
    private Context context;
    
    public CategoriesAdapter(Context context, OnCategoryClickListener listener) {
        this.context = context;
        this.itemClickListener = listener;
        
        // Инициализируем SettingsManager
        SettingsManager.init(context);
    }
    
    public void setOnCategoryLongClickListener(OnCategoryLongClickListener listener) {
        this.itemLongClickListener = listener;
    }
    
    public void setOnSelectedCategoriesChanged(OnSelectedCategoriesChanged listener) {
        this.externalSelectedCategoriesChanged = listener;
    }
    
    // Переменные для режима выбора
    private boolean isSelectionMode = false;
    private Set<Integer> selectedCategories = new HashSet<>();
    private OnSelectedCategoriesChanged externalSelectedCategoriesChanged;
    
    @NonNull
    @Override
    public StandartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        
        // Инициализируем менеджер настроек
        SettingsManager.init(parent.getContext());
        
        StandartViewHolder holder = new StandartViewHolder(view);
        
        // Настраиваем обработчики для универсального ViewHolder
        holder.setItemClickListener(itemId -> {
            if (itemClickListener != null) {
                Category category = findCategoryById(itemId);
                if (category != null) {
                    itemClickListener.onCategoryClick(category.getId());
                }
            }
        });
        
        holder.setItemLongClickListener(itemId -> {
            if (itemLongClickListener != null) {
                Category category = findCategoryById(itemId);
                if (category != null) {
                    itemLongClickListener.onCategoryLongClick(category.getId());
                }
            }
        });

        // Настраиваем обработчик изменения выбора конкретного элемента
        holder.setItemSelectionListener((itemId, isSelected) -> {
            if (isSelected) {
                selectedCategories.add(itemId);
            } else {
                selectedCategories.remove(itemId);
            }
            
            // Сообщаем наружу полный набор выбранных
            if (itemSelectionListener != null) {
                itemSelectionListener.onSelectedCategoriesChanged(itemId, isSelected);
            }
        });
        
        return holder;
    }
    
    @Override
    public void onBindViewHolder(@NonNull StandartViewHolder holder, int position) {
        Category category = categories.get(position);
        if (category != null) {
            // Используем метод bind() вместо отдельных setter'ов
            holder.bind(
                category.getPosition(), 
                category.getTitle(), 
                category.getId(), 
                0, // sum - не используется для категорий
                null, // shortName - не используется для категорий
                isSelectionMode, 
                selectedCategories.contains(category.getId()),
                SettingsManager.isShowPosition(),
                SettingsManager.isShowId()
            );
            
            // Настраиваем обработчики для универсального ViewHolder
            holder.setItemClickListener(itemId -> {
                if (itemClickListener != null) {
                    Category clickedCategory = findCategoryById(itemId);
                    if (clickedCategory != null) {
                        itemClickListener.onCategoryClick(clickedCategory.getId());
                    }
                }
            });
            
            holder.setItemLongClickListener(itemId -> {
                if (itemLongClickListener != null) {
                    Category longClickedCategory = findCategoryById(itemId);
                    if (longClickedCategory != null) {
                        itemLongClickListener.onCategoryLongClick(longClickedCategory.getId());
                    }
                }
            });
            
            holder.setItemSelectionListener((itemId, isSelected) -> {
                if (isSelected) {
                    selectedCategories.add(itemId);
                } else {
                    selectedCategories.remove(itemId);
                }
                
                // Сообщаем наружу полный набор выбранных
                if (externalSelectedCategoriesChanged != null) {
                    externalSelectedCategoriesChanged.onSelectedCategoriesChanged(itemId, isSelected);
                }
            });
        }
    }
    
    @Override
    public int getItemCount() {
        return categories.size();
    }
    
    /**
     * Устанавливает список категорий для отображения
     */
    public void setCategories(List<Category> categories) {
        this.categories = categories != null ? categories : new ArrayList<>();
        notifyDataSetChanged();
        Log.d(TAG, "Установлено категорий: " + this.categories.size());
    }
    
    /**
     * Включает/выключает режим выбора
     */
    public void setSelectionMode(boolean enabled) {
        this.isSelectionMode = enabled;
        if (!enabled) {
            clearSelection();
        }
        notifyDataSetChanged();
        Log.d(TAG, "Режим выбора категорий: " + (enabled ? "включен" : "выключен"));
    }
    
    /**
     * Получает список всех категорий
     * @return список категорий
     */
    public List<Category> getCategories() {
        return new ArrayList<>(categories);
    }
    
    /**
     * Получает список выбранных категорий
     * @return список выбранных категорий
     */
    public List<Category> getSelectedCategories() {
        List<Category> selected = new ArrayList<>();
        for (Category category : categories) {
            if (selectedCategories.contains(category.getId())) {
                selected.add(category);
            }
        }
        return selected;
    }
    
    /**
     * Очищает выбор категорий
     */
    public void clearSelection() {
        selectedCategories.clear();
        notifyDataSetChanged();
        Log.d(TAG, "Выбор категорий очищен");
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
}
