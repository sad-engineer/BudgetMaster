package com.sadengineer.budgetmaster.categories;

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
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;
import com.sadengineer.budgetmaster.backend.service.CategoryService;
import com.sadengineer.budgetmaster.backend.entity.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity для создания/изменения категории
 */
public class CategoryEditActivity extends BaseNavigationActivity {
    
    private static final String TAG = "CategoryEditActivity";
    
    private EditText categoryNameEdit;
    private Spinner categoryTypeSpinner;
    private Spinner categoryOperationTypeSpinner;
    private Spinner categoryParentSpinner;
    private ImageButton saveButton;
    private ImageButton backButton;
    private ImageButton menuButton;
    private CategoryService categoryService;
    
    // Поля для хранения данных категории
    private Category currentCategory;
    private boolean isEditMode = false;
    private int sourceOperationType = 1; // Тип операции по умолчанию (1 - доходы, 2 - расходы)
    
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
        categoryTypeSpinner = findViewById(R.id.category_type_spinner);
        categoryOperationTypeSpinner = findViewById(R.id.category_operation_type_spinner);
        categoryParentSpinner = findViewById(R.id.category_parent_spinner);
        saveButton = findViewById(R.id.save_button);
        backButton = findViewById(R.id.back_button);
        menuButton = findViewById(R.id.menu_button);

        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButton(R.id.back_button);

        // Инициализация сервисов
        categoryService = new CategoryService(this, "default_user");
        
        // Настраиваем спиннеры
        setupSpinners();
        
        // Получаем данные из Intent и заполняем поля
        loadCategoryData();
        
        // Обработчики кнопок
        setupButtonHandlers();
    }
    
    /**
     * Настраивает спиннеры для типов категорий
     */
    private void setupSpinners() {
        // Настройка спиннера типов операций
        String[] operationTypes = {"Доходы", "Расходы"};
        ArrayAdapter<String> operationTypeAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, operationTypes);
        operationTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryOperationTypeSpinner.setAdapter(operationTypeAdapter);
        
        // Настройка спиннера типов категорий
        String[] categoryTypes = {"Основная", "Подкатегория"};
        ArrayAdapter<String> categoryTypeAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, categoryTypes);
        categoryTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryTypeSpinner.setAdapter(categoryTypeAdapter);
        
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
        categoryService.getAllByOperationType(sourceOperationType, 
            com.sadengineer.budgetmaster.backend.entity.EntityFilter.ACTIVE).observe(this, categories -> {
            if (categories != null && !categories.isEmpty()) {
                List<String> parentOptions = new ArrayList<>();
                parentOptions.add("Нет родителя");
                
                for (Category category : categories) {
                    if (category.getParentId() == null) {
                        parentOptions.add(category.getTitle());
                    }
                }
                
                ArrayAdapter<String> parentAdapter = new ArrayAdapter<>(this, 
                    android.R.layout.simple_spinner_item, parentOptions);
                parentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categoryParentSpinner.setAdapter(parentAdapter);
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
            
            // Получаем категорию для редактирования
            currentCategory = intent.getParcelableExtra("category");
            
            if (currentCategory != null) {
                isEditMode = true;
                fillCategoryData();
                setToolbarTitle(R.string.toolbar_title_category_edit, R.dimen.toolbar_text);
            } else {
                isEditMode = false;
                setToolbarTitle(R.string.toolbar_title_category_add, R.dimen.toolbar_text);
            }
            
            // Устанавливаем тип операции в спиннер
            categoryOperationTypeSpinner.setSelection(sourceOperationType - 1);
        }
    }
    
    /**
     * Заполняет поля данными категории
     */
    private void fillCategoryData() {
        if (currentCategory != null) {
            categoryNameEdit.setText(currentCategory.getTitle());
            categoryTypeSpinner.setSelection(currentCategory.getType() - 1);
            categoryOperationTypeSpinner.setSelection(currentCategory.getOperationType() - 1);
            
            // Устанавливаем родительскую категорию
            if (currentCategory.getParentId() != null) {
                // TODO: Найти и установить родительскую категорию в спиннер
            }
        }
    }
    
    /**
     * Настраивает обработчики кнопок
     */
    private void setupButtonHandlers() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCategory();
            }
        });
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
     * Сохраняет категорию
     */
    private void saveCategory() {
        String title = categoryNameEdit.getText().toString().trim();
        
        if (TextUtils.isEmpty(title)) {
            categoryNameEdit.setError("Название категории не может быть пустым");
            return;
        }
        
        if (isEditMode) {
            // Редактирование существующей категории
            if (currentCategory != null) {
                currentCategory.setTitle(title);
                currentCategory.setOperationType(getSelectedOperationType());
                currentCategory.setType(getSelectedCategoryType());
                currentCategory.setParentId(getSelectedParentId());
                
                categoryService.update(currentCategory);
                Log.d(TAG, "✅ Категория обновлена: " + title);
            }
        } else {
            // Создание новой категории
            int operationType = getSelectedOperationType();
            int categoryType = getSelectedCategoryType();
            Integer parentId = getSelectedParentId();
            
            categoryService.create(title, operationType, categoryType, parentId);
            Log.d(TAG, "✅ Категория создана: " + title);
        }
        
        // Возвращаемся к списку категорий
        returnToCategories();
    }
    
    /**
     * Получает выбранный тип операции
     */
    private int getSelectedOperationType() {
        return categoryOperationTypeSpinner.getSelectedItemPosition() + 1;
    }
    
    /**
     * Получает выбранный тип категории
     */
    private int getSelectedCategoryType() {
        return categoryTypeSpinner.getSelectedItemPosition() + 1;
    }
    
    /**
     * Получает выбранный ID родителя
     */
    private Integer getSelectedParentId() {
        int position = categoryParentSpinner.getSelectedItemPosition();
        if (position == 0) {
            return null; // Нет родителя
        }
        // TODO: Реализовать получение ID родительской категории
        return null;
    }
    
    /**
     * Устанавливает заголовок тулбара
     */
    private void setToolbarTitle(int titleResId, int textSizeResId) {
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        if (toolbarTitle != null) {
            toolbarTitle.setText(titleResId);
            toolbarTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, 
                getResources().getDimensionPixelSize(textSizeResId));
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
