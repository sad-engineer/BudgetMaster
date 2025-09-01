package com.sadengineer.budgetmaster.backend.service;

import android.content.Context;
import android.util.Log;

/**
 * Менеджер сервисов, предоставляющий централизованный доступ ко всем сервисам приложения
 * Позволяет обращаться к сервисам через вложенную структуру: ServiceManager.accounts.getCount()
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
        this.accounts = new Accounts();
        this.budgets = new Budgets();
        this.categories = new Categories();
        this.currencies = new Currencies();
        this.operations = new Operations();
        
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
     */
    public class Accounts {
        private final AccountService service;
        
        private Accounts() {
            this.service = new AccountService(context, userName);
        }
        
        /**
         * Получить прямой доступ к AccountService
         * Использование: ServiceManager.getInstance().accounts.service.methodName()
         */
        public AccountService service() {
            return service;
        }
    }
    
    /**
     * Вложенный класс для работы с бюджетами
     */
    public class Budgets {
        private final BudgetService service;
        
        private Budgets() {
            this.service = new BudgetService(context, userName);
        }
        
        /**
         * Получить прямой доступ к BudgetService
         * Использование: ServiceManager.getInstance().budgets.service.methodName()
         */
        public BudgetService service() {
            return service;
        }
    }
    
    /**
     * Вложенный класс для работы с категориями
     */
    public class Categories {
        private final CategoryService service;
        
        private Categories() {
            this.service = new CategoryService(context, userName);
        }
        
        /**
         * Получить прямой доступ к CategoryService
         * Использование: ServiceManager.getInstance().categories.service.methodName()
         */
        public CategoryService service() {
            return service;
        }
    }
    
    /**
     * Вложенный класс для работы с валютами
     */
    public class Currencies {
        private final CurrencyService service;
        
        private Currencies() {
            this.service = new CurrencyService(context, userName);
        }
        
        /**
         * Получить прямой доступ к CurrencyService
         * Использование: ServiceManager.getInstance().currencies.service.methodName()
         */
        public CurrencyService service() {
            return service;
        }
    }
    
    /**
     * Вложенный класс для работы с операциями
     */
    public class Operations {
        private final OperationService service;
        
        private Operations() {
            this.service = new OperationService(context, userName);
        }
        
        /**
         * Получить прямой доступ к OperationService
         * Использование: ServiceManager.getInstance().operations.service.methodName()
         */
        public OperationService service() {
            return service;
        }
    }
    
    /**
     * Получить прямой доступ к AccountService
     * Использование: ServiceManager.getInstance().getAccountService().methodName()
     */
    public AccountService getAccountService() {
        return accounts.service();
    }

    /**
     * Получить прямой доступ к BudgetService
     * Использование: ServiceManager.getInstance().getBudgetService().methodName()
     */
    public BudgetService getBudgetService() {
        return budgets.service();
    }

    /**
     * Получить прямой доступ к CategoryService
     * Использование: ServiceManager.getInstance().getCategoryService().methodName()
     */
    public CategoryService getCategoryService() {
        return categories.service();
    }

    /**
     * Получить прямой доступ к CurrencyService
     * Использование: ServiceManager.getInstance().getCurrencyService().methodName()
     */
    public CurrencyService getCurrencyService() {
        return currencies.service();
    }

    /**
     * Получить прямой доступ к OperationService
     * Использование: ServiceManager.getInstance().getOperationService().methodName()
     */
    public OperationService getOperationService() {
        return operations.service();
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
 * 3. СПОСОБ 1 - Через вложенную структуру (как в Python):
 * 
 *    // Работа с аккаунтами
 *    sm.accounts.service().changePosition(account, newPosition);
 *    sm.accounts.service().getAll();
 *    sm.accounts.service().getById(id);
 * 
 *    // Работа с бюджетами
 *    sm.budgets.service().create(budget);
 *    sm.budgets.service().update(budget);
 *    sm.budgets.service().delete(id);
 * 
 *    // Работа с категориями
 *    sm.categories.service().getAll();
 *    sm.categories.service().getByType(type);
 * 
 *    // Работа с валютами
 *    sm.currencies.service().getDefault();
 *    sm.currencies.service().getAll();
 * 
 *    // Работа с операциями
 *    sm.operations.service().getCount();
 *    sm.operations.service().getOperationsByDateRange(start, end);
 *    sm.operations.service().create(operation);
 * 
 * 4. СПОСОБ 2 - Через прямые методы (более короткий синтаксис):
 * 
 *    // Работа с аккаунтами
 *    sm.getAccountService().changePosition(account, newPosition);
 *    sm.getAccountService().getAll();
 *    sm.getAccountService().getById(id);
 * 
 *    // Работа с бюджетами
 *    sm.getBudgetService().create(budget);
 *    sm.getBudgetService().update(budget);
 *    sm.getBudgetService().delete(id);
 * 
 *    // Работа с категориями
 *    sm.getCategoryService().getAll();
 *    sm.getCategoryService().getByType(type);
 * 
 *    // Работа с валютами
 *    sm.getCurrencyService().getDefault();
 *    sm.getCurrencyService().getAll();
 * 
 *    // Работа с операциями
 *    sm.getOperationService().getCount();
 *    sm.getOperationService().getOperationsByDateRange(start, end);
 *    sm.getOperationService().create(operation);
 * 
 * 5. Альтернативный способ получения сервиса:
 *    AccountService accountService = sm.getAccountService();
 *    accountService.changePosition(account, newPosition);
 * 
 * 6. Сброс экземпляра (при смене пользователя):
 *    ServiceManager.resetInstance();
 *    ServiceManager.getInstance(context, "new_user");
 * 
 * ПРЕИМУЩЕСТВА:
 * - Централизованный доступ ко всем сервисам
 * - Единая точка инициализации
 * - Четкая структура namespace (как в Python)
 * - Легко добавлять новые сервисы
 * - Singleton паттерн для экономии памяти
 * - Два способа доступа: вложенная структура или прямые методы
 * 
 * РЕКОМЕНДАЦИИ:
 * - Используйте Способ 1 (вложенная структура) для более читаемого кода
 * - Используйте Способ 2 (прямые методы) для более короткого синтаксиса
 * - Оба способа дают одинаковый результат
 */
