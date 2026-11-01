package com.example.expenseTracker.expenseTracker.modal;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "expense")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "user_id", nullable = false)
    private BigDecimal userId;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(name = "reference_number", unique = true)
    private String referenceNumber;

    @Column(name = "value_date")
    private LocalDate valueDate;

    // Use BigDecimal to handle exact currency values without floating-point math issues
    @Column(name = "withdrawal_amount", precision = 10, scale = 2)
    private BigDecimal withdrawalAmount;

    @Column(name = "deposit_amount", precision = 10, scale = 2)
    private BigDecimal depositAmount;

    @Column(name = "closing_balance", precision = 10, scale = 2)
    private BigDecimal closingBalance;

    @Column(nullable = false, length = 50)
    private String category = "UNASSIGNED";

    public Expense() {}

    public Expense(LocalDate transactionDate, String description, String referenceNumber, LocalDate valueDate, BigDecimal withdrawalAmount, BigDecimal depositAmount, BigDecimal closingBalance, String category, BigDecimal userId) {
        this.userId = userId;
        this.transactionDate = transactionDate;
        this.description = description;
        this.referenceNumber = referenceNumber;
        this.valueDate = valueDate;
        this.withdrawalAmount = withdrawalAmount;
        this.depositAmount = depositAmount;
        this.closingBalance = closingBalance;
        this.category = category != null ? category : "UNASSIGNED";
    }

    public BigDecimal getUserId(){
        return  userId;
    }

    public void setUserId(BigDecimal user_id){
        this.userId = user_id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public LocalDate getValueDate() {
        return valueDate;
    }

    public void setValueDate(LocalDate valueDate) {
        this.valueDate = valueDate;
    }

    public BigDecimal getWithdrawalAmount() {
        return withdrawalAmount;
    }

    public void setWithdrawalAmount(BigDecimal withdrawalAmount) {
        this.withdrawalAmount = withdrawalAmount;
    }

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }

    public BigDecimal getClosingBalance() {
        return closingBalance;
    }

    public void setClosingBalance(BigDecimal closingBalance) {
        this.closingBalance = closingBalance;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
