package service;

import model.Budget;
import repository.BudgetRepository;

import java.util.List;
import java.util.Optional;

public class BudgetService {
    private final BudgetRepository budgetRepository;

    public BudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    public Budget createBudget(Budget budget) {
        // Бизнес-валидация перед сохранением
        return budgetRepository.save(budget);
    }

    public Optional<Budget> getBudgetById(int id) {
        return budgetRepository.findById(id);
    }

    public List<Budget> getAllBudgets() {
        return budgetRepository.findAll();
    }

    public Budget updateBudget(Budget budget) {
        // Бизнес-валидация перед обновлением
        return budgetRepository.update(budget);
    }

    public boolean deleteBudget(int id) {
        return budgetRepository.delete(id);
    }
} 