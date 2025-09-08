package com.sadengineer.budgetmaster.backend.service;

import android.content.Context;
import android.util.Log;

import com.sadengineer.budgetmaster.backend.entity.Operation;
import com.sadengineer.budgetmaster.backend.entity.Category;
import com.sadengineer.budgetmaster.backend.entity.Budget;
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;
import com.sadengineer.budgetmaster.backend.constants.ServiceConstants;
import com.sadengineer.budgetmaster.backend.constants.ModelConstants;
import com.sadengineer.budgetmaster.backend.ThreadManager;

import static com.sadengineer.budgetmaster.backend.constants.ModelConstants.DEFAULT_AMOUNT;
import static com.sadengineer.budgetmaster.backend.constants.ModelConstants.DEFAULT_CURRENCY_ID;
import static com.sadengineer.budgetmaster.backend.constants.ServiceConstants.MSG_CREATE_CATEGORY_WITH_BUDGET_REQUEST;
import static com.sadengineer.budgetmaster.backend.constants.ServiceConstants.MSG_CREATE_CATEGORY_WITH_BUDGET_SUCCESS;
import static com.sadengineer.budgetmaster.backend.constants.ServiceConstants.MSG_CREATE_CATEGORY_WITH_BUDGET_ERROR;
import static com.sadengineer.budgetmaster.backend.constants.ServiceConstants.MSG_DELETE_CATEGORY_WITH_BUDGET_REQUEST;
import static com.sadengineer.budgetmaster.backend.constants.ServiceConstants.MSG_DELETE_CATEGORY_WITH_BUDGET_NOT_FOUND;
import static com.sadengineer.budgetmaster.backend.constants.ServiceConstants.MSG_DELETE_CATEGORY_WITH_BUDGET_SUCCESS;
import static com.sadengineer.budgetmaster.backend.constants.ServiceConstants.MSG_DELETE_CATEGORY_WITH_BUDGET_ERROR;


import androidx.room.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Менеджер сервисов, предоставляющий централизованный доступ ко всем сервисам приложения
 * Позволяет обращаться к сервисам напрямую: ServiceManager.accounts.getAll()
 */
public class ServiceManager {
    private static final String TAG = "ServiceManager";
    
    private static ServiceManager instance;
    private final Context context;
    private final String userName;
    private final ExecutorService executorService;
    
    // Вложенные классы для каждого сервиса
    public final Accounts accounts;
    public final Budgets budgets;
    public final Categories categories;
    public final Currencies currencies;
    public final Operations operations;
    
    /**
     * Конструктор
     * @param context контекст приложения
     * @param userName имя пользователя
     */
    private ServiceManager(Context context, String userName) {
        this.context = context;
        this.userName = userName;
        this.executorService = ThreadManager.getExecutor();
        
        // Инициализация вложенных классов
        this.accounts = new Accounts(context, userName);
        this.budgets = new Budgets(context, userName);
        this.categories = new Categories(context, userName);
        this.currencies = new Currencies(context, userName);
        this.operations = new Operations(context, userName);
        
        Log.d(TAG, "ServiceManager инициализирован для пользователя: " + userName);
    }
    
    /**
     * Получить экземпляр ServiceManager (Singleton)
     * @param context контекст приложения
     * @param userName имя пользователя
     * @return экземпляр ServiceManager
     */
    public static ServiceManager getInstance(Context context, String userName) {
        if (instance == null) {
            instance = new ServiceManager(context, userName);
        }
        return instance;
    }
    
    /**
     * Получить существующий экземпляр ServiceManager
     * @return экземпляр ServiceManager или null, если не инициализирован
     */
    public static ServiceManager getInstance() {
        return instance;
    }
    
    /**
     * Сбросить экземпляр (для тестирования или смены пользователя)
     */
    public static void resetInstance() {
        instance = null;
    }
    
    /**
     * Вложенный класс для работы с аккаунтами
     * Наследуется от AccountService для прямого доступа к методам
     */
    public class Accounts extends AccountService {
        
        public Accounts(Context context, String userName) {
            super(context, userName);
        }
    }
    
    /**
     * Вложенный класс для работы с бюджетами
     * Наследуется от BudgetService для прямого доступа к методам
     */
    public class Budgets extends BudgetService {
        public Budgets(Context context, String userName) {
            super(context, userName);
        }
    }
    
    /**
     * Вложенный класс для работы с категориями
     * Наследуется от CategoryService для прямого доступа к методам
     */
    public class Categories extends CategoryService {
        
        public Categories(Context context, String userName) {
            super(context, userName);
        }
    }
    
    /**
     * Вложенный класс для работы с валютами
     * Наследуется от CurrencyService для прямого доступа к методам
     */
    public class Currencies extends CurrencyService {
        
        public Currencies(Context context, String userName) {
            super(context, userName);
        }
    }
    
    /**
     * Вложенный класс для работы с операциями
     * Наследуется от OperationService для прямого доступа к методам
     */
    public class Operations extends OperationService {
        
        public Operations(Context context, String userName) {
            super(context, userName);
        }
    }

    /**
     * Создать категорию с автоматическим созданием бюджета
     * @param title название категории
     * @param operationType тип операции
     * @param type тип категории
     * @param parentId ID родителя
     * @param defaultBudgetAmount сумма бюджета по умолчанию (если null, то дефолтная)
     * @param currencyId ID валюты для бюджета (если null, то дефолтная)
     */
    public void createCategoryWithBudget(String title, Integer operationType, Integer type, 
            Integer parentId, Long defaultBudgetAmount, Integer currencyId) {
        
        // Валидация параметров
        categories.validator.validateTitle(title);
        categories.validator.validateOperationType(operationType);
        categories.validator.validateType(type);
        categories.validator.validateParentId(parentId, categories.getCount(EntityFilter.ALL));

        // Определяем финальные значения для использования в лямбде
        final long finalBudgetAmount;
        final int finalCurrencyId;
        
        if (defaultBudgetAmount == null) {
            finalBudgetAmount = DEFAULT_AMOUNT;
        } 
        else {
            budgets.validator.validateAmount(defaultBudgetAmount);
            finalBudgetAmount = defaultBudgetAmount;
        }
        if (currencyId == null) {
            finalCurrencyId = DEFAULT_CURRENCY_ID;
        }
        else {
            budgets.validator.validateCurrencyId(currencyId, currencies.getCount(EntityFilter.ALL));
            finalCurrencyId = currencyId;
        }

        // Создание категории и бюджета в одной транзакции
        executorService.execute(() -> {
            createCategoryWithBudgetInTransaction(title, operationType, type, parentId, 
                    finalBudgetAmount, finalCurrencyId);
        });
    }

    /**
     * Создать категорию с бюджетом без проверок значений
     * @param title название категории
     * @param operationType тип операции
     * @param type тип категории
     * @param parentId ID родителя
     * @param defaultBudgetAmount сумма бюджета по умолчанию (если null, то дефолтная)
     * @param currencyId ID валюты для бюджета (если null, то дефолтная)
     */
    public void createCategoryWithBudgetWithoutValidation(String title, int operationType, int type, 
            int parentId, Long defaultBudgetAmount, Integer currencyId) {

        // Определяем финальные значения для использования в лямбде
        final long finalBudgetAmount = (defaultBudgetAmount != null) ? defaultBudgetAmount : DEFAULT_AMOUNT;
        final int finalCurrencyId = (currencyId != null) ? currencyId : DEFAULT_CURRENCY_ID;

        executorService.execute(() -> {
            createCategoryWithBudgetInTransaction(title, operationType, type, parentId, 
                    finalBudgetAmount, finalCurrencyId);
        });
    }

    /**
     * Транзакция для создания категории с бюджетом
     * @param title название категории
     * @param operationType тип операции
     * @param type тип категории
     * @param parentId ID родителя
     * @param defaultBudgetAmount сумма бюджета по умолчанию
     * @param currencyId ID валюты для бюджета 
     */
    @Transaction
    private void createCategoryWithBudgetInTransaction(String title, int operationType, int type, 
            int parentId, long defaultBudgetAmount, int currencyId) {
        Log.d(TAG, String.format(MSG_CREATE_CATEGORY_WITH_BUDGET_REQUEST, title, "true"));
        
        // Создаем категорию через существующую транзакцию и получаем ID
        long categoryId = categories.createCategoryInTransaction(title, operationType, type, parentId);
        
        if (categoryId > 0) {
            // Создаем бюджет через существующую транзакцию
            budgets.createBudgetInTransaction((int)categoryId, defaultBudgetAmount, currencyId);
            Log.d(TAG, String.format(MSG_CREATE_CATEGORY_WITH_BUDGET_SUCCESS, title, defaultBudgetAmount));
        } else {
            Log.e(TAG, String.format(MSG_CREATE_CATEGORY_WITH_BUDGET_ERROR, title));
        }
    }

    /**
     * Удалить категорию с бюджетом
     * @param category категория
     * @param softDelete true - soft delete, false - полное удаление
     */
    public void deleteCategoryWithBudget(Category category, boolean softDelete) {
        if (category == null) {
            Log.e(TAG, MSG_DELETE_CATEGORY_WITH_BUDGET_NOT_FOUND);
            return;
        }
        
        executorService.execute(() -> {
            deleteCategoryWithBudgetInTransaction(category, softDelete);
        });
    }

    /**
     * Транзакция для удаления категории с бюджетом
     * @param category категория
     * @param softDelete true - soft delete, false - полное удаление
     */
    @Transaction
    private void deleteCategoryWithBudgetInTransaction(Category category, boolean softDelete) {
        Log.d(TAG, String.format(MSG_DELETE_CATEGORY_WITH_BUDGET_REQUEST, category.getTitle()));
        
        try {
            // Удаляем связанный бюджет (если есть) - синхронно
            Budget budget = budgets.getByCategorySync(category.getId());
            if (budget != null) {
                budgets.delete(budget, softDelete);
            }
            
            // Удаляем категорию
            categories.delete(category, softDelete);
            
            Log.d(TAG, String.format(MSG_DELETE_CATEGORY_WITH_BUDGET_SUCCESS, category.getTitle()));
        } catch (Exception e) {
            Log.e(TAG, String.format(MSG_DELETE_CATEGORY_WITH_BUDGET_ERROR, category.getTitle()) + ": " + e.getMessage(), e);
        }
    }

}

/*
 * ПРИМЕРЫ ИСПОЛЬЗОВАНИЯ ServiceManager:
 * 
 * 1. Инициализация в StartActivity или Application:
 *    ServiceManager serviceManager = ServiceManager.getInstance(context, "user_name");
 * 
 * 2. Использование в любом месте приложения:
 *    ServiceManager sm = ServiceManager.getInstance();
 * 
 * 3. ПРЯМОЙ ДОСТУП К СЕРВИСАМ:
 * 
 *    // Работа с аккаунтами
 *    sm.accounts.changePosition(account, newPosition);
 *    sm.accounts.getAll();
 *    sm.accounts.getById(id);
 * 
 *    // Работа с бюджетами
 *    sm.budgets.create(budget);
 *    sm.budgets.update(budget);
 *    sm.budgets.delete(id);
 * 
 *    // Работа с категориями
 *    sm.categories.getAll();
 *    sm.categories.getByType(type);
 * 
 *    // Работа с валютами
 *    sm.currencies.getDefault();
 *    sm.currencies.getAll();
 * 
 *    // Работа с операциями
 *    sm.operations.getCount();
 *    sm.operations.getOperationsByDateRange(start, end);
 *    sm.operations.create(operation);
 * 
 * 4. Альтернативный способ получения сервиса:
 *    AccountService accountService = sm.accounts;
 *    accountService.changePosition(account, newPosition);
 * 
 * 5. Сброс экземпляра (при смене пользователя):
 *    ServiceManager.resetInstance();
 *    ServiceManager.getInstance(context, "new_user");
 * 
 */
