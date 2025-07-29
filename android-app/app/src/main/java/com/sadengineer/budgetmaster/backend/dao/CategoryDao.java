// -*- coding: utf-8 -*-
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
    
    @Query("SELECT * FROM categories WHERE deleteTime IS NULL ORDER BY title ASC")
    List<Category> getAllActiveCategories();
    
    @Query("SELECT * FROM categories WHERE deleteTime IS NULL ORDER BY title ASC")
    List<Category> getAllCategories();
    
    @Query("SELECT * FROM categories WHERE operationType = :operationType AND deleteTime IS NULL ORDER BY title ASC")
    List<Category> getCategoriesByType(int operationType);
    
    @Query("SELECT * FROM categories WHERE parentId IS NULL AND deleteTime IS NULL ORDER BY title ASC")
    List<Category> getRootCategories();
    
    @Query("SELECT * FROM categories WHERE parentId = :parentId AND deleteTime IS NULL ORDER BY title ASC")
    List<Category> getSubCategories(int parentId);
    
    @Query("SELECT * FROM categories WHERE id = :id AND deleteTime IS NULL")
    Category getCategoryById(int id);
    

    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertCategory(Category category);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Category category);
    
    @Update
    void updateCategory(Category category);
    
    @Delete
    void deleteCategory(Category category);
    
    @Query("UPDATE categories SET deleteTime = :deleteTime, deletedBy = :deletedBy WHERE id = :id")
    void softDeleteCategory(int id, String deleteTime, String deletedBy);
    
    @Query("SELECT COUNT(*) FROM categories WHERE deleteTime IS NULL")
    int getActiveCategoriesCount();
    
    @Query("SELECT COUNT(*) FROM categories WHERE operationType = :operationType AND deleteTime IS NULL")
    int getCategoriesCountByType(int operationType);
    
    @Query("SELECT * FROM categories WHERE deleteTime IS NOT NULL")
    List<Category> getAllDeletedCategories();
    
    @Query("SELECT * FROM categories WHERE title = :name AND deleteTime IS NULL")
    Category getCategoryByName(String name);
    
    @Query("SELECT * FROM categories WHERE title = :title AND deleteTime IS NULL")
    Category getCategoryByTitle(String title);
    
    @Query("SELECT * FROM categories WHERE position = :position AND deleteTime IS NULL")
    Category getCategoryByPosition(int position);
    
    @Query("SELECT MAX(position) FROM categories WHERE deleteTime IS NULL")
    Integer getMaxPosition();
    
    @Query("UPDATE categories SET deleteTime = NULL, deletedBy = NULL, updateTime = :updateTime, updatedBy = :updatedBy WHERE id = :id")
    void restoreCategory(int id, String updateTime, String updatedBy);
    
    @Query("UPDATE categories SET deleteTime = :deleteTime, deletedBy = :deletedBy WHERE title = :name")
    void softDeleteCategoryByName(String name, String deleteTime, String deletedBy);
    
    @Query("SELECT * FROM categories WHERE parentId = :parentId AND deleteTime IS NULL ORDER BY position ASC")
    List<Category> getCategoriesByParent(int parentId);
    
    @Query("SELECT * FROM categories WHERE deleteTime IS NULL ORDER BY position ASC")
    List<Category> getCategoryHierarchy();
    
    @Query("DELETE FROM categories")
    void deleteAll();
} 