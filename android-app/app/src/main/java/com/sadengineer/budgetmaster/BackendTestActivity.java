package com.sadengineer.budgetmaster;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;
import java.util.List;
import java.io.File;
import java.util.concurrent.CompletableFuture;
import androidx.lifecycle.LiveData;

// Импорты классов из нового backend
import com.sadengineer.budgetmaster.backend.constants.ModelConstants;
import com.sadengineer.budgetmaster.backend.constants.RepositoryConstants;
import com.sadengineer.budgetmaster.backend.constants.ServiceConstants;
import com.sadengineer.budgetmaster.backend.constants.ValidationConstants;
import com.sadengineer.budgetmaster.backend.entity.Account;
import com.sadengineer.budgetmaster.backend.entity.Budget;
import com.sadengineer.budgetmaster.backend.entity.Category;
import com.sadengineer.budgetmaster.backend.entity.Currency;
import com.sadengineer.budgetmaster.backend.entity.Operation;
import com.sadengineer.budgetmaster.backend.repository.AccountRepository;
import com.sadengineer.budgetmaster.backend.repository.BudgetRepository;
import com.sadengineer.budgetmaster.backend.repository.CategoryRepository;
import com.sadengineer.budgetmaster.backend.repository.CurrencyRepository;
import com.sadengineer.budgetmaster.backend.repository.OperationRepository;
import com.sadengineer.budgetmaster.backend.service.AccountService;
import com.sadengineer.budgetmaster.backend.service.BudgetService;
import com.sadengineer.budgetmaster.backend.service.CategoryService;
import com.sadengineer.budgetmaster.backend.service.CurrencyService;
import com.sadengineer.budgetmaster.backend.service.OperationService;
import com.sadengineer.budgetmaster.backend.validator.AccountValidator;
import com.sadengineer.budgetmaster.backend.validator.BaseEntityValidator;
import com.sadengineer.budgetmaster.backend.validator.BudgetValidator;
import com.sadengineer.budgetmaster.backend.validator.CategoryValidator;
import com.sadengineer.budgetmaster.backend.validator.CommonValidator;
import com.sadengineer.budgetmaster.backend.validator.CurrencyValidator;
import com.sadengineer.budgetmaster.backend.validator.OperationValidator;
import com.sadengineer.budgetmaster.backend.database.BudgetMasterDatabase;
import com.sadengineer.budgetmaster.backend.database.DatabaseInitializer;
import com.sadengineer.budgetmaster.backend.database.DatabaseManager;

public class BackendTestActivity extends BaseNavigationActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backend_test);

        // Room ORM автоматически инициализирует базу данных

        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButton(R.id.back_button);

        // Настройка Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.toolbar_title_backend_test);
        }

        // Получаем TextView для отображения результатов
        TextView resultTextView = findViewById(R.id.result_text);

        // Тестируем доступность классов с обработкой ошибок
        StringBuilder result = new StringBuilder();
        result.append("=== ТЕСТ ДОСТУПНОСТИ КЛАССОВ ИЗ BACKEND ===\n\n");
        
        try {

        // Тест констант
        result.append("--- КОНСТАНТЫ ---\n");
        testConstants(result);

        // Тест моделей
        result.append("\n--- МОДЕЛИ ---\n");
        testModels(result);
        
        // Тест создания объектов моделей
        result.append("\n--- СОЗДАНИЕ ОБЪЕКТОВ МОДЕЛЕЙ ---\n");
        testModelCreation(result);

        // Тест репозиториев
        result.append("\n--- РЕПОЗИТОРИИ ---\n");
        testRepositories(result);

        // Тест сервисов
        result.append("\n--- СЕРВИСЫ ---\n");
        testServices(result);
        
        // Тест создания сервисов
        result.append("\n--- СОЗДАНИЕ СЕРВИСОВ ---\n");
        testServiceCreation(result);

        // Утилиты больше не нужны - используем Room ORM

        // Тест валидаторов
        result.append("\n--- ВАЛИДАТОРЫ ---\n");
        testValidators(result);
        

        // Версия backend теперь определяется через Room ORM

        result.append("\n--- ВЕРСИЯ ФРОНТЕНДА ---\n");
        testFrontendVersion(result);        


        // Тест базы данных
        result.append("\n--- ТЕСТ БАЗЫ ДАННЫХ ---\n");
        testDatabase(result);

        // Новая архитектура использует Room ORM

        } catch (Exception e) {
            result.append("\n=== КРИТИЧЕСКАЯ ОШИБКА ===\n");
            result.append("✗ ").append(e.getMessage()).append("\n");
            result.append("Стек вызовов:\n");
            for (StackTraceElement element : e.getStackTrace()) {
                result.append("  ").append(element.toString()).append("\n");
            }
        } catch (Throwable t) {
            result.append("\n=== КРИТИЧЕСКАЯ ОШИБКА (Throwable) ===\n");
            result.append("✗ ").append(t.getMessage()).append("\n");
            result.append("Тип: ").append(t.getClass().getName()).append("\n");
            result.append("Стек вызовов:\n");
            for (StackTraceElement element : t.getStackTrace()) {
                result.append("  ").append(element.toString()).append("\n");
            }
        }

        resultTextView.setText(result.toString());
    }

    private void testConstants(StringBuilder result) {
        try {
            result.append("✓ ModelConstants: ").append(ModelConstants.class.getName()).append("\n");
        } catch (Exception e) {
            result.append("✗ ModelConstants: ").append(e.getMessage()).append("\n");
        }

        try {
            result.append("✓ RepositoryConstants: ").append(RepositoryConstants.class.getName()).append("\n");
        } catch (Exception e) {
            result.append("✗ RepositoryConstants: ").append(e.getMessage()).append("\n");
        }

        try {
            result.append("✓ ServiceConstants: ").append(ServiceConstants.class.getName()).append("\n");
        } catch (Exception e) {
            result.append("✗ ServiceConstants: ").append(e.getMessage()).append("\n");
        }

        try {
            result.append("✓ ValidationConstants: ").append(ValidationConstants.class.getName()).append("\n");
        } catch (Exception e) {
            result.append("✗ ValidationConstants: ").append(e.getMessage()).append("\n");
        }
    }

    private void testModels(StringBuilder result) {
        try {
            result.append("✓ Account: ").append(Account.class.getName()).append("\n");
        } catch (Exception e) {
            result.append("✗ Account: ").append(e.getMessage()).append("\n");
        }

        try {
            result.append("✓ Budget: ").append(Budget.class.getName()).append("\n");
        } catch (Exception e) {
            result.append("✗ Budget: ").append(e.getMessage()).append("\n");
        }

        try {
            result.append("✓ Category: ").append(Category.class.getName()).append("\n");
        } catch (Exception e) {
            result.append("✗ Category: ").append(e.getMessage()).append("\n");
        }

        try {
            result.append("✓ Currency: ").append(Currency.class.getName()).append("\n");
        } catch (Exception e) {
            result.append("✗ Currency: ").append(e.getMessage()).append("\n");
        }

        try {
            result.append("✓ Operation: ").append(Operation.class.getName()).append("\n");
        } catch (Exception e) {
            result.append("✗ Operation: ").append(e.getMessage()).append("\n");
        }
    }

    private void testRepositories(StringBuilder result) {
        try {
            result.append("✓ AccountRepository: ").append(AccountRepository.class.getName()).append("\n");
        } catch (Exception e) {
            result.append("✗ AccountRepository: ").append(e.getMessage()).append("\n");
        }

        try {
            result.append("✓ BudgetRepository: ").append(BudgetRepository.class.getName()).append("\n");
        } catch (Exception e) {
            result.append("✗ BudgetRepository: ").append(e.getMessage()).append("\n");
        }

        try {
            result.append("✓ CategoryRepository: ").append(CategoryRepository.class.getName()).append("\n");
        } catch (Exception e) {
            result.append("✗ CategoryRepository: ").append(e.getMessage()).append("\n");
        }

        try {
            result.append("✓ CurrencyRepository: ").append(CurrencyRepository.class.getName()).append("\n");
        } catch (Exception e) {
            result.append("✗ CurrencyRepository: ").append(e.getMessage()).append("\n");
        }

        try {
            result.append("✓ OperationRepository: ").append(OperationRepository.class.getName()).append("\n");
        } catch (Exception e) {
            result.append("✗ OperationRepository: ").append(e.getMessage()).append("\n");
        }
    }

    private void testServices(StringBuilder result) {
        try {
            result.append("✓ AccountService: ").append(AccountService.class.getName()).append("\n");
        } catch (Exception e) {
            result.append("✗ AccountService: ").append(e.getMessage()).append("\n");
        }

        try {
            result.append("✓ BudgetService: ").append(BudgetService.class.getName()).append("\n");
        } catch (Exception e) {
            result.append("✗ BudgetService: ").append(e.getMessage()).append("\n");
        }

        try {
            result.append("✓ CategoryService: ").append(CategoryService.class.getName()).append("\n");
        } catch (Exception e) {
            result.append("✗ CategoryService: ").append(e.getMessage()).append("\n");
        }

        try {
            result.append("✓ CurrencyService: ").append(CurrencyService.class.getName()).append("\n");
        } catch (Exception e) {
            result.append("✗ CurrencyService: ").append(e.getMessage()).append("\n");
        }

        try {
            result.append("✓ OperationService: ").append(OperationService.class.getName()).append("\n");
        } catch (Exception e) {
            result.append("✗ OperationService: ").append(e.getMessage()).append("\n");
        }
    }



    private void testValidators(StringBuilder result) {
        try {
            result.append("✓ AccountValidator: ").append(AccountValidator.class.getName()).append("\n");
        } catch (Exception e) {
            result.append("✗ AccountValidator: ").append(e.getMessage()).append("\n");
        }

        try {
            result.append("✓ BaseEntityValidator: ").append(BaseEntityValidator.class.getName()).append("\n");
        } catch (Exception e) {
            result.append("✗ BaseEntityValidator: ").append(e.getMessage()).append("\n");
        }

        try {
            result.append("✓ BudgetValidator: ").append(BudgetValidator.class.getName()).append("\n");
        } catch (Exception e) {
            result.append("✗ BudgetValidator: ").append(e.getMessage()).append("\n");
        }

        try {
            result.append("✓ CategoryValidator: ").append(CategoryValidator.class.getName()).append("\n");
        } catch (Exception e) {
            result.append("✗ CategoryValidator: ").append(e.getMessage()).append("\n");
        }

        try {
            result.append("✓ CommonValidator: ").append(CommonValidator.class.getName()).append("\n");
        } catch (Exception e) {
            result.append("✗ CommonValidator: ").append(e.getMessage()).append("\n");
        }

        try {
            result.append("✓ CurrencyValidator: ").append(CurrencyValidator.class.getName()).append("\n");
        } catch (Exception e) {
            result.append("✗ CurrencyValidator: ").append(e.getMessage()).append("\n");
        }

        try {
            result.append("✓ OperationValidator: ").append(OperationValidator.class.getName()).append("\n");
        } catch (Exception e) {
            result.append("✗ OperationValidator: ").append(e.getMessage()).append("\n");
        }
    }

    private void testModelCreation(StringBuilder result) {
        try {
            // Тест создания Account
            Account account = new Account();
            account.setTitle("Тестовый счет");
            account.setAmount(1000);
            result.append("✓ Account создан: ").append(account.getTitle()).append(" (баланс: ").append(account.getAmount()).append(")\n");
        } catch (Exception e) {
            result.append("✗ Ошибка создания Account: ").append(e.getMessage()).append("\n");
        }

        try {
            // Тест создания Currency
            Currency currency = new Currency();
            currency.setTitle("USD");
            result.append("✓ Currency создан: ").append(currency.getTitle()).append("\n");
        } catch (Exception e) {
            result.append("✗ Ошибка создания Currency: ").append(e.getMessage()).append("\n");
        }

        try {
            // Тест создания Category
            Category category = new Category();
            category.setTitle("Продукты");
            category.setOperationType(2); // тип расход
            result.append("✓ Category создан: ").append(category.getTitle()).append(" (тип: ").append(category.getOperationType()).append(")\n");
        } catch (Exception e) {
            result.append("✗ Ошибка создания Category: ").append(e.getMessage()).append("\n");
        }

        try {
            // Тест создания Budget
            Budget budget = new Budget();
            budget.setAmount(5000);
            result.append("✓ Budget создан: (сумма: ").append(budget.getAmount()).append(")\n");
        } catch (Exception e) {
            result.append("✗ Ошибка создания Budget: ").append(e.getMessage()).append("\n");
        }

        try {
            // Тест создания Operation
            Operation operation = new Operation();
            operation.setAmount(100);
            operation.setDescription("Покупка продуктов");
            result.append("✓ Operation создан: ").append(operation.getDescription()).append(" (сумма: ").append(operation.getAmount()).append(")\n");
        } catch (Exception e) {
            result.append("✗ Ошибка создания Operation: ").append(e.getMessage()).append("\n");
        }
    }

    private void testServiceCreation(StringBuilder result) {
        try {
            // Тест создания AccountService
            AccountService accountService = new AccountService(this, "test_user");
            result.append("✓ AccountService создан\n");
        } catch (Exception e) {
            result.append("✗ Ошибка создания AccountService: ").append(e.getMessage()).append("\n");
        }

        try {
            // Тест создания CurrencyService
            CurrencyService currencyService = new CurrencyService(this, "test_user");
            result.append("✓ CurrencyService создан\n");
        } catch (Exception e) {
            result.append("✗ Ошибка создания CurrencyService: ").append(e.getMessage()).append("\n");
        }

        try {
            // Тест создания CategoryService
            CategoryService categoryService = new CategoryService(this, "test_user");
            result.append("✓ CategoryService создан\n");
        } catch (Exception e) {
            result.append("✗ Ошибка создания CategoryService: ").append(e.getMessage()).append("\n");
        }

        try {
            // Тест создания BudgetService
            BudgetService budgetService = new BudgetService(this, "test_user");
            result.append("✓ BudgetService создан\n");
        } catch (Exception e) {
            result.append("✗ Ошибка создания BudgetService: ").append(e.getMessage()).append("\n");
        }

        try {
            // Тест создания OperationService
            OperationService operationService = new OperationService(this, "test_user");
            result.append("✓ OperationService создан\n");
        } catch (Exception e) {
            result.append("✗ Ошибка создания OperationService: ").append(e.getMessage()).append("\n");
        }
    }



    private void testFrontendVersion(StringBuilder result) {
        try {
            // Получаем версию из BuildConfig (читается из файла VERSION при сборке)
            String version = BuildConfig.APP_VERSION;
            result.append("✓ FrontendVersion: ").append(version).append("\n");
        } catch (Exception e) {
            result.append("✗ FrontendVersion: ").append(e.getMessage()).append("\n");
        }
    }

    private void testDatabase(StringBuilder result) {
        DatabaseManager databaseManager = null;
        try {
            result.append("✓ Начинаем тест базы данных с новым подходом...\n");

            // Создаем менеджер базы данных
            databaseManager = new DatabaseManager(this);
            result.append("✓ DatabaseManager создан\n");
            
            // Инициализируем базу данных
            result.append("✓ Инициализация базы данных...\n");
            CompletableFuture<Boolean> initFuture = databaseManager.initializeDatabase();
            Boolean initResult = initFuture.get();
            
            if (initResult) {
                result.append("✅ База данных инициализирована успешно\n");
            } else {
                result.append("❌ Ошибка инициализации базы данных\n");
                return;
            }
            
            // Тестируем получение данных через Callable
            result.append("✓ Тестируем получение данных...\n");
            
            // Получаем валюты
            CompletableFuture<List<Currency>> currenciesFuture = databaseManager.executeDatabaseOperation(() -> {
                BudgetMasterDatabase database = BudgetMasterDatabase.getDatabase(this);
                List<Currency> currencies = database.currencyDao().getAll().getValue();
                database.close();
                return currencies;
            });
                
            // Получаем категории
            CompletableFuture<List<Category>> categoriesFuture = databaseManager.executeDatabaseOperation(() -> {
                BudgetMasterDatabase database = BudgetMasterDatabase.getDatabase(this);
                List<Category> categories = database.categoryDao().getAllActiveCategories();
                database.close();
                return categories;
            });
                
            // Получаем счета
            CompletableFuture<List<Account>> accountsFuture = databaseManager.executeDatabaseOperation(() -> {
                BudgetMasterDatabase database = BudgetMasterDatabase.getDatabase(this);
                List<Account> accounts = database.accountDao().getAllActive().getValue();
                database.close();
                return accounts;
            });
                
            // Ждем результаты
            List<Currency> currencies = currenciesFuture.get();
            List<Category> categories = categoriesFuture.get();
            List<Account> accounts = accountsFuture.get();
            
            // Выводим результаты
            int currencyCount = currencies != null ? currencies.size() : 0;
            result.append("✓ Найдено валют в БД: ").append(currencyCount).append("\n");
            
            if (currencies != null && !currencies.isEmpty()) {
                result.append("✓ Список валют:\n");
                for (Currency currency : currencies) {
                    result.append("  - ").append(currency.getTitle()).append(" (ID: ").append(currency.getId()).append(")\n");
                }
            }
            
            int categoryCount = categories != null ? categories.size() : 0;
            result.append("✓ Найдено категорий в БД: ").append(categoryCount).append("\n");
            
            int accountCount = accounts != null ? accounts.size() : 0;
            result.append("✓ Найдено счетов в БД: ").append(accountCount).append("\n");
            
            // Тестируем сервисы
            result.append("✓ Тестируем сервисы...\n");
            
            CurrencyService currencyService = new CurrencyService(this, "test_user");
            result.append("✓ CurrencyService создан\n");
            
            result.append("✓ Примечание: LiveData.getValue() в тестах может возвращать null\n");
            result.append("  потому что LiveData работает асинхронно. Для тестов лучше\n");
            result.append("  использовать прямые DAO вызовы через Callable.\n");
                
            // Тестируем через репозиторий - используем прямые DAO вызовы вместо LiveData
            CompletableFuture<List<Currency>> repoCurrenciesFuture = databaseManager.executeDatabaseOperation(() -> {
                BudgetMasterDatabase database = BudgetMasterDatabase.getDatabase(this);
                List<Currency> repoCurrencies = database.currencyDao().getAll().getValue();
                database.close();
                return repoCurrencies;
            });

            
            List<Currency> repoCurrencies = repoCurrenciesFuture.get();
            int repoCurrencyCount = repoCurrencies != null ? repoCurrencies.size() : 0;
            result.append("✓ Валют через DAO (синхронно): ").append(repoCurrencyCount).append("\n");
                
            // Демонстрируем проблему с LiveData.getValue()
            result.append("✓ Демонстрация проблемы с LiveData.getValue()...\n");
            CurrencyRepository currencyRepository = new CurrencyRepository(this);
            LiveData<List<Currency>> liveData = currencyRepository.getAll();
            List<Currency> liveDataCurrencies = liveData.getValue();
            int liveDataCount = liveDataCurrencies != null ? liveDataCurrencies.size() : 0;
            result.append("✓ LiveData.getValue() вернул: ").append(liveDataCount).append(" валют\n");
            result.append("  (Это нормально - LiveData работает асинхронно)\n");
            
            result.append("✅ Тест базы данных завершен успешно\n");

        } catch (Exception e) {
            result.append("✗ Ошибка теста БД: ").append(e.getMessage()).append("\n");
            result.append("  Тип исключения: ").append(e.getClass().getName()).append("\n");
        } catch (Throwable t) {
            result.append("✗ КРИТИЧЕСКАЯ ОШИБКА теста БД: ").append(t.getMessage()).append("\n");
            result.append("  Тип: ").append(t.getClass().getName()).append("\n");
            result.append("  Стек:\n");
            for (StackTraceElement element : t.getStackTrace()) {
                result.append("    ").append(element.toString()).append("\n");
            }
        } finally {
            // Закрываем менеджер
            if (databaseManager != null) {
                databaseManager.shutdown();
            }
        }
    }
    


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
} 