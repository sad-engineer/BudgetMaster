package com.sadengineer.budgetmaster.categories;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.backend.entity.Category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Адаптер для отображения категорий в виде дерева
 */
public class CategoryTreeAdapter extends RecyclerView.Adapter<CategoryTreeAdapter.CategoryTreeViewHolder> {
    private static final String TAG = "CategoryTreeAdapter";

    private List<CategoryTreeItem> treeItems = new ArrayList<>();
    private List<Category> allCategories = new ArrayList<>();
    
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
    
    // Переменные для режима выбора
    private boolean isSelectionMode = false;
    private Set<Integer> selectedCategories = new HashSet<>();
    
    public CategoryTreeAdapter(OnCategoryClickListener listener) {
        this.itemClickListener = listener;
    }
    
    public void setOnCategoryLongClickListener(OnCategoryLongClickListener listener) {
        this.itemLongClickListener = listener;
    }
    
    public void setOnSelectedCategoriesChanged(OnSelectedCategoriesChanged listener) {
        this.itemSelectionListener = listener;
    }
    
    @NonNull
    @Override
    public CategoryTreeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_tree, parent, false);
        return new CategoryTreeViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CategoryTreeViewHolder holder, int position) {
        CategoryTreeItem treeItem = treeItems.get(position);
        if (treeItem != null) {
            holder.bind(treeItem, isSelectionMode, selectedCategories.contains(treeItem.getCategoryId()));
        }
    }
    
    @Override
    public int getItemCount() {
        return treeItems.size();
    }
    
    /**
     * Устанавливает список категорий и строит дерево
     */
    public void setCategories(List<Category> categories) {
        this.allCategories = categories != null ? categories : new ArrayList<>();
        buildTree();
        notifyDataSetChanged();
        Log.d(TAG, "Установлено категорий: " + this.allCategories.size() + ", элементов дерева: " + this.treeItems.size());
    }
    
    /**
     * Строит древовидную структуру из плоского списка категорий
     */
    private void buildTree() {
        treeItems.clear();
        
        Log.d(TAG, "Строим дерево из " + allCategories.size() + " категорий");
        
        // Создаем карту категорий по ID для быстрого поиска
        Map<Integer, Category> categoryMap = new HashMap<>();
        for (Category category : allCategories) {
            categoryMap.put(category.getId(), category);
        }
        
        // Создаем карту дочерних элементов
        Map<Integer, List<Category>> childrenMap = new HashMap<>();
        for (Category category : allCategories) {
            Integer parentId = category.getParentId();
            if (parentId != null && parentId > 0) { // Не корневая категория
                childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(category);
                Log.d(TAG, "Дочерняя категория: " + category.getTitle() + " -> родитель ID: " + parentId);
            }
        }
        
        Log.d(TAG, "Найдено " + childrenMap.size() + " родительских категорий с дочерними элементами");
        
        // Добавляем корневые категории (parentId == null или <= 0)
        for (Category category : allCategories) {
            Integer parentId = category.getParentId();
            if (parentId == null || parentId <= 0) {
                Log.d(TAG, "Корневая категория: " + category.getTitle() + " (ID: " + category.getId() + ")");
                addCategoryToTree(category, 0, childrenMap, new HashMap<>());
            }
        }
        
        Log.d(TAG, "Построено дерево с " + treeItems.size() + " элементами");
    }
    
    /**
     * Рекурсивно добавляет категорию и её дочерние элементы в дерево
     */
    private void addCategoryToTree(Category category, int level, Map<Integer, List<Category>> childrenMap, Map<Integer, Boolean> expansionState) {
        // Добавляем текущую категорию
        CategoryTreeItem treeItem = new CategoryTreeItem(category, level);
        
        // Восстанавливаем состояние развернутости
        Boolean expanded = expansionState.get(category.getId());
        if (expanded != null) {
            treeItem.setExpanded(expanded);
        }
        
        // Проверяем, есть ли дочерние элементы
        List<Category> children = childrenMap.get(category.getId());
        if (children != null && !children.isEmpty()) {
            treeItem.setHasChildren(true);
            treeItems.add(treeItem);
            Log.d(TAG, "Добавлен узел с дочерними элементами: " + category.getTitle() + " (уровень " + level + ", " + children.size() + " дочерних)");
            
            // Добавляем дочерние элементы только если узел развернут
            if (treeItem.isExpanded()) {
                for (Category child : children) {
                    addCategoryToTree(child, level + 1, childrenMap, expansionState);
                }
            }
        } else {
            treeItem.setHasChildren(false);
            treeItems.add(treeItem);
            Log.d(TAG, "Добавлен листовой узел: " + category.getTitle() + " (уровень " + level + ")");
        }
    }
    
    /**
     * Переключает состояние развернутости узла
     */
    public void toggleExpanded(int position) {
        if (position >= 0 && position < treeItems.size()) {
            CategoryTreeItem treeItem = treeItems.get(position);
            if (treeItem.hasChildren()) {
                treeItem.setExpanded(!treeItem.isExpanded());
                rebuildTreeWithExpansion();
                notifyDataSetChanged();
            }
        }
    }
    
    /**
     * Перестраивает дерево с учетом состояния развернутости
     */
    private void rebuildTreeWithExpansion() {
        // Сохраняем состояние развернутости
        Map<Integer, Boolean> expansionState = new HashMap<>();
        for (CategoryTreeItem item : treeItems) {
            expansionState.put(item.getCategoryId(), item.isExpanded());
        }
        
        // Перестраиваем дерево с сохраненным состоянием
        treeItems.clear();
        
        // Создаем карту дочерних элементов
        Map<Integer, List<Category>> childrenMap = new HashMap<>();
        for (Category category : allCategories) {
            Integer parentId = category.getParentId();
            if (parentId != null && parentId > 0) { // Не корневая категория
                childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(category);
            }
        }
        
        // Добавляем корневые категории (parentId == null или <= 0)
        for (Category category : allCategories) {
            Integer parentId = category.getParentId();
            if (parentId == null || parentId <= 0) {
                addCategoryToTree(category, 0, childrenMap, expansionState);
            }
        }
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
     * Получает список выбранных категорий
     */
    public List<Category> getSelectedCategories() {
        List<Category> selected = new ArrayList<>();
        for (CategoryTreeItem treeItem : treeItems) {
            if (selectedCategories.contains(treeItem.getCategoryId())) {
                selected.add(treeItem.getCategory());
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
     * ViewHolder для элементов дерева категорий
     */
    public class CategoryTreeViewHolder extends RecyclerView.ViewHolder {
        private TextView titleText;
        private TextView positionText;
        private TextView idText;
        private ImageView expandIcon;
        private ImageView checkBox;
        private View itemView;
        
        public CategoryTreeViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.titleText = itemView.findViewById(R.id.category_title);
            this.positionText = itemView.findViewById(R.id.category_position);
            this.idText = itemView.findViewById(R.id.category_id);
            this.expandIcon = itemView.findViewById(R.id.expand_icon);
            this.checkBox = itemView.findViewById(R.id.checkbox);
        }
        
        public void bind(CategoryTreeItem treeItem, boolean selectionMode, boolean isSelected) {
            Category category = treeItem.getCategory();
            if (category == null) return;
            
            // Устанавливаем отступ в зависимости от уровня
            int paddingLeft = treeItem.getLevel() * 32; // 32dp на уровень
            itemView.setPadding(paddingLeft, itemView.getPaddingTop(), 
                              itemView.getPaddingRight(), itemView.getPaddingBottom());
            
            // Устанавливаем позицию
            if (positionText != null) {
                positionText.setText(String.valueOf(category.getPosition()));
            }
            
            // Устанавливаем название
            titleText.setText(category.getTitle());
            
            // Устанавливаем ID
            if (idText != null) {
                idText.setText("ID: " + category.getId());
            }
            
            // Настраиваем иконку разворачивания
            if (treeItem.hasChildren()) {
                expandIcon.setVisibility(View.VISIBLE);
                expandIcon.setImageResource(treeItem.isExpanded() ? 
                    R.drawable.ic_expand_less : R.drawable.ic_expand_more);
                
                expandIcon.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        toggleExpanded(position);
                    }
                });
            } else {
                expandIcon.setVisibility(View.INVISIBLE);
            }
            
            // Настраиваем режим выбора
            if (selectionMode) {
                checkBox.setVisibility(View.VISIBLE);
                checkBox.setImageResource(isSelected ? 
                    R.drawable.ic_checkbox_checked : R.drawable.ic_checkbox_unchecked);
                
                checkBox.setOnClickListener(v -> {
                    int categoryId = treeItem.getCategoryId();
                    if (isSelected) {
                        selectedCategories.remove(categoryId);
                    } else {
                        selectedCategories.add(categoryId);
                    }
                    
                    if (itemSelectionListener != null) {
                        itemSelectionListener.onSelectedCategoriesChanged(categoryId, !isSelected);
                    }
                    
                    notifyItemChanged(getAdapterPosition());
                });
            } else {
                checkBox.setVisibility(View.GONE);
            }
            
            // Настраиваем обработчики кликов
            itemView.setOnClickListener(v -> {
                if (!selectionMode && itemClickListener != null) {
                    itemClickListener.onCategoryClick(category.getId());
                }
            });
            
            itemView.setOnLongClickListener(v -> {
                if (!selectionMode && itemLongClickListener != null) {
                    itemLongClickListener.onCategoryLongClick(category.getId());
                    return true;
                }
                return false;
            });
        }
    }
}
