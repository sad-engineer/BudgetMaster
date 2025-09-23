package com.sadengineer.budgetmaster.categories;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
 
import android.view.View;
import android.widget.ImageButton;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.base.BaseContentActivity;
import com.sadengineer.budgetmaster.backend.entity.Category;
import com.sadengineer.budgetmaster.backend.service.ServiceManager;
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;
import com.sadengineer.budgetmaster.backend.constants.ModelConstants;
import com.sadengineer.budgetmaster.utils.LogManager;

import java.util.List;
import java.util.ArrayList;

/**
 * Activity для отображения категорий доходов
 */
public class IncomeCategoriesActivity extends BaseContentActivity {
    
    private static final String TAG = "IncomeCategoriesActivity";

    /** Имя пользователя по умолчанию */
    /** TODO: передлать на получение имени пользователя из SharedPreferences */
    private String userName = "default_user";

    private static final int OPERATION_TYPE = ModelConstants.OPERATION_TYPE_INCOME; // Доходы
    
    private RecyclerView recyclerView;
    private CategoryTreeAdapter adapter;
    private ImageButton addCategoryButton;
    private ImageButton deleteCategoryButton;
    private ServiceManager serviceManager;
    private boolean isSelectionMode = false;
    private List<Category> categories = new ArrayList<>();

    /**
     * Метод вызывается при создании Activity
     * @param savedInstanceState - сохраненное состояние Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income_categories);

        // Инициализация навигации
        initializeNavigation();
        // Устанавливаем заголовок
        setToolbarTitle(R.string.menu_income_categories, R.dimen.toolbar_text);

        // Инициализация ServiceManager
        serviceManager = ServiceManager.getInstance(this, userName);

        // Инициализация RecyclerView
        setupRecyclerView();
        
        // Загружаем категории доходов из базы данных
        loadCategoriesFromDatabase();

        // Обработчики кнопок категорий
        setupButtons();
    }
    
    /**
     * Настраивает кнопки
     */
    private void setupButtons() {
        addCategoryButton = findViewById(R.id.add_button_bottom);
        deleteCategoryButton = findViewById(R.id.delete_button_bottom);

        /**
         * Обработчик нажатия на кнопку добавления категории
         */
        addCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSelectionMode) {
                    // В режиме выбора - удаляем выбранные категории
                    deleteSelectedCategories();
                } else {
                    // Запускаем окно создания категории
                    String[] params = {"operation_type", String.valueOf(OPERATION_TYPE)};
                    goTo(CategoryEditActivity.class, false, params);
                }
            }
        });

        /**
         * Обработчик нажатия на кнопку удаления категорий
         */
        deleteCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSelectionMode) {
                    // В режиме выбора - отменяем выбор
                    cancelSelectionMode();
                } else {
                    // Включаем режим выбора
                    enableSelectionMode();
                }
            }
        });
    }
    
    /**
     * Включает режим выбора категорий
     */
    private void enableSelectionMode() {
        isSelectionMode = true;
        
        // Меняем иконки кнопок
        addCategoryButton.setImageResource(R.drawable.ic_save);
        deleteCategoryButton.setImageResource(R.drawable.ic_back);
        
        // Небольшая задержка для плавного перехода
        recyclerView.postDelayed(() -> {
            adapter.setSelectionMode(true);
            LogManager.d(TAG, "Режим выбора категорий включен");
        }, 100);
    }
    
    /**
     * Отменяет режим выбора
     */
    private void cancelSelectionMode() {
        isSelectionMode = false;
        adapter.setSelectionMode(false);
        adapter.clearSelection();
        
        // Возвращаем иконки кнопок
        addCategoryButton.setImageResource(R.drawable.ic_add);
        deleteCategoryButton.setImageResource(R.drawable.ic_delete);
        
        LogManager.d(TAG, "Режим выбора категорий отменен");
    }
    
    /**
     * Удаляет выбранные категории
     */
    private void deleteSelectedCategories() {
        List<Category> selectedCategories = adapter.getSelectedCategories();
        
        LogManager.d(TAG, "Удаляем выбранные категории: " + selectedCategories.size());
        
        // Удаляем категории из базы данных
        for (Category category : selectedCategories) {
            try {
                serviceManager.categories.delete(category, true);
                LogManager.d(TAG, "Удалена категория: " + category.getTitle());
            } catch (Exception e) {
                LogManager.e(TAG, "Ошибка удаления категории " + category.getTitle() + ": " + e.getMessage(), e);
            }
        }
        
        // Отменяем режим выбора
        cancelSelectionMode();
        LogManager.d(TAG, "Удалено категорий: " + selectedCategories.size());
    }
    
    /**
     * Настраивает RecyclerView
     */
    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.income_categories_recycler_view);
        adapter = new CategoryTreeAdapter(this, this::onCategoryClick);
        adapter.setOnCategoryLongClickListener(this::onCategoryLongClick);
        adapter.setOnSelectedCategoriesChanged(this::onSelectedCategoriesChanged);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    
    /**
     * Показывает диалог подтверждения удаления категории
     */
    private void showDeleteConfirmationDialog(Category category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Удаление категории")
               .setMessage("Вы уверены, что хотите полностью удалить категорию '" + category.getTitle() + "'?\n\n" +
                          "⚠️ Это действие нельзя отменить!")
               .setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       deleteCategory(category);
                   }
               })
               .setNegativeButton("Отмена", null)
               .setIcon(android.R.drawable.ic_dialog_alert)
               .show();
    }
    
    /**
     * Удаляет категорию из базы данных
     */
    private void deleteCategory(Category category) {
        try {
            LogManager.d(TAG, "Удаляем категорию из базы данных: " + category.getTitle());
            serviceManager.categories.delete(category, false);
            LogManager.d(TAG, "Запрос на удаление категории отправлен: " + category.getTitle());
        } catch (Exception e) {
            LogManager.e(TAG, "Ошибка удаления категории " + category.getTitle() + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Загружает категории доходов из базы данных
     */
    private void loadCategoriesFromDatabase() {
        LogManager.d(TAG, "Загружаем категории доходов из базы данных...");
        
        try {
            serviceManager.categories.getAllByOperationType(OPERATION_TYPE, EntityFilter.ALL).observe(this, loadedCategories -> {
                LogManager.d(TAG, "Загружено категорий доходов: " + (loadedCategories != null ? loadedCategories.size() : 0));
                if (loadedCategories != null && !loadedCategories.isEmpty()) {
                    categories.clear();
                    categories.addAll(loadedCategories);
                    adapter.setCategories(loadedCategories);
                    LogManager.d(TAG, "Категории доходов отображены в списке");
                    
                    // // Сбрасываем счетчик свайпов при изменении содержимого списка
                    // resetSwipeCount();
                } else {
                    categories.clear();
                    LogManager.w(TAG, "Категории доходов не найдены в базе данных");
                }
            }); 
        } catch (Exception e) {
            LogManager.e(TAG, "Ошибка загрузки категорий доходов: " + e.getMessage(), e);
        }
    }
    
    /**
     * Обрабатывает клик по категории
     * @param categoryId - ID выбранной категории
     */
    private void onCategoryClick(int categoryId) {
        LogManager.d(TAG, "Клик по категории с ID: " + categoryId);
        // Находим категорию по ID и переходим к редактированию
        Category category = findCategoryById(categoryId);
        if (category != null) {
            goToCategoryEdit(category);
        }
    }
    
    /**
     * Обрабатывает длительное нажатие на категорию
     * @param categoryId - ID выбранной категории
     */
    private void onCategoryLongClick(int categoryId) {
        LogManager.d(TAG, "Длительное нажатие на категорию с ID: " + categoryId);
        // Находим категорию по ID и показываем диалог удаления
        Category category = findCategoryById(categoryId);
        if (category != null) {
            showDeleteConfirmationDialog(category);
        }
    }
    
    /**
     * Обрабатывает изменение выбранных категорий
     * @param categoryId - ID категории
     * @param isSelected - выбрана ли категория
     */
    private void onSelectedCategoriesChanged(int categoryId, boolean isSelected) {
        LogManager.d(TAG, "Категория " + categoryId + " " + (isSelected ? "выбрана" : "снята с выбора"));
        // Здесь можно добавить логику, если нужно реагировать на изменения выбора
    }
    
    /**
     * Находит категорию по ID
     * @param categoryId - ID категории
     * @return найденная категория или null
     */
    private Category findCategoryById(int categoryId) {
        for (Category category : categories) {
            if (category.getId() == categoryId) {
                return category;
            }
        }
        return null;
    }

    /**
     * Переходит на экран редактирования категории
     * @param category - выбранная категория
     */
    private void goToCategoryEdit(Category category) {
        LogManager.d(TAG, "Переходим к окну редактирования категории");
        String[] params = {"operation_type", String.valueOf(OPERATION_TYPE), "category", category.getTitle()};
        goTo(CategoryEditActivity.class, false, params);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Обновляем отображение настроек (ID, позиция)
        if (adapter != null) {
            adapter.refreshSettings();
        }
    }
} 