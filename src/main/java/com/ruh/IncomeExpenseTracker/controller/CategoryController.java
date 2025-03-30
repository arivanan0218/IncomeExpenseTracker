package com.ruh.IncomeExpenseTracker.controller;

import com.ruh.IncomeExpenseTracker.model.Category;
import com.ruh.IncomeExpenseTracker.model.TransactionType;
import com.ruh.IncomeExpenseTracker.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/categories")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Categories", description = "Category management endpoints")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieves all categories belonging to the authenticated user")
    public ResponseEntity<?> getAllCategories() {
        try {
            System.out.println("Fetching all categories for authenticated user");
            List<Category> categories = categoryService.getAllCategories();
            System.out.println("Successfully retrieved " + categories.size() + " categories");
            return ResponseEntity.ok(categories);
        } catch (RuntimeException e) {
            System.err.println("Error fetching categories: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get categories by type", description = "Retrieves categories filtered by transaction type (INCOME or EXPENSE)")
    public ResponseEntity<?> getCategoriesByType(@PathVariable TransactionType type) {
        try {
            System.out.println("Fetching categories by type: " + type);
            List<Category> categories = categoryService.getCategoriesByType(type);
            System.out.println("Successfully retrieved " + categories.size() + " categories of type " + type);
            return ResponseEntity.ok(categories);
        } catch (RuntimeException e) {
            System.err.println("Error fetching categories by type: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieves a specific category by its ID")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PostMapping
    @Operation(summary = "Create a category", description = "Creates a new category for the authenticated user")
    public ResponseEntity<?> createCategory(@Valid @RequestBody Category category) {
        try {
            System.out.println("Creating new category: " + category.getName());
            Category createdCategory = categoryService.createCategory(category);
            System.out.println("Category created successfully with ID: " + createdCategory.getId());
            return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            System.err.println("Error creating category: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a category", description = "Updates an existing category")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @Valid @RequestBody Category category) {
        return ResponseEntity.ok(categoryService.updateCategory(id, category));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category", description = "Deletes a category by its ID")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }
}
