package com.sadengineer.budgetmaster.backend.service;

import android.content.Context;
import android.util.Log;

import com.sadengineer.budgetmaster.backend.filters.EntityFilter;
import com.sadengineer.budgetmaster.backend.entity.AccountSummary;

/**
 * Примеры правильного использования ServiceManager для координации между сервисами
 * Демонстрирует соблюдение принципов SOLID
 */
public class ServiceManagerUsageExamples {
    private static final String TAG = "ServiceManagerExamples";

    /**
     * Пример 1: Получение общей суммы на счетах с конвертацией валют
     * Вместо прямого вызова AccountService.getTotalAmountByType()
     * используем координационный метод в ServiceManager
     */
    public void exampleGetTotalAmountWithCurrencyConversion(Context context) {
        ServiceManager sm = ServiceManager.getInstance(context, "user_name");
        
        // ❌ ПЛОХО - AccountService напрямую зависит от CurrencyService
        // AccountService accountService = sm.getAccountService();
        // long total = accountService.getTotalAmountByType(1, EntityFilter.ACTIVE);
        
        // ✅ ХОРОШО - ServiceManager координирует работу сервисов
        long currentAccountsTotal = sm.getTotalAmountByTypeWithCurrencyConversion(1, EntityFilter.ACTIVE);
        long savingsAccountsTotal = sm.getTotalAmountByTypeWithCurrencyConversion(2, EntityFilter.ACTIVE);
        long creditAccountsTotal = sm.getTotalAmountByTypeWithCurrencyConversion(3, EntityFilter.ACTIVE);
        
        Log.d(TAG, "Текущие счета: " + currentAccountsTotal);
        Log.d(TAG, "Сбережения: " + savingsAccountsTotal);
        Log.d(TAG, "Кредитные: " + creditAccountsTotal);
    }

    /**
     * Пример 2: Создание операции с автоматической конвертацией валют
     * ServiceManager координирует работу OperationService и CurrencyService
     */
    public void exampleCreateOperationWithCurrencyConversion(Context context) {
        ServiceManager sm = ServiceManager.getInstance(context, "user_name");
        
        // Создаем операцию в одной валюте
        // Operation operation = new Operation();
        // operation.setAmount(1000);
        // operation.setCurrencyId(1); // USD
        
        // ❌ ПЛОХО - каждый сервис сам решает как работать с другими
        // OperationService operationService = sm.getOperationService();
        // operationService.create(operation);
        // // Потом отдельно конвертируем валюту...
        
        // ✅ ХОРОШО - ServiceManager координирует всю логику
        // sm.createOperationWithCurrencyConversion(operation, 2); // Конвертируем в EUR
    }

    /**
     * Пример 3: Получение полной сводки по всем счетам
     * ServiceManager объединяет данные из всех сервисов
     */
    public void exampleGetCompleteAccountSummary(Context context) {
        ServiceManager sm = ServiceManager.getInstance(context, "user_name");
        
        // ❌ ПЛОХО - каждый сервис работает независимо
        // AccountService accountService = sm.getAccountService();
        // CurrencyService currencyService = sm.getCurrencyService();
        // // Много кода для координации...
        
        // ✅ ХОРОШО - ServiceManager предоставляет готовое решение
        AccountSummary summary = sm.getCompleteAccountSummary();
        
        Log.d(TAG, "Общая сумма: " + summary.getTotalAmount());
        Log.d(TAG, "Текущие счета: " + summary.getCurrentAccountsAmount());
        Log.d(TAG, "Сбережения: " + summary.getSavingsAccountsAmount());
        Log.d(TAG, "Кредитные: " + summary.getCreditAccountsAmount());
    }

    /**
     * Пример 4: Работа с отдельными сервисами (когда координация не нужна)
     * Для простых операций можно использовать сервисы напрямую
     */
    public void exampleSimpleServiceUsage(Context context) {
        ServiceManager sm = ServiceManager.getInstance(context, "user_name");
        
        // ✅ ХОРОШО - простые операции без координации
        // Получить все валюты
        sm.currencies.service().getAll();
        
        // Получить счет по ID
        sm.accounts.service().getById(1);
        
        // Создать новую категорию
        sm.categories.service().create("Продукты", 1);
    }

    /**
     * Пример 5: Соблюдение принципа единственной ответственности
     * Каждый сервис отвечает только за свою область
     */
    public void exampleSingleResponsibilityPrinciple(Context context) {
        ServiceManager sm = ServiceManager.getInstance(context, "user_name");
        
        // ✅ AccountService отвечает только за счета
        sm.accounts.service().getAll();
        sm.accounts.service().create("Новый счет", 1, 1000L, 1, 0);
        
        // ✅ CurrencyService отвечает только за валюты
        sm.currencies.service().getAll();
        sm.currencies.service().getExchangeRateById(1);
        
        // ✅ ServiceManager координирует сложные операции
        sm.getTotalAmountByTypeWithCurrencyConversion(1, EntityFilter.ACTIVE);
    }
}

/**
 * ПРЕИМУЩЕСТВА НОВОЙ АРХИТЕКТУРЫ:
 * 
 * 1. СОБЛЮДЕНИЕ SOLID ПРИНЦИПОВ:
 *    - Single Responsibility: каждый сервис отвечает только за свою область
 *    - Open/Closed: легко добавлять новые координационные методы
 *    - Liskov Substitution: сервисы можно заменять на реализации
 *    - Interface Segregation: четкое разделение ответственности
 *    - Dependency Inversion: ServiceManager управляет зависимостями
 * 
 * 2. УМЕНЬШЕНИЕ СВЯЗАННОСТИ:
 *    - AccountService не знает о CurrencyService
 *    - Сервисы не зависят друг от друга напрямую
 *    - Легко тестировать каждый сервис отдельно
 * 
 * 3. ЦЕНТРАЛИЗОВАННАЯ КООРДИНАЦИЯ:
 *    - ServiceManager управляет сложными операциями
 *    - Единая точка для бизнес-логики высокого уровня
 *    - Легко добавлять новые координационные сценарии
 * 
 * 4. УЛУЧШЕНИЕ ЧИТАЕМОСТИ:
 *    - Код становится более понятным
 *    - Четкое разделение ответственности
 *    - Легче поддерживать и расширять
 */
