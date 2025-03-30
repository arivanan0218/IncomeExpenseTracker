package com.ruh.IncomeExpenseTracker.repository;

import com.ruh.IncomeExpenseTracker.model.Category;
import com.ruh.IncomeExpenseTracker.model.Transaction;
import com.ruh.IncomeExpenseTracker.model.TransactionType;
import com.ruh.IncomeExpenseTracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUser(User user);

    List<Transaction> findByUserAndType(User user, TransactionType type);

    List<Transaction> findByUserAndCategory(User user, Category category);

    List<Transaction> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user = ?1 AND t.type = ?2")
    Double sumByUserAndType(User user, TransactionType type);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user = ?1 AND t.type = ?2 AND t.date BETWEEN ?3 AND ?4")
    Double sumByUserAndTypeAndDateBetween(User user, TransactionType type, LocalDate startDate, LocalDate endDate);
}
