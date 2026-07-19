package com.example.expenseTracker.expenseTracker.repository;

import com.example.expenseTracker.expenseTracker.modal.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    boolean existsByReferenceNumber(String referenceNumber);

    List<Expense> findByCategory(String category);

    List<Expense> findByWithdrawalAmountIsNotNull();

    // 4. Retrieve only deposit transactions (money earned)
    List<Expense> findByDepositAmountIsNotNull();

    // 5. Calculate total expenses (Sum of all values in your Withdrawal column)
    @Query("SELECT COALESCE(SUM(e.withdrawalAmount), 0) FROM Expense e WHERE e.withdrawalAmount IS NOT NULL")
    BigDecimal getTotalExpenses();

    // 6. Calculate total income (Sum of all values in your Deposit column)
    @Query("SELECT COALESCE(SUM(e.depositAmount), 0) FROM Expense e WHERE e.depositAmount IS NOT NULL")
    BigDecimal getTotalIncome();

}
