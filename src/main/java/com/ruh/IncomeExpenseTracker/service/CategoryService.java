package com.ruh.IncomeExpenseTracker.service;

import com.ruh.IncomeExpenseTracker.model.Category;
import com.ruh.IncomeExpenseTracker.model.TransactionType;
import com.ruh.IncomeExpenseTracker.model.User;
import com.ruh.IncomeExpenseTracker.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserService userService;

    public List<Category> getAllCategories() {
        try {
            User currentUser = userService.getCurrentUser();
            System.out.println("Getting all categories for user: " + currentUser.getUsername());
            return categoryRepository.findByUser(currentUser);
        } catch (Exception e) {
            System.err.println("Error getting all categories: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve categories. Please ensure you are authenticated.", e);
        }
    }

    public List<Category> getCategoriesByType(TransactionType type) {
        try {
            User currentUser = userService.getCurrentUser();
            System.out.println("Getting categories of type " + type + " for user: " + currentUser.getUsername());
            return categoryRepository.findByUserAndType(currentUser, type);
        } catch (Exception e) {
            System.err.println("Error getting categories by type: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve categories by type. Please ensure you are authenticated.", e);
        }
    }

    public Category getCategoryById(Long id) {
        try {
            User currentUser = userService.getCurrentUser();
            System.out.println("Getting category with ID " + id + " for user: " + currentUser.getUsername());

            return categoryRepository.findById(id)
                    .filter(category -> category.getUser().getId().equals(currentUser.getId()))
                    .orElseThrow(() -> {
                        System.err.println("Category with ID " + id + " not found or access denied");
                        return new RuntimeException("Category not found or you don't have access to it");
                    });
        } catch (Exception e) {
            System.err.println("Error getting category by ID: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve category. Please ensure you are authenticated.", e);
        }
    }

    public Category createCategory(Category category) {
        try {
            User currentUser = userService.getCurrentUser();
            System.out.println("Creating new category for user: " + currentUser.getUsername());
            category.setUser(currentUser);
            Category savedCategory = categoryRepository.save(category);
            System.out.println("Category created with ID: " + savedCategory.getId());
            return savedCategory;
        } catch (Exception e) {
            System.err.println("Error creating category: " + e.getMessage());
            throw new RuntimeException("Failed to create category. Please ensure you are authenticated.", e);
        }
    }

    public Category updateCategory(Long id, Category categoryDetails) {
        Category category = getCategoryById(id);

        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());
        category.setType(categoryDetails.getType());

        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        categoryRepository.delete(category);
    }
}
