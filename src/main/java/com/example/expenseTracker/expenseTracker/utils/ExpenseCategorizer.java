package com.example.expenseTracker.expenseTracker.utils;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class ExpenseCategorizer {

    private static final Map<ExpenseCategory, Pattern> PATTERNS = new LinkedHashMap<>();

    static {
        PATTERNS.put(ExpenseCategory.MEDICINE, Pattern.compile(
                "(?i)(pharmacy|medplus|apollo|netmeds|1mg|pharmeasy|medical|chemist|hospital|clinic|diagnostic)"));

        PATTERNS.put(ExpenseCategory.FOOD, Pattern.compile(
                "(?i)(swiggy|zomato|restaurant|cafe|dominos|pizza|mcdonald|kfc|burger|eatsure|starbucks)"));

        PATTERNS.put(ExpenseCategory.GROCERY, Pattern.compile(
                "(?i)(bigbasket|blinkit|zepto|dmart|grofers|instamart|grocery|reliance ?fresh)"));

        PATTERNS.put(ExpenseCategory.FUEL, Pattern.compile(
                "(?i)(petrol|diesel|fuel|hp ?petrol|indian ?oil|bharat ?petroleum|shell)"));

        PATTERNS.put(ExpenseCategory.SHOPPING, Pattern.compile(
                "(?i)(amazon|flipkart|myntra|ajio|meesho)"));

        PATTERNS.put(ExpenseCategory.ENTERTAINMENT, Pattern.compile(
                "(?i)(netflix|prime ?video|hotstar|spotify|bookmyshow|pvr|inox)"));

        PATTERNS.put(ExpenseCategory.UTILITY_BILLS, Pattern.compile(
                "(?i)(electricity|water ?bill|broadband|airtel|jio|vi ?recharge|gas ?bill|dth)"));

        PATTERNS.put(ExpenseCategory.RENT, Pattern.compile("(?i)(rent|landlord|housing ?society)"));
        PATTERNS.put(ExpenseCategory.SALARY, Pattern.compile("(?i)(salary|payroll)"));
        PATTERNS.put(ExpenseCategory.TRANSFER, Pattern.compile("(?i)(neft|imps|upi|rtgs)"));
    }

    public ExpenseCategory categorize(String description) {
        if (description == null || description.isBlank()) return ExpenseCategory.UNKNOWN;
        for (var entry : PATTERNS.entrySet()) {
            if (entry.getValue().matcher(description).find()) return entry.getKey();
        }
        return ExpenseCategory.UNKNOWN;
    }
}