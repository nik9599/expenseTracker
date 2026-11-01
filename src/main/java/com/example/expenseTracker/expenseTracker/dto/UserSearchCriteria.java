package com.example.expenseTracker.expenseTracker.dto;

import java.math.BigDecimal;
import java.sql.Date;

public class UserSearchCriteria {
    private String description;
    private Date transactionDate;
    private String refernceNumber;
    private BigDecimal withdrawalAmount;
    private BigDecimal depositAmount;
    private BigDecimal closingBalance;
    private String category;
    private BigDecimal userId;

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Date getTransactionDate() {
        return transactionDate;
    }
    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }
    public String getRefernceNumber() {
        return refernceNumber;
    }
    public void setRefernceNumber(String refernceNumber) {
        this.refernceNumber = refernceNumber;
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

    public void setUserId(BigDecimal userId){
        this.userId = userId;
    }

    public BigDecimal getUserId(){
        return this.userId;
    }

    public String viewAllData() {
    return "description=" + description
            + ", transactionDate=" + transactionDate
            + ", refernceNumber=" + refernceNumber
            + ", withdrawalAmount=" + withdrawalAmount
            + ", depositAmount=" + depositAmount
            + ", closingBalance=" + closingBalance
            + ", userId=" + userId
            + ", category=" + category;
}
    
}
