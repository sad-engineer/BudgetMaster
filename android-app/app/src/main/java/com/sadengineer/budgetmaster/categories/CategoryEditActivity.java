package com.sadengineer.budgetmaster.categories;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.util.TypedValue;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.base.BaseEditActivity;
import com.sadengineer.budgetmaster.backend.service.CategoryService;
import com.sadengineer.budgetmaster.backend.entity.Category;
import com.sadengineer.budgetmaster.backend.constants.ModelConstants;
import com.sadengineer.budgetmaster.backend.entity.EntityFilter;



/**
 * Activity для создания/изменения категории
 */
public class CategoryEditActivity extends BaseEditActivity<Category> {
    
    private static final String TAG = "CategoryEditActivity";

    /** Имя пользователя по умолчанию */
    /** TODO: передлать на получение имени пользователя из SharedPreferences */
    private String userName = "default_user";
    
    private EditText categoryNameEdit;
    private Spinner categoryParentSpinner;
    private ImageButton saveButton;
    private ImageButton backButton;
    private ImageButton menuButton;
    private CategoryService categoryService;
    
    // Поля для хранения данных категории
    private Category currentCategory;
    private boolean isEditMode = false;
    private int sourceOperationType = 1; // Тип операции по умолчанию (1 - доходы, 2 - расходы)
    private List<Category> parentCategories = new ArrayList<>(); // Список родительских категорий
    
    private static final int PARENT = ModelConstants.CATEGORY_TYPE_PARENT;
    private static final int CHILD = ModelConstants.CATEGORY_TYPE_CHILD;
    public static final int DEFAULT_CATEGORY_ID = ModelConstants.DEFAULT_PARENT_CATEGORY_ID;
    
    /**
     * Метод вызывается при создании Activity
     * @param savedInstanceState - сохраненное состояние Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_edit);

        // Инициализация всех View элементов
        categoryNameEdit = findViewById(R.id.category_name_edit_text);
        categoryParentSpinner = findViewById(R.id.category_parent_spinner);
        saveButton = findViewById(R.id.position_change_button);
        backButton = findViewById(R.id.back_button);
        menuButton = findViewById(R.id.menu_button);

        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButton(R.id.back_button);
        
        // Инициализация общих действий экрана редактирования
        setupCommonEditActions(R.id.position_change_button);

        // Инициализация сервисов
        categoryService = new CategoryService(this, userName);
        
        // Получаем данные из Intent ПЕРЕД настройкой спиннеров
        loadCategoryData();
        
        // Настраиваем спиннеры
        setupSpinners();
    }
    
    /**
     * Настраивает спиннеры для типов категорий
     */
    private void setupSpinners() {
        // Настройка спиннера родительских категорий
        String[] parentCategories = {"Нет родителя"};
        ArrayAdapter<String> parentAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, parentCategories);
        parentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryParentSpinner.setAdapter(parentAdapter);
        
        // Загружаем родительские категории
        loadParentCategories();
    }
    
    /**
     * Загружает родительские категории для спиннера
     */
    private void loadParentCategories() {
        // Используем тип операции, переданный из Intent
        categoryService.getAllByOperationType(sourceOperationType, EntityFilter.ACTIVE).observe(this, categories -> {
            if (categories != null) {
                List<String> parentOptions = new ArrayList<>();
                List<Category> parentCategories = new ArrayList<>();
                
                // Добавляем опцию "Нет родителя"
                parentOptions.add("Нет родителя");
                parentCategories.add(null); // null для "Нет родителя"
                
                // Добавляем все категории (и основные, и подкатегории)
                for (Category category : categories) {
                    parentOptions.add(category.getTitle());
                    parentCategories.add(category);
                }
                
                // Сохраняем список категорий для получения ID
                this.parentCategories = parentCategories;
                
                ArrayAdapter<String> parentAdapter = new ArrayAdapter<>(this, 
                    android.R.layout.simple_spinner_item, parentOptions);
                parentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categoryParentSpinner.setAdapter(parentAdapter);
                
                // Если это режим редактирования, устанавливаем родительскую категорию
                if (isEditMode && currentCategory != null) {
                    Log.d(TAG, "Устанавливаем родительскую категорию для редактирования. ParentId: " + currentCategory.getParentId());
                    if (currentCategory.getParentId() != null && currentCategory.getParentId() != PARENT) {
                        setSelectedParentCategory(currentCategory.getParentId());
                    } else {
                        setSelectedParentCategory(PARENT);
                    }
                }
            }
        });
    }
    
    /**
     * Загружает данные категории из Intent
     */
    @SuppressWarnings("deprecation") 
    private void loadCategoryData() {
        Intent intent = getIntent();
        if (intent != null) {
            // Получаем тип операции из Intent
            sourceOperationType = intent.getIntExtra("operation_type", 1);
            Log.d(TAG, "Загружен тип операции из Intent: " + sourceOperationType);
            
            // Получаем категорию для редактирования
            currentCategory = (Category) intent.getSerializableExtra("category");
            
            if (currentCategory != null) {
                isEditMode = true;
                Log.d(TAG, "Режим редактирования. Категория: " + currentCategory.getTitle() + 
                          ", ID: " + currentCategory.getId() + 
                          ", ParentId: " + currentCategory.getParentId());
                fillCategoryData();
                setToolbarTitle(R.string.toolbar_title_category_edit, R.dimen.toolbar_text);
            } else {
                isEditMode = false;
                Log.d(TAG, "Режим создания новой категории");
                setToolbarTitle(R.string.toolbar_title_category_add, R.dimen.toolbar_text);
            }
        }
    }
    
    /**
     * Заполняет поля данными категории
     */
    private void fillCategoryData() {
        if (currentCategory != null) {
            categoryNameEdit.setText(currentCategory.getTitle());
            Log.d(TAG, "Заполнено название категории: " + currentCategory.getTitle());
            
            // Родительская категория будет установлена после загрузки списка в loadParentCategories()
        }
    }
    
    @Override
    protected void setupBackButton(int backButtonId) {
        super.setupBackButton(backButtonId);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    
    /**
     * Реализация абстрактного метода для валидации и сохранения
     */
    @Override
    protected boolean validateAndSave() {
        return saveCategory();
    }
    
    /**
     * Сохраняет категорию
     */
    private boolean saveCategory() {
        String title = categoryNameEdit.getText().toString().trim();
        
        if (TextUtils.isEmpty(title)) {
            categoryNameEdit.setError("Название категории не может быть пустым");
            return false;
        }
        
        try {
            if (isEditMode) {
                // Редактирование существующей категории
                if (currentCategory != null) {
                    currentCategory.setTitle(title);
                    currentCategory.setOperationType(sourceOperationType);
                    currentCategory.setType(getSelectedCategoryType());
                    currentCategory.setParentId(getSelectedParentId());
                    
                    categoryService.update(currentCategory);
                    Log.d(TAG, "Категория обновлена: " + title);
                }
            } else {
                // Создание новой категории
                int categoryType = getSelectedCategoryType();
                Integer parentId = getSelectedParentId();
                
                categoryService.create(title, sourceOperationType, categoryType, parentId);
                Log.d(TAG, "Категория создана: " + title);
            }
            
            // Возвращаемся к списку категорий
            returnToCategories();
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при сохранении категории: " + e.getMessage(), e);
            return false;
        }
    }
    

    
    /**
     * Автоматически определяет тип категории на основе выбранного родителя
     * @return 0 - основная категория, 1 - подкатегория
     */
    private int getSelectedCategoryType() {
        Integer parentId = getSelectedParentId();
        // Если есть родитель - это подкатегория (CHILD), иначе основная (PARENT)
        return (parentId != null && parentId != PARENT) ? CHILD : PARENT;
    }
    
    /**
     * Получает выбранный ID родителя
     */
    private Integer getSelectedParentId() {
        int position = categoryParentSpinner.getSelectedItemPosition();
        if (position == 0 || position >= parentCategories.size()) {
            return PARENT; // Нет родителя - используем PARENT (0)
        }
        
        Category selectedCategory = parentCategories.get(position);
        return selectedCategory != null ? selectedCategory.getId() : PARENT;
    }
    
    /**
     * Устанавливает выбранную родительскую категорию по ID
     */
    private void setSelectedParentCategory(Integer parentId) {
        Log.d(TAG, "setSelectedParentCategory вызван с parentId: " + parentId);
        if (parentId == null || parentId == DEFAULT_CATEGORY_ID) {
            categoryParentSpinner.setSelection(0); // "Нет родителя"
            Log.d(TAG, "Установлена позиция 0 (Нет родителя)");
            return;
        }
        
        for (int i = 0; i < parentCategories.size(); i++) {
            Category category = parentCategories.get(i);
            if (category != null && category.getId() == parentId) {
                categoryParentSpinner.setSelection(i);
                Log.d(TAG, "Установлена позиция " + i + " для категории с ID " + parentId);
                break;
            }
        }
    }
    

    
    /**
     * Возвращается к списку категорий
     */
    private void returnToCategories() {
        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
