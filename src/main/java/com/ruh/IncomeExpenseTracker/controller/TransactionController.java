package com.ruh.IncomeExpenseTracker.controller;

import com.ruh.IncomeExpenseTracker.model.Transaction;
import com.ruh.IncomeExpenseTracker.model.TransactionType;
import com.ruh.IncomeExpenseTracker.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/transactions")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Transactions", description = "Transaction management endpoints")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping
    @Operation(summary = "Get all transactions", description = "Retrieves all transactions belonging to the authenticated user")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get transactions by type", description = "Retrieves transactions filtered by transaction type (INCOME or EXPENSE)")
    public ResponseEntity<List<Transaction>> getTransactionsByType(@PathVariable TransactionType type) {
        return ResponseEntity.ok(transactionService.getTransactionsByType(type));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get transactions by category", description = "Retrieves transactions filtered by category ID")
    public ResponseEntity<List<Transaction>> getTransactionsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(transactionService.getTransactionsByCategory(categoryId));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get transactions by date range", description = "Retrieves transactions within a specified date range")
    public ResponseEntity<List<Transaction>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(transactionService.getTransactionsByDateRange(startDate, endDate));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID", description = "Retrieves a specific transaction by its ID")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    @PostMapping
    @Operation(summary = "Create a transaction", description = "Creates a new transaction for the authenticated user")
    public ResponseEntity<?> createTransaction(@Valid @RequestBody Transaction transaction) {
        try {
            System.out.println("Creating transaction: " + transaction);
            Transaction createdTransaction = transactionService.createTransaction(transaction);
            System.out.println("Transaction created successfully with ID: " + createdTransaction.getId());
            return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            System.err.println("Error creating transaction: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a transaction", description = "Updates an existing transaction")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable Long id, @Valid @RequestBody Transaction transaction) {
        return ResponseEntity.ok(transactionService.updateTransaction(id, transaction));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a transaction", description = "Deletes a transaction by its ID")
    public ResponseEntity<?> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/summary/total/{type}")
    @Operation(summary = "Get total by type", description = "Returns the total amount by transaction type")
    public ResponseEntity<Double> getTotalByType(@PathVariable TransactionType type) {
        return ResponseEntity.ok(transactionService.getTotalByType(type));
    }

    @GetMapping("/summary/date-range/{type}")
    @Operation(summary = "Get total by type and date range", description = "Returns the total amount by transaction type and date range")
    public ResponseEntity<Double> getTotalByTypeAndDateRange(
            @PathVariable TransactionType type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(transactionService.getTotalByTypeAndDateRange(type, startDate, endDate));
    }

    @GetMapping("/summary/category/{type}")
    @Operation(summary = "Get category summary", description = "Returns a summary of transactions by category")
    public ResponseEntity<Map<String, Double>> getCategorySummary(@PathVariable TransactionType type) {
        return ResponseEntity.ok(transactionService.getCategorySummary(type));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get transaction summary", description = "Returns a summary of all transactions with total income, expense, and balance")
    public ResponseEntity<Map<String, BigDecimal>> getTransactionSummary() {
        Map<String, BigDecimal> summary = new java.util.HashMap<>();
        Double incomeTotal = transactionService.getTotalByType(TransactionType.INCOME);
        Double expenseTotal = transactionService.getTotalByType(TransactionType.EXPENSE);

        // Convert to BigDecimal for precision
        BigDecimal totalIncome = incomeTotal != null ? BigDecimal.valueOf(incomeTotal) : BigDecimal.ZERO;
        BigDecimal totalExpense = expenseTotal != null ? BigDecimal.valueOf(expenseTotal) : BigDecimal.ZERO;
        BigDecimal balance = totalIncome.subtract(totalExpense);

        summary.put("totalIncome", totalIncome);
        summary.put("totalExpense", totalExpense);
        summary.put("balance", balance);

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/monthly-summary")
    @Operation(summary = "Get monthly transaction summary", description = "Returns a monthly summary of transactions with income and expense for each month")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyTransactions() {
        // Get transactions for the last 6 months
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(5).withDayOfMonth(1); // 6 months including current

        List<Transaction> transactions = transactionService.getTransactionsByDateRange(startDate, endDate);

        // Group transactions by month
        Map<String, Map<String, Object>> monthlySummary = new java.util.HashMap<>();

        for (Transaction transaction : transactions) {
            String month = transaction.getDate().getMonth().toString() + " " + transaction.getDate().getYear();

            monthlySummary.putIfAbsent(month, new java.util.HashMap<>());
            Map<String, Object> monthData = monthlySummary.get(month);

            monthData.putIfAbsent("month", month);
            monthData.putIfAbsent("totalIncome", BigDecimal.ZERO);
            monthData.putIfAbsent("totalExpense", BigDecimal.ZERO);

            if (transaction.getType() == TransactionType.INCOME) {
                BigDecimal currentIncome = (BigDecimal) monthData.get("totalIncome");
                monthData.put("totalIncome", currentIncome.add(transaction.getAmount()));
            } else {
                BigDecimal currentExpense = (BigDecimal) monthData.get("totalExpense");
                monthData.put("totalExpense", currentExpense.add(transaction.getAmount()));
            }
        }

        // Convert to list and sort by date
        List<Map<String, Object>> result = new java.util.ArrayList<>(monthlySummary.values());
        return ResponseEntity.ok(result);
    }
}
