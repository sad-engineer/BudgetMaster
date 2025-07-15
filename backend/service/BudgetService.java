package service;

import model.Budget;
import repository.BudgetRepository;
import validator.BudgetValidator;
import validator.BaseEntityValidator;
import validator.CommonValidator;
import constants.ServiceConstants;
import constants.ModelConstants;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

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
        this.budgetRepository = new BudgetRepository(ServiceConstants.DEFAULT_DATABASE_NAME);
        this.user = user;
    }

    /**
     * Удаляет бюджет по id
     * @param id id бюджета
     * @return true, если удаление успешно
     */
    public boolean delete(Integer id) {
        BaseEntityValidator.validatePositiveId(id, "ID бюджета");
        return budgetRepository.deleteById(id, user);
    }

    /**
     * Удаляет бюджет по id категории
     * @param categoryId id категории
     * @return true, если удаление успешно
     */
    public boolean deleteByCategoryId(Integer categoryId) {
        CommonValidator.validateCategoryId(categoryId);
        return budgetRepository.deleteByCategoryId(categoryId, user);
    }

    /**
     * Изменяет порядок бюджета с переупорядочиванием других бюджетов
     * @param budget бюджет для изменения позиции
     * @param newPosition новая позиция
     * @return бюджет с новой позицией
     */
    public Budget changePosition(Budget budget, int newPosition) {
        BaseEntityValidator.validate(budget);
        CommonValidator.validatePositivePosition(newPosition);
        int oldPosition = budget.getPosition();
        
        // Если позиция не изменилась, ничего не делаем
        if (oldPosition == newPosition) {
            return budget;
        }
        
        // Получаем все бюджеты для переупорядочивания
        List<Budget> allBudgets = getAll();
        
        // Проверяем, что новая позиция валидна
        if (newPosition < 1 || newPosition > allBudgets.size()) {
            throw new IllegalArgumentException(ServiceConstants.ERROR_POSITION_OUT_OF_RANGE + allBudgets.size());
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
     * @return бюджет с новой позицией. Если бюджет не найден, возвращает null
     */
    public Budget changePosition(int oldPosition, int newPosition) {
        CommonValidator.validatePositivePosition(oldPosition);
        Optional<Budget> budget = budgetRepository.findByPosition(oldPosition);
        if (budget.isPresent()) {
            return changePosition(budget.get(), newPosition);
        }
        return null;
    }

    /**
     * Создает новый бюджет без валидации (для внутреннего использования)
     * @param categoryId ID категории
     * @param amount сумма бюджета в копейках валюты
     * @param currencyId ID валюты
     * @return созданный бюджет
     */
    private Budget create(Integer categoryId, int amount, int currencyId) {
        Budget newBudget = new Budget();
        int nextPosition = budgetRepository.getMaxPosition() + 1;
        newBudget.setCategoryId(categoryId);
        newBudget.setAmount(amount);
        newBudget.setPosition(nextPosition);
        newBudget.setCreateTime(LocalDateTime.now());
        newBudget.setCreatedBy(user);
        newBudget.setCurrencyId(currencyId);

        // Валидация бюджета
        BudgetValidator.validateForCreate(newBudget);

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
     * Получает бюджет по ID категории. 
     * Если бюджет с таким ID категории существует, возвращает его.
     * Если бюджет с таким ID категории существует, но удален, восстанавливает его.
     * Если бюджет с таким ID категории не существует, вернет null.
     * @param categoryId ID категории
     * @return бюджет
     */
    public Budget getByCategoryId(Integer categoryId) {
        CommonValidator.validateCategoryId(categoryId);
        Optional<Budget> budget = budgetRepository.findByCategoryId(categoryId);
        if (budget.isPresent()) {
            Budget budgetObj = budget.get();
            if (budgetObj.isDeleted()) {
                return restore(budgetObj);
            }
            return budgetObj;
        }
        return null;
    }

    /**
     * Получает бюджет по ID категории с указанными параметрами.
     * Если бюджет с таким ID категории существует, и параметры совпадают с указанными, возвращает его.
     * Если бюджет с таким ID категории существует, но параметры отличные от указанных, обновляет его.
     * Если бюджет с таким ID категории существует, но удален, восстанавливает его.
     * Если бюджет с таким ID категории не существует, создает новый с указанными параметрами.
     * @param categoryId ID категории
     * @param amount сумма бюджета в копейках валюты
     * @param currencyId ID валюты
     * @return бюджет
     */
    public Budget get(Integer categoryId, int amount, int currencyId) {
        CommonValidator.validateCategoryId(categoryId);
        CommonValidator.validateBudgetAmount(amount);
        CommonValidator.validateCurrencyId(currencyId);

        Optional<Budget> budget = budgetRepository.findByCategoryId(categoryId);
        if (budget.isPresent()) {
            Budget budgetObj = budget.get();
            
            if (budgetObj.getCategoryId() == categoryId &&
                budgetObj.getAmount() == amount &&
                budgetObj.getCurrencyId() == currencyId) {
                
                return budgetObj;
            }

            return update(budgetObj, amount, currencyId);
        }

        return create(categoryId, amount, currencyId);
    }
    
    /**
     * Восстанавливает удаленный бюджет (для внутреннего использования)
     * @param restoredBudget бюджет для восстановления
     * @return восстановленный бюджет
     */
    private Budget restore(Budget restoredBudget) {
        restoredBudget.setDeletedBy(null);
        restoredBudget.setDeleteTime(null);
        restoredBudget.setUpdateTime(LocalDateTime.now());
        restoredBudget.setUpdatedBy(user);
        return updateInternal(restoredBudget);
    }    

    /**
     * Обновляет бюджет. 
     * 
     * @param updatedBudget бюджет для обновления
     * @param newAmount новое значение суммы бюджета (может быть null)
     * @param newCurrencyId новое значение ID валюты (может быть null)
     * @return обновленный бюджет
     */
    public Budget update(Budget updatedBudget, 
                         Integer newAmount,
                         Integer newCurrencyId) {
        BaseEntityValidator.validate(updatedBudget);
        
        if (newAmount != null) {
            CommonValidator.validateBudgetAmount(newAmount);
            updatedBudget.setAmount(newAmount);
        }
        
        if (newCurrencyId != null) {
            CommonValidator.validateCurrencyId(newCurrencyId);
            updatedBudget.setCurrencyId(newCurrencyId);
        }

        if (updatedBudget.isDeleted()) {
            return restore(updatedBudget);
        }
        
        // Проверяем, был ли задан хотя бы один параметр для обновления
        if (newAmount != null || newCurrencyId != null) {
            return updateInternal(updatedBudget);
        }
        
        // Если ни один параметр не задан, возвращаем null
        return null;
    }

    /**
     * Обновляет бюджет без новых параметров (для внутреннего использования)
     * @param updatedBudget бюджет для обновления
     * @return обновленный бюджет
     */
    private Budget updateInternal(Budget updatedBudget) {
        updatedBudget.setUpdateTime(LocalDateTime.now());
        updatedBudget.setUpdatedBy(user);
        
        return budgetRepository.update(updatedBudget);
    }    
} 