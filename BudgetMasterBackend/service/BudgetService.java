package service;

import model.Budget;
import repository.BudgetRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с бюджетами
 */
public class BudgetService {
    /**
     * Репозиторий для работы с бюджетами
     */
    private final BudgetRepository budgetRepository;
    
    /**
     * Пользователь, выполняющий операции
     */
    private final String user;
    
    /**
     * Конструктор для сервиса
     * @param budgetRepository репозиторий для работы с бюджетами
     * @param user пользователь, выполняющий операции
     */
    public BudgetService(BudgetRepository budgetRepository, String user) {
        this.budgetRepository = budgetRepository;
        this.user = user;
    }

    /**
     * Конструктор для сервиса с автоматическим созданием репозитория
     * @param user пользователь, выполняющий операции
     */
    public BudgetService(String user) {
        this.budgetRepository = new BudgetRepository("budget_master.db");
        this.user = user;
    }

    /**
     * Удаляет бюджет по id
     * @param id id бюджета
     * @return true, если удаление успешно
     */
    public boolean delete(int id) {
        return budgetRepository.deleteById(id, user);
    }

    /**
     * Удаляет бюджет по id категории
     * @param categoryId id категории
     * @return true, если удаление успешно
     */
    public boolean deleteByCategoryId(int categoryId) {
        return budgetRepository.deleteByCategoryId(categoryId, user);
    }

    /**
     * Изменяет порядок бюджета с переупорядочиванием других бюджетов
     * @param budget бюджет для изменения позиции
     * @param newPosition новая позиция
     * @return бюджет с новой позицией
     */
    public Budget changePosition(Budget budget, int newPosition) {
        int oldPosition = budget.getPosition();
        
        // Если позиция не изменилась, ничего не делаем
        if (oldPosition == newPosition) {
            return budget;
        }
        
        // Получаем все бюджеты для переупорядочивания
        List<Budget> allBudgets = getAll();
        
        // Проверяем, что новая позиция валидна
        if (newPosition < 1 || newPosition > allBudgets.size()) {
            throw new IllegalArgumentException("Новая позиция должна быть от 1 до " + allBudgets.size());
        }
        
        // Переупорядочиваем позиции
        if (oldPosition < newPosition) {
            // Двигаем бюджет вниз: сдвигаем бюджеты между старой и новой позицией вверх
            for (Budget b : allBudgets) {
                if (b.getId() != budget.getId() && 
                    b.getPosition() > oldPosition && 
                    b.getPosition() <= newPosition) {
                    b.setPosition(b.getPosition() - 1);
                    b.setUpdateTime(LocalDateTime.now());
                    b.setUpdatedBy(user);
                    budgetRepository.update(b);
                }
            }
        } else {
            // Двигаем бюджет вверх: сдвигаем бюджеты между новой и старой позицией вниз
            for (Budget b : allBudgets) {
                if (b.getId() != budget.getId() && 
                    b.getPosition() >= newPosition && 
                    b.getPosition() < oldPosition) {
                    b.setPosition(b.getPosition() + 1);
                    b.setUpdateTime(LocalDateTime.now());
                    b.setUpdatedBy(user);
                    budgetRepository.update(b);
                }
            }
        }
        
        // Устанавливаем новую позицию для целевого бюджета
        budget.setPosition(newPosition);
        budget.setUpdateTime(LocalDateTime.now());    
        budget.setUpdatedBy(user);
        return budgetRepository.update(budget);
    }

    /**
     * Изменяет порядок бюджета с переупорядочиванием других бюджетов
     * @param oldPosition старая позиция
     * @param newPosition новая позиция
     * @return бюджет с новой позицией
     */
    public Budget changePosition(int oldPosition, int newPosition) {
        // поиск по позиции  
        Optional<Budget> budgetOpt = budgetRepository.findByPosition(oldPosition);
        if (budgetOpt.isPresent()) {
            return changePosition(budgetOpt.get(), newPosition);
        }
        return null;
    }

    /**
     * Создает новый бюджет
     * @param categoryId ID категории
     * @param amount сумма бюджета в копейках валюты
     * @param currencyId ID валюты
     * @return созданный бюджет
     */
    public Budget create(Integer categoryId, int amount, int currencyId) {
        Budget newBudget = new Budget();
        int nextPosition = budgetRepository.getMaxPosition() + 1;
        newBudget.setCategoryId(categoryId);
        newBudget.setAmount(amount);
        newBudget.setPosition(nextPosition);
        newBudget.setCreateTime(LocalDateTime.now());
        newBudget.setCreatedBy(user);
        newBudget.setUpdateTime(LocalDateTime.now());
        newBudget.setUpdatedBy(user);
        newBudget.setCurrencyId(currencyId);
        return budgetRepository.save(newBudget);
    }

    /**
     * Получает все бюджеты
     * @return список бюджетов
     */
    public List<Budget> getAll() {
        return budgetRepository.findAll();
    }

    /**
     * Получает все бюджеты по ID валюты
     * @param currencyId ID валюты
     * @return список бюджетов
     */
    public List<Budget> getAllByCurrencyId(int currencyId) {
        return budgetRepository.findAllByCurrencyId(currencyId);
    }

    /**
     * Получает бюджет по ID
     * @param id ID бюджета
     * @return бюджет
     */
    public Optional<Budget> getById(int id) { 
        return budgetRepository.findById(id);
    }

    /**
     * Получает бюджет по ID категории
     * @param categoryId ID категории
     * @return бюджет
     */
    public Optional<Budget> getByCategoryId(int categoryId) {
        return budgetRepository.findByCategoryId(categoryId);
    }

    /**
     * Проверка бюджета на удаление
     * @param budget класс бюджета
     * @return true, если бюджет удален
     */
    public boolean isBudgetDeleted(Budget budget) {
        return budget.getDeleteTime() != null;
    }

    /**
     * Восстанавливает бюджет
     * @param restoredBudget бюджет
     * @return бюджет
     */
    public Budget restore(Budget restoredBudget) {
        restoredBudget.setDeleteTime(null);
        restoredBudget.setDeletedBy(null);
        restoredBudget.setUpdateTime(LocalDateTime.now());
        restoredBudget.setUpdatedBy(user);
        return budgetRepository.update(restoredBudget);
    }

    /**
     * Восстанавливает бюджет по id
     * @param id id бюджета
     * @return бюджет или null, если бюджет не найден
     */
    public Budget restore(int id) {
        Optional<Budget> budgetOpt = getById(id);
        if (budgetOpt.isPresent()) {
            return restore(budgetOpt.get());
        }
        return null;
    }

    /**
     * Устанавливает сумму бюджета
     * @param id id бюджета
     * @param amount сумма бюджета в копейках валюты    
     * @return бюджет или null, если бюджет не найден
     */
    public Budget setAmount(int id, int amount) {
        Optional<Budget> budgetOpt = getById(id);   
        if (budgetOpt.isPresent()) {
            Budget budget = budgetOpt.get();
            budget.setAmount(amount);
            budget.setUpdateTime(LocalDateTime.now());
            budget.setUpdatedBy(user);
            return budgetRepository.update(budget);
        }
        return null;
    }

    /**
     * Устанавливает нового пользователя для операций
     * @param newUser новый пользователь
     */
    public void setUser(String newUser) {
        // Обратите внимание: поле user final, поэтому нужно создать новый экземпляр сервиса
        // или использовать другой подход для смены пользователя
        throw new UnsupportedOperationException("Для смены пользователя создайте новый экземпляр BudgetService");
    }
} 