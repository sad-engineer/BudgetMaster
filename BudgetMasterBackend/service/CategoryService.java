package service;

import model.Category;
import repository.CategoryRepository;

import java.util.List;
import java.util.Optional;

public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category createCategory(Category category) {
        // Бизнес-валидация перед сохранением
        return categoryRepository.save(category);
    }

    public Optional<Category> getCategoryById(int id) {
        return categoryRepository.findById(id);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category updateCategory(Category category) {
        // Бизнес-валидация перед обновлением
        return categoryRepository.update(category);
    }

    public boolean deleteCategory(int id) {
        return categoryRepository.delete(id);
    }
} 