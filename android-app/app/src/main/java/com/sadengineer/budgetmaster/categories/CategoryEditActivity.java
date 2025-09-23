package com.sadengineer.budgetmaster.categories;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import android.content.Intent;
import android.os.Bundle;
 
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.base.BaseEditActivity;
import com.sadengineer.budgetmaster.backend.service.ServiceManager;
import com.sadengineer.budgetmaster.backend.validator.CategoryValidator;
import com.sadengineer.budgetmaster.backend.entity.Category;
import com.sadengineer.budgetmaster.backend.constants.ModelConstants;
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;
import com.sadengineer.budgetmaster.utils.LogManager;

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
    private ServiceManager serviceManager;
    private CategoryValidator validator = new CategoryValidator();
    // Поля для хранения данных категории
    private Category currentCategory;
    private boolean isEditMode = false;
    private int sourceOperationType = 1; // Тип операции по умолчанию (1 - доходы, 2 - расходы)
    private List<Category> parentCategories = new ArrayList<>(); // Список родительских категорий
    
    private static final int PARENT = ModelConstants.CATEGORY_TYPE_PARENT;
    private static final int CHILD = ModelConstants.CATEGORY_TYPE_CHILD;
    public static final int DEFAULT_CATEGORY_ID = ModelConstants.DEFAULT_CATEGORY_ID;
    
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
        
        // Инициализация общих действий экрана редактирования
        setupCommonEditActions(R.id.position_change_button);

        // Настройка кнопки "Назад"
        setupBackButton(R.id.back_button);

        // Инициализация сервисов
        serviceManager = ServiceManager.getInstance(this, userName);
        
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
        //TODO: убрать вложенный observe
        serviceManager.categories.getAllByOperationType(sourceOperationType, EntityFilter.ACTIVE).observe(this, categories -> {
            if (categories != null) {
                List<String> parentOptions = new ArrayList<>();
                List<Category> parentCategories = new ArrayList<>();
                
                // Добавляем опцию "Нет родителя"
                parentOptions.add("Нет родителя");
                parentCategories.add(null); // null для "Нет родителя"
                
                // Если это режим редактирования, исключаем дочерние категории
                if (isEditMode && currentCategory != null) {
                    // Получаем все дочерние категории редактируемой категории
                    serviceManager.categories.getAllDescendants(currentCategory.getId(), EntityFilter.ACTIVE).observe(this, descendants -> {
                        if (descendants != null) {
                            // Создаем множество ID дочерних категорий для быстрого поиска
                            Set<Integer> descendantIds = new HashSet<>();
                            for (Category descendant : descendants) {
                                descendantIds.add(descendant.getId());
                            }
                            
                                                         // Добавляем только те категории, которые не являются дочерними
                             for (Category category : categories) {
                                 if (!descendantIds.contains(category.getId()) && category.getId() != currentCategory.getId()) {
                                     parentOptions.add(category.getTitle());
                                     parentCategories.add(category);
                                 }
                             }
                            
                            // Сохраняем список категорий для получения ID
                            this.parentCategories = parentCategories;
                            
                            ArrayAdapter<String> parentAdapter = new ArrayAdapter<>(this, 
                                android.R.layout.simple_spinner_item, parentOptions);
                            parentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            categoryParentSpinner.setAdapter(parentAdapter);
                            
                            // Устанавливаем родительскую категорию для редактирования
                            LogManager.d(TAG, "Устанавливаем родительскую категорию для редактирования. ParentId: " + currentCategory.getParentId());
                            if (currentCategory.getParentId() != null && currentCategory.getParentId() != PARENT) {
                                setSelectedParentCategory(currentCategory.getParentId());
                            } else {
                                setSelectedParentCategory(PARENT);
                            }
                        }
                    });
                } else {
                    // Для создания новой категории добавляем все категории
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
            LogManager.d(TAG, "Загружен тип операции из Intent: " + sourceOperationType);
            
            // Получаем категорию для редактирования
            currentCategory = (Category) intent.getSerializableExtra("category");
            
            if (currentCategory != null) {
                isEditMode = true;
                LogManager.d(TAG, "Режим редактирования. Категория: " + currentCategory.getTitle() + 
                          ", ID: " + currentCategory.getId() + 
                          ", ParentId: " + currentCategory.getParentId());
                fillCategoryData();
                setToolbarTitle(R.string.toolbar_title_category_edit, R.dimen.toolbar_text);
            } else {
                isEditMode = false;
                LogManager.d(TAG, "Режим создания новой категории");
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
            LogManager.d(TAG, "Заполнено название категории: " + currentCategory.getTitle());
            
            // Родительская категория будет установлена после загрузки списка в loadParentCategories()
        }
    }
    
    /**
     * Переопределяем настройку кнопки "назад" для перехода к списку категорий
     */
    protected void setupBackButton(int backButtonId) {
        ImageButton back = findViewById(backButtonId);
        if (back != null) {
            back.setOnClickListener(v -> {
                LogManager.d(TAG, "Нажата кнопка 'Назад'");
                returnToCategories();
            });
        }
    }
    
    /**
     * Реализация абстрактного метода для валидации и сохранения
     */
    @Override
    protected boolean validateAndSave() {
        return saveCategory();
    }
    
    /**
     * Валидирует название категории
     * @param editText поле ввода названия категории
     * @return true если название категории валидно, false если нет
     */
    private boolean validateCategoryName(EditText editText) {
        String title = editText.getText().toString().trim();
        try {
            validator.validateTitle(title);
        } catch (IllegalArgumentException e) {
            LogManager.e(TAG, e.getMessage());
            editText.setError(e.getMessage());
            editText.requestFocus();
            return false;
        }
        return true;
    }


    /**
     * Сохраняет категорию
     */
    private boolean saveCategory() {
        LogManager.d(TAG, "Сохранение категории...");
        String title = categoryNameEdit.getText().toString().trim();

        if (!validateCategoryName(categoryNameEdit)) return false;
        
        try {
            if (isEditMode) {
                LogManager.d(TAG, "Редактирование категории.");
                LogManager.d(TAG, "Текущая категория: название=" + currentCategory.getTitle() + ", operationType=" + currentCategory.getOperationType() + ", тип=" + currentCategory.getType() + ", родитель=" + currentCategory.getParentId());

                // Редактирование существующей категории
                if (currentCategory != null) {
                    currentCategory.setTitle(title);
                    currentCategory.setOperationType(sourceOperationType);
                    currentCategory.setType(getSelectedCategoryType());
                    currentCategory.setParentId(getSelectedParentId());
                    LogManager.d(TAG, "Обновленнаz категория: название=" + title + ", operationType=" + sourceOperationType + ", тип=" + getSelectedCategoryType() + ", родитель=" + getSelectedParentId());
                    serviceManager.categories.update(currentCategory);
                    LogManager.d(TAG, "Запрос на обновление счета отправлен");
                }
            } else {
                // Создание новой категории с бюджетом
                int categoryType = getSelectedCategoryType();
                int parentId = getSelectedParentId();
                LogManager.d(TAG, "Создание новой категории с бюджетом: " + title);
                serviceManager.createCategoryWithBudgetWithoutValidation(title, sourceOperationType, categoryType, parentId, null, null);
                LogManager.d(TAG, "Запрос на создание категории с бюджетом отправлен");
            }
            
            // Возвращаемся к списку категорий
            returnToCategories();
            return true;
            
        } catch (Exception e) {
            LogManager.e(TAG, "Ошибка при сохранении категории: " + e.getMessage(), e);
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
        LogManager.d(TAG, "setSelectedParentCategory вызван с parentId: " + parentId);
        if (parentId == null || parentId == DEFAULT_CATEGORY_ID) {
            categoryParentSpinner.setSelection(0); // "Нет родителя"
            LogManager.d(TAG, "Установлена позиция 0 (Нет родителя)");
            return;
        }
        
        for (int i = 0; i < parentCategories.size(); i++) {
            Category category = parentCategories.get(i);
            if (category != null && category.getId() == parentId) {
                categoryParentSpinner.setSelection(i);
                LogManager.d(TAG, "Установлена позиция " + i + " для категории с ID " + parentId);
                break;
            }
        }
    }    
    
    /**
     * Возвращается к списку категорий
     */
    private void returnToCategories() {
        LogManager.d(TAG, "Переход к окну списка категорий, вкладка " + sourceOperationType);
        // Возвращаемся к соответствующему экрану категорий в зависимости от типа операции
        if (sourceOperationType == ModelConstants.OPERATION_TYPE_INCOME) {
            returnTo(IncomeCategoriesActivity.class, true, new String[0]);
        } else {
            returnTo(ExpenseCategoriesActivity.class, true, new String[0]);
        }
    }
}
