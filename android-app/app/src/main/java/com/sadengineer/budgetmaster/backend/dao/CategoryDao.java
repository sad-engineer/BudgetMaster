package com.sadengineer.budgetmaster.backend.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.sadengineer.budgetmaster.backend.entity.Category;

import java.util.List;

/**
 * Data Access Object для работы с Category Entity
 */
@Dao
public interface CategoryDao {
    
    @Query("SELECT * FROM categories WHERE deleteTime IS NULL ORDER BY name ASC")
    List<Category> getAllActiveCategories();
    
    @Query("SELECT * FROM categories WHERE type = :type AND deleteTime IS NULL ORDER BY name ASC")
    List<Category> getCategoriesByType(String type);
    
    @Query("SELECT * FROM categories WHERE parentId IS NULL AND deleteTime IS NULL ORDER BY name ASC")
    List<Category> getRootCategories();
    
    @Query("SELECT * FROM categories WHERE parentId = :parentId AND deleteTime IS NULL ORDER BY name ASC")
    List<Category> getSubCategories(int parentId);
    
    @Query("SELECT * FROM categories WHERE id = :id AND deleteTime IS NULL")
    Category getCategoryById(int id);
    
    @Query("SELECT * FROM categories WHERE isDefault = 1 AND type = :type AND deleteTime IS NULL")
    List<Category> getDefaultCategoriesByType(String type);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertCategory(Category category);
    
    @Update
    void updateCategory(Category category);
    
    @Delete
    void deleteCategory(Category category);
    
    @Query("UPDATE categories SET deleteTime = :deleteTime, deletedBy = :deletedBy WHERE id = :id")
    void softDeleteCategory(int id, String deleteTime, String deletedBy);
    
    @Query("SELECT COUNT(*) FROM categories WHERE deleteTime IS NULL")
    int getActiveCategoriesCount();
    
    @Query("SELECT COUNT(*) FROM categories WHERE type = :type AND deleteTime IS NULL")
    int getCategoriesCountByType(String type);
    
    @Query("SELECT * FROM categories WHERE deleteTime IS NOT NULL")
    List<Category> getAllDeletedCategories();
    
    @Query("SELECT * FROM categories WHERE name = :name AND deleteTime IS NULL")
    Category getCategoryByName(String name);
    
    @Query("SELECT * FROM categories WHERE position = :position AND deleteTime IS NULL")
    Category getCategoryByPosition(int position);
    
    @Query("SELECT MAX(position) FROM categories WHERE deleteTime IS NULL")
    Integer getMaxPosition();
    
    @Query("UPDATE categories SET deleteTime = NULL, deletedBy = NULL, updateTime = :updateTime, updatedBy = :updatedBy WHERE id = :id")
    void restoreCategory(int id, String updateTime, String updatedBy);
    
    @Query("UPDATE categories SET deleteTime = :deleteTime, deletedBy = :deletedBy WHERE name = :name")
    void softDeleteCategoryByName(String name, String deleteTime, String deletedBy);
    
    @Query("SELECT * FROM categories WHERE parentId = :parentId AND deleteTime IS NULL ORDER BY position ASC")
    List<Category> getCategoriesByParent(int parentId);
    
    @Query("SELECT * FROM categories WHERE deleteTime IS NULL ORDER BY position ASC")
    List<Category> getCategoryHierarchy();
} 