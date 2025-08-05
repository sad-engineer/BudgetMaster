
package com.sadengineer.budgetmaster.backend.database;

import android.content.Context;
import android.util.Log;

import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.sadengineer.budgetmaster.backend.entity.Account;
import com.sadengineer.budgetmaster.backend.entity.Category;
import com.sadengineer.budgetmaster.backend.entity.Currency;
import com.sadengineer.budgetmaster.backend.entity.Operation;

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
        List<Category> existingCategories = database.categoryDao().getAllActiveCategories();
        if (existingCategories != null && !existingCategories.isEmpty()) {
            Log.d(TAG, "🔄 initializeDefaultCategories: Категории уже существуют, пропускаем");
            return;
        }
        
        // Создаем родительские категории
        Category incomeParent = createCategory("Доходы", 1, null, 1);
        Category expenseParent = createCategory("Расходы", 2, null, 2);
        
        database.categoryDao().insert(incomeParent);
        database.categoryDao().insert(expenseParent);
        
        // Получаем ID родительских категорий
        Category incomeParentCategory = database.categoryDao().getCategoryByTitle("Доходы");
        Category expenseParentCategory = database.categoryDao().getCategoryByTitle("Расходы");
        int incomeParentId = incomeParentCategory.getId();
        int expenseParentId = expenseParentCategory.getId();
        
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
        Category necessaryCategory = database.categoryDao().getCategoryByTitle("Необходимые");
        Category additionalCategory = database.categoryDao().getCategoryByTitle("Дополнительные");
        int necessaryId = necessaryCategory.getId();
        int additionalId = additionalCategory.getId();
        
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
        List<Account> existingAccounts = database.accountDao().getAllActiveAccounts();
        if (existingAccounts != null && !existingAccounts.isEmpty()) {
            Log.d(TAG, "🔄 initializeDefaultAccounts: Счета уже существуют, пропускаем");
            return;
        }
        
        // Используем константу DEFAULT_CURRENCY_ID из ModelConstants
        int defaultCurrencyId = com.sadengineer.budgetmaster.backend.constants.ModelConstants.DEFAULT_CURRENCY_ID;
        
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