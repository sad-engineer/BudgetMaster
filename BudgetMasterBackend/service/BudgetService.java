package service;

import model.Budget;
import repository.BudgetRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class BudgetService {
    private final BudgetRepository budgetRepository;

    public BudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    public Budget createBudget(Budget budget) {
        // Проверяем, есть ли удаленная запись с таким же category_id, и восстанавливаем её
        if (budget.getCategoryId() != null) {
            Optional<Integer> deletedId = budgetRepository.findDeletedByCategoryId(budget.getCategoryId());
            if (deletedId.isPresent()) {
                // Восстанавливаем удаленную запись
                budgetRepository.restore(deletedId.get());
                
                // Получаем восстановленную запись
                Optional<Budget> restoredBudget = budgetRepository.findById(deletedId.get());
                if (restoredBudget.isPresent()) {
                    Budget restored = restoredBudget.get();
                    
                    // Обновляем данные восстановленной записи
                    restored.setUpdateTime(LocalDateTime.now());
                    restored.setUpdatedBy(budget.getUpdatedBy());
                    
                    // Обновляем запись в БД
                    budgetRepository.update(restored);
                    
                    // Устанавливаем ID восстановленной записи
                    budget.setId(restored.getId());
                    budget.setPosition(restored.getPosition());
                    
                    return budget;
                }
            }
        }
        
        // Удаленной записи не найдено, продолжаем обычное сохранение
        // Автоматически устанавливаем позицию, если она не установлена (равна 0)
        if (budget.getPosition() == 0) {
            budget.setPosition(budgetRepository.getNextPosition());
        }
        
        // Сохраняем новую запись
        Budget savedBudget = budgetRepository.save(budget);
        
        // Нормализуем позиции после сохранения
        budgetRepository.normalizePositions();
        
        return savedBudget;
    }

    public Optional<Budget> getBudgetById(int id) {
        return budgetRepository.findById(id);
    }

    public List<Budget> getAllBudgets() {
        return budgetRepository.findAll();
    }

    public Budget updateBudget(Budget budget) {
        // Корректируем позиции, если это необходимо перед обновлением
        budgetRepository.adjustPositionsForUpdate(budget);
        
        // Обновляем запись
        Budget updatedBudget = budgetRepository.update(budget);
        
        // Нормализуем позиции после обновления
        budgetRepository.normalizePositions();
        
        return updatedBudget;
    }

    public boolean deleteBudget(int id) {
        return budgetRepository.delete(id);
    }

    public boolean deleteBudget(int id, String deletedBy) {
        return budgetRepository.delete(id, deletedBy);
    }

    public boolean restoreBudget(int id) {
        boolean restored = budgetRepository.restore(id);
        if (restored) {
            // Нормализуем позиции после восстановления
            budgetRepository.normalizePositions();
        }
        return restored;
    }

    public List<Budget> getDeletedBudgets() {
        return budgetRepository.findDeleted();
    }

    /**
     * Нормализует позиции всех активных бюджетов
     */
    public void normalizePositions() {
        budgetRepository.normalizePositions();
    }
} 