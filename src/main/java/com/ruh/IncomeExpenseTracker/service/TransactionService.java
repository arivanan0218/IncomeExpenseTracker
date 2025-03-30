package com.ruh.IncomeExpenseTracker.service;

import com.ruh.IncomeExpenseTracker.model.Category;
import com.ruh.IncomeExpenseTracker.model.Transaction;
import com.ruh.IncomeExpenseTracker.model.TransactionType;
import com.ruh.IncomeExpenseTracker.model.User;
import com.ruh.IncomeExpenseTracker.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    public List<Transaction> getAllTransactions() {
        try {
            User currentUser = userService.getCurrentUser();
            System.out.println("Fetching all transactions for user: " + currentUser.getUsername());
            List<Transaction> transactions = transactionRepository.findByUser(currentUser);
            System.out.println("Retrieved " + transactions.size() + " transactions");
            return transactions;
        } catch (Exception e) {
            System.err.println("Error retrieving all transactions: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve transactions. Authentication error.", e);
        }
    }

    public List<Transaction> getTransactionsByType(TransactionType type) {
        User currentUser = userService.getCurrentUser();
        return transactionRepository.findByUserAndType(currentUser, type);
    }

    public List<Transaction> getTransactionsByCategory(Long categoryId) {
        User currentUser = userService.getCurrentUser();
        Category category = categoryService.getCategoryById(categoryId);
        return transactionRepository.findByUserAndCategory(currentUser, category);
    }

    public List<Transaction> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        User currentUser = userService.getCurrentUser();
        return transactionRepository.findByUserAndDateBetween(currentUser, startDate, endDate);
    }

    public Transaction getTransactionById(Long id) {
        try {
            User currentUser = userService.getCurrentUser();
            System.out.println("Fetching transaction with ID: " + id + " for user: " + currentUser.getUsername());

            return transactionRepository.findById(id)
                    .filter(transaction -> {
                        boolean hasAccess = transaction.getUser().getId().equals(currentUser.getId());
                        if (!hasAccess) {
                            System.err.println("Access denied to transaction ID " + id + " for user " + currentUser.getUsername());
                        }
                        return hasAccess;
                    })
                    .orElseThrow(() -> {
                        System.err.println("Transaction with ID " + id + " not found or access denied");
                        return new RuntimeException("Transaction not found or you don't have access to it");
                    });
        } catch (Exception e) {
            System.err.println("Error retrieving transaction by ID: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve transaction: " + e.getMessage(), e);
        }
    }

    public Transaction createTransaction(Transaction transaction) {
        try {
            System.out.println("Creating new transaction: " + transaction.getDescription());
            User currentUser = userService.getCurrentUser();
            System.out.println("Retrieved current user: " + currentUser.getUsername());

            if (transaction.getCategory() == null || transaction.getCategory().getId() == null) {
                System.err.println("Transaction creation failed: Category is null or has null ID");
                throw new RuntimeException("Category information is required for creating a transaction");
            }

            Category category = categoryService.getCategoryById(transaction.getCategory().getId());
            System.out.println("Retrieved category: " + category.getName());

            transaction.setUser(currentUser);
            transaction.setCategory(category);

            Transaction savedTransaction = transactionRepository.save(transaction);
            System.out.println("Transaction created successfully with ID: " + savedTransaction.getId());
            return savedTransaction;
        } catch (Exception e) {
            System.err.println("Error creating transaction: " + e.getMessage());
            throw new RuntimeException("Failed to create transaction: " + e.getMessage(), e);
        }
    }

    public Transaction updateTransaction(Long id, Transaction transactionDetails) {
        Transaction transaction = getTransactionById(id);
        Category category = categoryService.getCategoryById(transactionDetails.getCategory().getId());

        transaction.setDescription(transactionDetails.getDescription());
        transaction.setAmount(transactionDetails.getAmount());
        transaction.setDate(transactionDetails.getDate());
        transaction.setType(transactionDetails.getType());
        transaction.setCategory(category);

        return transactionRepository.save(transaction);
    }

    public void deleteTransaction(Long id) {
        Transaction transaction = getTransactionById(id);
        transactionRepository.delete(transaction);
    }

    public Double getTotalByType(TransactionType type) {
        User currentUser = userService.getCurrentUser();
        Double sum = transactionRepository.sumByUserAndType(currentUser, type);
        return sum != null ? sum : 0.0;
    }

    public Double getTotalByTypeAndDateRange(TransactionType type, LocalDate startDate, LocalDate endDate) {
        User currentUser = userService.getCurrentUser();
        Double sum = transactionRepository.sumByUserAndTypeAndDateBetween(currentUser, type, startDate, endDate);
        return sum != null ? sum : 0.0;
    }

    public Map<String, Double> getCategorySummary(TransactionType type) {
        User currentUser = userService.getCurrentUser();
        List<Transaction> transactions = transactionRepository.findByUserAndType(currentUser, type);

        return transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getName(),
                        Collectors.summingDouble(t -> t.getAmount().doubleValue())
                ));
    }
}
