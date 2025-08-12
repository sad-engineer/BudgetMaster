
package com.sadengineer.budgetmaster.backend.database;

import android.content.Context;
import android.util.Log;

import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.sadengineer.budgetmaster.backend.entity.Account;
import com.sadengineer.budgetmaster.backend.entity.Budget;
import com.sadengineer.budgetmaster.backend.entity.Category;
import com.sadengineer.budgetmaster.backend.entity.Currency;
import com.sadengineer.budgetmaster.backend.entity.Operation;
import com.sadengineer.budgetmaster.backend.constants.ModelConstants;   

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Инициализатор базы данных с дефолтными данными
 */
public class DatabaseInitializer {
    
    private static final String TAG = "DatabaseInitializer";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    

    
    /**
     * Инициализирует дефолтные данные в базе
     */
    public static void initializeDefaultData(BudgetMasterDatabase database) {
        Log.d(TAG, "🔄 initializeDefaultData: Начинаем инициализацию");
        
        // Инициализируем валюты
        initializeDefaultCurrencies(database);
        
        // Инициализируем категории
        initializeDefaultCategories(database);
        
        // Инициализируем счета
        initializeDefaultAccounts(database);
        
        // Инициализируем бюджеты
        initializeDefaultBudgets(database);
        
        Log.d(TAG, "🔄 initializeDefaultData: Инициализация завершена");
    }
    
    /**
     * Инициализирует дефолтные валюты
     */
    private static void initializeDefaultCurrencies(BudgetMasterDatabase database) {
        Log.d(TAG, "🔄 initializeDefaultCurrencies: Начинаем инициализацию валют");
        
        // Проверяем, есть ли уже валюты
        int currencyCount = database.currencyDao().count();
        if (currencyCount > 0) {
            Log.d(TAG, "🔄 initializeDefaultCurrencies: Валюты уже существуют, пропускаем");
            return;
        }
        
        // Создаем дефолтные валюты
        Currency[] currencies = {
            createCurrency("Рубль", 1),
            createCurrency("Доллар", 2),
            createCurrency("Евро", 3)
        };
        
        for (Currency currency : currencies) {
            database.currencyDao().insert(currency);
            Log.d(TAG, "🔄 initializeDefaultCurrencies: Добавлена валюта: " + currency.getTitle());
        }
        
        Log.d(TAG, "🔄 initializeDefaultCurrencies: Инициализация валют завершена");
    }
    
    /**
     * Инициализирует дефолтные категории
     */
    private static void initializeDefaultCategories(BudgetMasterDatabase database) {
        Log.d(TAG, "🔄 initializeDefaultCategories: Начинаем инициализацию категорий");
        
        // Проверяем, есть ли уже категории
        int categoryCount = database.categoryDao().count();
        if (categoryCount > 0) {
            Log.d(TAG, "🔄 initializeDefaultCategories: Категории уже существуют, пропускаем");
            return;
        }
        
        // Создаем родительские категории
        Category incomeParent = createCategory("Доходы", 1, null, 1);
        Category expenseParent = createCategory("Расходы", 2, null, 2);
        
        database.categoryDao().insert(incomeParent);
        database.categoryDao().insert(expenseParent);
        
        // Получаем ID родительских категорий
        // Category incomeParentCategory = database.categoryDao().getByTitle("Доходы").getValue();
        // Category expenseParentCategory = database.categoryDao().getByTitle("Расходы").getValue();
        // int incomeParentId = incomeParentCategory.getId();
        // int expenseParentId = expenseParentCategory.getId();
        int incomeParentId = 1;
        int expenseParentId = 2;
        
        // Создаем дочерние категории доходов
        Category[] incomeCategories = {
            createCategory("Работа", 1, incomeParentId, 3),
            createCategory("Подработка", 1, incomeParentId, 4),
            createCategory("Подарки", 1, incomeParentId, 5)
        };
        
        for (Category category : incomeCategories) {
            database.categoryDao().insert(category);
        }
        
        // Создаем промежуточные категории расходов
        Category necessary = createCategory("Необходимые", 2, expenseParentId, 6);
        Category additional = createCategory("Дополнительные", 2, expenseParentId, 7);
        
        database.categoryDao().insert(necessary);
        database.categoryDao().insert(additional);
        
        // Получаем ID промежуточных категорий
        // Category necessaryCategory = database.categoryDao().getByTitle("Необходимые").getValue();
        // Category additionalCategory = database.categoryDao().getByTitle("Дополнительные").getValue();
        // int necessaryId = necessaryCategory.getId();
        // int additionalId = additionalCategory.getId();
        int necessaryId = 6;
        int additionalId = 7;
        
        // Создаем дочерние категории необходимых расходов
        Category[] necessaryCategories = {
            createCategory("Коммунальные", 2, necessaryId, 8),
            createCategory("Продукты", 2, necessaryId, 9),
            createCategory("Транспорт", 2, necessaryId, 10),
            createCategory("Медицина", 2, necessaryId, 11),
            createCategory("Одежда", 2, necessaryId, 12),
            createCategory("Налоги", 2, necessaryId, 13)
        };
        
        for (Category category : necessaryCategories) {
            database.categoryDao().insert(category);
        }
        
        // Создаем дочерние категории дополнительных расходов
        Category[] additionalCategories = {
            createCategory("Домашние нужды", 2, additionalId, 14),
            createCategory("Кино", 2, additionalId, 15),
            createCategory("Кафе и рестораны", 2, additionalId, 16),
            createCategory("Подарки", 2, additionalId, 17)
        };
        
        for (Category category : additionalCategories) {
            database.categoryDao().insert(category);
        }
        
        Log.d(TAG, "🔄 initializeDefaultCategories: Инициализация категорий завершена");
    }
    
    /**
     * Инициализирует дефолтные счета
     */
    private static void initializeDefaultAccounts(BudgetMasterDatabase database) {
        Log.d(TAG, "🔄 initializeDefaultAccounts: Начинаем инициализацию счетов");
        
        // Проверяем, есть ли уже счета
        int accountCount = database.accountDao().count();
        if (accountCount > 0) {
            Log.d(TAG, "🔄 initializeDefaultAccounts: Счета уже существуют, пропускаем");
            return;
        }
        
        // Используем константу DEFAULT_CURRENCY_ID из ModelConstants
        int defaultCurrencyId = ModelConstants.DEFAULT_CURRENCY_ID;
        
        // Создаем дефолтные счета
        Account[] accounts = {
            createAccount("Наличные", 1, 0, 1, defaultCurrencyId, 0),
            createAccount("Зарплатная карта", 2, 0, 1, defaultCurrencyId, 0),
            createAccount("Сберегательный счет", 3, 0, 2, defaultCurrencyId, 0),
            createAccount("Кредитная карта", 4, 0, 3, defaultCurrencyId, 0),
            createAccount("Карта рассрочки", 5, 0, 3, defaultCurrencyId, 0)
        };
        
        for (Account account : accounts) {
            database.accountDao().insert(account);
            Log.d(TAG, "🔄 initializeDefaultAccounts: Добавлен счет: " + account.getTitle());
        }
        
        Log.d(TAG, "🔄 initializeDefaultAccounts: Инициализация счетов завершена");
    }
    
    /**
     * Инициализирует дефолтные бюджеты для каждой категории
     */
    private static void initializeDefaultBudgets(BudgetMasterDatabase database) {
        Log.d(TAG, "🔄 initializeDefaultBudgets: Начинаем инициализацию бюджетов");
        
        // Проверяем, есть ли уже бюджеты
        int budgetCount = database.budgetDao().count();
        if (budgetCount > 0) {
            Log.d(TAG, "🔄 initializeDefaultBudgets: Бюджеты уже существуют, пропускаем");
            return;
        }
        
        // Получаем все категории
        List<Category> categories = database.categoryDao().getAllSync();
        if (categories == null || categories.isEmpty()) {
            Log.d(TAG, "❌ initializeDefaultBudgets: Категории не найдены, пропускаем инициализацию бюджетов");
            return;
        }
        
        Log.d(TAG, "🔄 initializeDefaultBudgets: Найдено категорий: " + categories.size());
        
        // Создаем бюджет для каждой категории
        int position = 1;
        for (Category category : categories) {
            Budget budget = createBudget(category.getId(), 0, 1, position);
            database.budgetDao().insert(budget);
            Log.d(TAG, "🔄 initializeDefaultBudgets: Добавлен бюджет для категории '" + category.getTitle() + "' (ID: " + category.getId() + ")");
            position++;
        }
        
        Log.d(TAG, "🔄 initializeDefaultBudgets: Инициализация бюджетов завершена");
    }
    
    /**
     * Создает объект валюты
     */
    private static Currency createCurrency(String title, int position) {
        Currency currency = new Currency();
        currency.setTitle(title);
        currency.setPosition(position);
        currency.setCreateTime(LocalDateTime.now());
        currency.setCreatedBy("initializer");
        return currency;
    }
    
    /**
     * Создает объект категории
     */
    private static Category createCategory(String title, int operationType, Integer parentId, int position) {
        Category category = new Category();
        category.setTitle(title);
        category.setOperationType(operationType);
        category.setParentId(parentId);
        category.setPosition(position);
        category.setCreateTime(LocalDateTime.now());
        category.setCreatedBy("initializer");
        return category;
    }
    
    /**
     * Создает объект счета
     */
    private static Account createAccount(String title, int position, int amount, int type, int currencyId, int closed) {
        Account account = new Account();
        account.setTitle(title);
        account.setPosition(position);
        account.setAmount(amount);
        account.setType(type);
        account.setCurrencyId(currencyId);
        account.setClosed(closed);
        account.setCreateTime(LocalDateTime.now());
        account.setCreatedBy("initializer");
        return account;
    }
    
    /**
     * Создает объект бюджета
     */
    private static Budget createBudget(int categoryId, int amount, int currencyId, int position) {
        Budget budget = new Budget();
        budget.setCategoryId(categoryId);
        budget.setAmount(amount);
        budget.setCurrencyId(currencyId);
        budget.setPosition(position);
        budget.setCreateTime(LocalDateTime.now());
        budget.setCreatedBy("initializer");
        return budget;
    }
    
    /**
     * Очищает все данные из базы
     */
    public static void clearAllData(BudgetMasterDatabase database) {
        Log.d(TAG, "🔄 clearAllData: Начинаем очистку данных");
        
        database.operationDao().deleteAll();
        database.accountDao().deleteAll();
        database.categoryDao().deleteAll();
        database.currencyDao().deleteAll();
        database.budgetDao().deleteAll();
        
        Log.d(TAG, "🔄 clearAllData: Очистка данных завершена");
    }
    
    /**
     * Восстанавливает дефолтные данные
     */
    public static void restoreDefaults(BudgetMasterDatabase database) {
        Log.d(TAG, "🔄 restoreDefaults: Начинаем восстановление дефолтных данных");
        
        // Очищаем все данные
        clearAllData(database);
        
        // Инициализируем дефолтные данные
        initializeDefaultData(database);
        
        Log.d(TAG, "🔄 restoreDefaults: Восстановление завершено");
    }
} 