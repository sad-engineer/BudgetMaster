package com.sadengineer.budgetmaster.backend;

// -*- coding: utf-8 -*-
import com.sadengineer.budgetmaster.backend.model.*;
import com.sadengineer.budgetmaster.backend.repository.*;
import com.sadengineer.budgetmaster.backend.service.*;
import com.sadengineer.budgetmaster.backend.util.DatabaseUtil;
import com.sadengineer.budgetmaster.backend.database.jdbc.JdbcDatabaseFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

   public class Main {
       public static void main(String[] args) {
        // Выводим версию backend
        System.out.println("Backend version: " + BackendVersion.VERSION);

        // регистрируем JDBC-драйвер
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC driver not found!");
            e.printStackTrace();
            return;
        }

        // Инициализируем JDBC провайдер
        JdbcDatabaseFactory.initialize();

        String dbPath = "BudgetMasterDB.db";
        String user = "admin";
        try {
            // 1. Создание базы данных и таблиц
            DatabaseUtil.createDatabaseIfNotExists(dbPath);
            System.out.println("База данных и таблицы успешно созданы/проверены.");

            // 2. Создание репозиториев
            CurrencyRepository currencyRepo = new CurrencyRepository(dbPath);
            AccountRepository accountRepo = new AccountRepository(dbPath);
            CategoryRepository categoryRepo = new CategoryRepository(dbPath);
            BudgetRepository budgetRepo = new BudgetRepository(dbPath);
            OperationRepository operationRepo = new OperationRepository(dbPath);

            // 3. Создание сервисов
            CurrencyService currencyService = new CurrencyService(currencyRepo, user);
            AccountService accountService = new AccountService(accountRepo, user);
            CategoryService categoryService = new CategoryService(categoryRepo, user);
            BudgetService budgetService = new BudgetService(budgetRepo, user);
            OperationService operationService = new OperationService(operationRepo, accountService, currencyService, user);

            // 4. CRUD для Currency
            //Currency rub = new Currency(0, LocalDateTime.now(), null, null, null, null, null, 1, "Рубль");
            //currencyService.create(rub);
            //System.out.println("Добавлена валюта: " + rub);

            //List<Currency> allCurrencies = currencyService.getAll();
            //System.out.println("Все валюты: " + allCurrencies);

            //Optional<Currency> foundCurrency = currencyService.getById(1);
            //foundCurrency.ifPresent(c -> System.out.println("Найдена валюта: " + c));

            //rub.setTitle("Рубль (обновлено)");
            //currencyService.update(rub);
            //System.out.println("Валюта после обновления: " + currencyService.getById(1).get());

            // currencyService.deleteCurrency(1);
            // System.out.println("Валюта удалена");

            // 5. Аналогично можно делать CRUD для других сущностей через сервисы
            // ...

        } catch (SQLException e) {
            System.err.println("Ошибка при работе с базой данных: " + e.getMessage());
            e.printStackTrace();
        }
       }
   }