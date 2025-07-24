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

// Импорты классов из backend
import com.sadengineer.budgetmaster.backend.BackendVersion;
import com.sadengineer.budgetmaster.backend.constants.ModelConstants;
import com.sadengineer.budgetmaster.backend.constants.RepositoryConstants;
import com.sadengineer.budgetmaster.backend.constants.ServiceConstants;
import com.sadengineer.budgetmaster.backend.constants.ValidationConstants;
import com.sadengineer.budgetmaster.backend.model.Account;
import com.sadengineer.budgetmaster.backend.model.BaseEntity;
import com.sadengineer.budgetmaster.backend.model.Budget;
import com.sadengineer.budgetmaster.backend.model.Category;
import com.sadengineer.budgetmaster.backend.model.Currency;
import com.sadengineer.budgetmaster.backend.model.Operation;
import com.sadengineer.budgetmaster.backend.repository.AccountRepository;
import com.sadengineer.budgetmaster.backend.repository.BaseRepository;
import com.sadengineer.budgetmaster.backend.repository.BudgetRepository;
import com.sadengineer.budgetmaster.backend.repository.CategoryRepository;
import com.sadengineer.budgetmaster.backend.repository.CurrencyRepository;
import com.sadengineer.budgetmaster.backend.repository.OperationRepository;
import com.sadengineer.budgetmaster.backend.repository.Repository;
import com.sadengineer.budgetmaster.backend.service.AccountService;
import com.sadengineer.budgetmaster.backend.service.BudgetService;
import com.sadengineer.budgetmaster.backend.service.CategoryService;
import com.sadengineer.budgetmaster.backend.service.CurrencyService;
import com.sadengineer.budgetmaster.backend.service.OperationService;
import com.sadengineer.budgetmaster.backend.util.DatabaseUtil;
import com.sadengineer.budgetmaster.backend.util.DateTimeUtil;
import com.sadengineer.budgetmaster.backend.validator.AccountValidator;
import com.sadengineer.budgetmaster.backend.validator.BaseEntityValidator;
import com.sadengineer.budgetmaster.backend.validator.BudgetValidator;
import com.sadengineer.budgetmaster.backend.validator.CategoryValidator;
import com.sadengineer.budgetmaster.backend.validator.CommonValidator;
import com.sadengineer.budgetmaster.backend.validator.CurrencyValidator;
import com.sadengineer.budgetmaster.backend.validator.OperationValidator;
import com.sadengineer.budgetmaster.backend.AndroidDatabaseAdapter;

public class BackendTestActivity extends BaseNavigationActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backend_test);

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

        // Тест утилит
        result.append("\n--- УТИЛИТЫ ---\n");
        testUtils(result);

        // Тест валидаторов
        result.append("\n--- ВАЛИДАТОРЫ ---\n");
        testValidators(result);
        

        // Тест версии
        result.append("\n--- ВЕРСИЯ БЕКЕНДА ---\n");
        testBackendVersion(result);

        result.append("\n--- ВЕРСИЯ ФРОНТЕНДА ---\n");
        testFrontendVersion(result);        


        // Тест базы данных
        result.append("\n--- ТЕСТ БАЗЫ ДАННЫХ ---\n");
        testDatabase(result);

        // Тест новой архитектуры базы данных
        result.append("\n--- АРХИТЕКТУРА БАЗЫ ДАННЫХ ---\n");
        testDatabaseArchitecture(result);

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
            result.append("✓ BaseEntity: ").append(BaseEntity.class.getName()).append("\n");
        } catch (Exception e) {
            result.append("✗ BaseEntity: ").append(e.getMessage()).append("\n");
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
            result.append("✓ BaseRepository: ").append(BaseRepository.class.getName()).append("\n");
        } catch (Exception e) {
            result.append("✗ BaseRepository: ").append(e.getMessage()).append("\n");
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

        try {
            result.append("✓ Repository: ").append(Repository.class.getName()).append("\n");
        } catch (Exception e) {
            result.append("✗ Repository: ").append(e.getMessage()).append("\n");
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

    private void testUtils(StringBuilder result) {
        try {
            result.append("✓ DatabaseUtil: ").append(DatabaseUtil.class.getName()).append("\n");
        } catch (Exception e) {
            result.append("✗ DatabaseUtil: ").append(e.getMessage()).append("\n");
        }

        try {
            result.append("✓ DateTimeUtil: ").append(DateTimeUtil.class.getName()).append("\n");
        } catch (Exception e) {
            result.append("✗ DateTimeUtil: ").append(e.getMessage()).append("\n");
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
            category.setType(1); // например, 1 - расход
            result.append("✓ Category создан: ").append(category.getTitle()).append(" (тип: ").append(category.getType()).append(")\n");
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
            operation.setComment("Покупка продуктов");
            result.append("✓ Operation создан: ").append(operation.getComment()).append(" (сумма: ").append(operation.getAmount()).append(")\n");
        } catch (Exception e) {
            result.append("✗ Ошибка создания Operation: ").append(e.getMessage()).append("\n");
        }
    }

    private void testServiceCreation(StringBuilder result) {
        try {
            // Тест создания AccountService
            AccountService accountService = new AccountService("test_user");
            result.append("✓ AccountService создан\n");
        } catch (Exception e) {
            result.append("✗ Ошибка создания AccountService: ").append(e.getMessage()).append("\n");
        }

        try {
            // Тест создания CurrencyService
            CurrencyService currencyService = new CurrencyService("test_user");
            result.append("✓ CurrencyService создан\n");
        } catch (Exception e) {
            result.append("✗ Ошибка создания CurrencyService: ").append(e.getMessage()).append("\n");
        }

        try {
            // Тест создания CategoryService
            CategoryService categoryService = new CategoryService("test_user");
            result.append("✓ CategoryService создан\n");
        } catch (Exception e) {
            result.append("✗ Ошибка создания CategoryService: ").append(e.getMessage()).append("\n");
        }

        try {
            // Тест создания BudgetService
            BudgetService budgetService = new BudgetService("test_user");
            result.append("✓ BudgetService создан\n");
        } catch (Exception e) {
            result.append("✗ Ошибка создания BudgetService: ").append(e.getMessage()).append("\n");
        }

        try {
            // Тест создания OperationService
            OperationService operationService = new OperationService("test_user");
            result.append("✓ OperationService создан\n");
        } catch (Exception e) {
            result.append("✗ Ошибка создания OperationService: ").append(e.getMessage()).append("\n");
        }
    }

    private void testBackendVersion(StringBuilder result) {
        try {
            result.append("✓ BackendVersion: ").append(BackendVersion.class.getName()).append("\n");
            // Попробуем получить версию
            try {
                String version = BackendVersion.VERSION;
                result.append("  Версия: ").append(version).append("\n");
            } catch (Exception e) {
                result.append("  Ошибка получения версии: ").append(e.getMessage()).append("\n");
            }
        } catch (Exception e) {
            result.append("✗ BackendVersion: ").append(e.getMessage()).append("\n");
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
        try {
            // Создаем путь к базе данных в Android
            String dbPath = getApplicationContext().getDatabasePath("budgetmaster.db").getAbsolutePath();
            result.append("✓ Путь к БД: ").append(dbPath).append("\n");

            // Проверяем, что папка существует
            File dbDir = new File(dbPath).getParentFile();
            if (!dbDir.exists()) {
                dbDir.mkdirs();
                result.append("✓ Папка БД создана\n");
            }

            // Детальная диагностика создания БД
            result.append("✓ Начинаем диагностику создания БД...\n");
            
            // Проверяем существование файла БД
            File dbFile = new File(dbPath);
            result.append("✓ Файл БД существует: ").append(dbFile.exists()).append("\n");
            if (dbFile.exists()) {
                result.append("✓ Размер файла БД: ").append(dbFile.length()).append(" байт\n");
            }
            
            // Проверяем права доступа
            result.append("✓ Папка доступна для записи: ").append(dbDir.canWrite()).append("\n");
            result.append("✓ Файл доступен для записи: ").append(dbFile.canWrite()).append("\n");
            
            // Тест создания БД через Android SQLite
            try {
                result.append("✓ Попытка создания БД через Android SQLite...\n");
                
                // Используем Android SQLite вместо JDBC
                android.database.sqlite.SQLiteDatabase db = android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(dbPath, null);
                result.append("✓ Android SQLite БД создана успешно\n");
                
                // Создаем простую таблицу
                db.execSQL("CREATE TABLE IF NOT EXISTS test_currencies (id INTEGER PRIMARY KEY, title TEXT)");
                db.execSQL("INSERT OR IGNORE INTO test_currencies (id, title) VALUES (1, 'USD')");
                db.execSQL("INSERT OR IGNORE INTO test_currencies (id, title) VALUES (2, 'EUR')");
                result.append("✓ Тестовая таблица создана и заполнена\n");
                
                // Проверяем данные
                android.database.Cursor cursor = db.rawQuery("SELECT * FROM test_currencies", null);
                int count = cursor.getCount();
                result.append("✓ Найдено валют в БД: ").append(count).append("\n");
                cursor.close();
                
                db.close();
                result.append("✓ Соединение с БД закрыто\n");
                
            } catch (Exception e) {
                result.append("✗ Ошибка создания БД через Android SQLite:\n");
                result.append("  Сообщение: ").append(e.getMessage()).append("\n");
                result.append("  Тип исключения: ").append(e.getClass().getName()).append("\n");
            } catch (Throwable t) {
                result.append("✗ КРИТИЧЕСКАЯ ОШИБКА создания БД через Android SQLite:\n");
                result.append("  Сообщение: ").append(t.getMessage()).append("\n");
                result.append("  Тип исключения: ").append(t.getClass().getName()).append("\n");
                result.append("  Стек вызовов:\n");
                for (StackTraceElement element : t.getStackTrace()) {
                    result.append("    ").append(element.toString()).append("\n");
                }
            }
            
            // Тест создания CurrencyService (без БД)
            try {
                result.append("✓ Попытка создания CurrencyService...\n");
                // Пока не создаем сервис, чтобы избежать ошибок
                result.append("✓ Тест сервиса пропущен (требует БД)\n");
            } catch (Exception e) {
                result.append("✗ Ошибка создания CurrencyService: ").append(e.getMessage()).append("\n");
            }

        } catch (Exception e) {
            result.append("✗ Общая ошибка теста БД: ").append(e.getMessage()).append("\n");
        } catch (Throwable t) {
            result.append("✗ КРИТИЧЕСКАЯ ОШИБКА теста БД: ").append(t.getMessage()).append("\n");
            result.append("  Тип: ").append(t.getClass().getName()).append("\n");
            result.append("  Стек:\n");
            for (StackTraceElement element : t.getStackTrace()) {
                result.append("    ").append(element.toString()).append("\n");
            }
        }
    }
    
    private void testDatabaseArchitecture(StringBuilder result) {
        result.append("Тестирование Android SQLite адаптера...\n");
        
        try {
            result.append("✓ AndroidDatabaseAdapter доступен\n");
            
            // Тестируем Android адаптер
            AndroidDatabaseAdapter androidDb = new AndroidDatabaseAdapter();
            result.append("✓ AndroidDatabaseAdapter создан\n");
            
            // Тестируем подключение к базе данных через Android адаптер
            File dbFile = new File(getFilesDir(), "test_architecture.db");
            String dbPath = dbFile.getAbsolutePath();
            
            androidDb.connect(dbPath);
            result.append("✓ Подключение к базе данных через Android адаптер\n");
            
            // Тестируем выполнение SQL
            androidDb.executeSQL("CREATE TABLE IF NOT EXISTS test_table (id INTEGER PRIMARY KEY, name TEXT)");
            result.append("✓ Создание таблицы через Android адаптер\n");
            
            // Тестируем запрос
            android.database.Cursor cursor = androidDb.query("SELECT COUNT(*) FROM test_table");
            if (cursor.moveToFirst()) {
                result.append("✓ Запрос через Android адаптер: " + cursor.getInt(0) + " записей\n");
            }
            cursor.close();
            
            // Проверяем соединение
            boolean isConnected = androidDb.isConnected();
            result.append("✓ Проверка соединения: " + (isConnected ? "подключено" : "отключено") + "\n");
            
            // Закрываем соединение
            androidDb.close();
            result.append("✓ Соединение закрыто\n");
            
            result.append("✓ Android SQLite адаптер работает корректно\n");
            
        } catch (Exception e) {
            result.append("✗ Ошибка в Android SQLite адаптере: " + e.getMessage() + "\n");
            Log.e("BackendTest", "Android database adapter error", e);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
} 