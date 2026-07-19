package com.example.expenseTracker.expenseTracker.utils;

import com.example.expenseTracker.expenseTracker.modal.Expense;
import com.example.expenseTracker.expenseTracker.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class BankRegex {

    @Autowired
    ExpenseRepository expenseRepository;

    public static final Pattern TRANSACTION_PATTERN = Pattern.compile(
            "^(\\d{2}/\\d{2}/\\d{2})\\s+(.+?)\\s+([A-Za-z0-9]{12,16})\\s+(\\d{2}/\\d{2}/\\d{2})\\s+([\\d,]+\\.\\d{2})\\s+([\\d,]+\\.\\d{2})$"
    );

    public void saveIfUnique(Expense expense, List<Expense> list) {
        if (!expenseRepository.existsByReferenceNumber(expense.getReferenceNumber())) {
            list.add(expense);
        }
    }

}