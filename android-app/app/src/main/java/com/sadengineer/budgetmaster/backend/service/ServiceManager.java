package com.sadengineer.budgetmaster.backend.service;

import android.content.Context;
import android.util.Log;

import com.sadengineer.budgetmaster.backend.entity.Operation;
import com.sadengineer.budgetmaster.backend.entity.KeyValuePair;
import com.sadengineer.budgetmaster.backend.entity.AccountSummary;
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Менеджер сервисов, предоставляющий централизованный доступ ко всем сервисам приложения
 * Позволяет обращаться к сервисам напрямую: ServiceManager.accounts.getAll()
 */
public class ServiceManager {
    private static final String TAG = "ServiceManager";
    
    private static ServiceManager instance;
    private final Context context;
    private final String userName;
    
    // Вложенные классы для каждого сервиса
    public final Accounts accounts;
    public final Budgets budgets;
    public final Categories categories;
    public final Currencies currencies;
    public final Operations operations;
    
    private ServiceManager(Context context, String userName) {
        this.context = context;
        this.userName = userName;
        
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
}

/*
 * ПРИМЕРЫ ИСПОЛЬЗОВАНИЯ ServiceManager:
 * 
 * 1. Инициализация в MainActivity или Application:
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
