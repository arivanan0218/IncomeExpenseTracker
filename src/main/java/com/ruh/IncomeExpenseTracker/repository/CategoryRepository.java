package com.ruh.IncomeExpenseTracker.repository;

import com.ruh.IncomeExpenseTracker.model.Category;
import com.ruh.IncomeExpenseTracker.model.TransactionType;
import com.ruh.IncomeExpenseTracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUser(User user);

    List<Category> findByUserAndType(User user, TransactionType type);
}

