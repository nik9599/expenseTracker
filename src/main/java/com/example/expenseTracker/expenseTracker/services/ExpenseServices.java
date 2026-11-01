package com.example.expenseTracker.expenseTracker.services;

import com.example.expenseTracker.expenseTracker.bankParser.HDFCBankStatementParser;
import com.example.expenseTracker.expenseTracker.dto.ApiResponse;
import com.example.expenseTracker.expenseTracker.modal.Expense;
import com.example.expenseTracker.expenseTracker.repository.ExpenseRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.example.expenseTracker.expenseTracker.dto.UserSearchCriteria;

import java.math.BigDecimal;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class ExpenseServices {

    @Autowired
    HDFCBankStatementParser hdfcParser;

    @Autowired 
    ExpenseRepository expenseDb;

    private static final Pattern BANK_PATTERN = Pattern.compile(
            "\\b(HDFC\\s*BANK|ICICI\\s*BANK|STATE\\s*BANK\\s*OF\\s*INDIA|SBI|AXIS\\s*BANK|KOTAK\\s*MAHINDRA\\s*BANK)\\b",
            Pattern.CASE_INSENSITIVE
    );

    public ApiResponse<Void> uploadStatement(MultipartFile file, BigDecimal userId) {
        return switch (detectBankName(file)) {
            case "HDFC BANK" -> hdfcParser.parsePdfFile(file, userId);
            default -> ApiResponse.error("Invalid file");
        };
    }

    public String detectBankName (MultipartFile file){
        try (
            InputStream is = file.getInputStream();
            PDDocument document = Loader.loadPDF(is.readAllBytes())){
            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.setSortByPosition(true);
            String rawText = pdfStripper.getText(document);
            String[] lines = rawText.split("\\r?\\n");
            for (int i = 0; i < Math.min(lines.length, 15); i++) {
                Matcher matcher = BANK_PATTERN.matcher(lines[i]);

                if (matcher.find()) {
                    return matcher.group();
                }
            }
            return "UNKNOWN";
        }catch(Exception e){
            e.printStackTrace();
            return "UNKNOWN";
        }

    }

    public String addStatement (UserSearchCriteria input){
        try {
                Expense expense = new Expense(
                input.getTransactionDate().toLocalDate(),
                input.getDescription(),
                input.getRefernceNumber(),
                null,
                BigDecimal.valueOf(input.getWithdrawalAmount()),
                BigDecimal.valueOf(input.getDepositAmount()),
                BigDecimal.valueOf(input.getClosingBalance()),
                input.getCategory(),
                input.getUserId()
        );
        Expense savedExpense = expenseDb.save(expense);
        return "Saved successfully with id " + savedExpense.getId();
        } catch (Exception e) {
          System.out.print(e);
          return "Error while creating db entry";
        }
    
    }

    public Optional<Expense> updateStatement(Long id, UserSearchCriteria input) {
        return expenseDb.findById(id).map(expense -> {
            expense.setUserId(input.getUserId());
            expense.setTransactionDate(input.getTransactionDate().toLocalDate());
            expense.setDescription(input.getDescription());
            expense.setReferenceNumber(input.getRefernceNumber());
            expense.setWithdrawalAmount(BigDecimal.valueOf(input.getWithdrawalAmount()));
            expense.setDepositAmount(BigDecimal.valueOf(input.getDepositAmount()));
            expense.setClosingBalance(BigDecimal.valueOf(input.getClosingBalance()));
            expense.setCategory(input.getCategory());
            return expenseDb.save(expense);
        });
    }
 
    public boolean deleteEntity(Long id) {
        Optional<Expense> expense = expenseDb.findById(id);
        if (expense.isEmpty()) {
            return false;
        }

        expenseDb.delete(expense.get());
        return true;
    }

    public List<Expense> fetchAllTheEntry(BigDecimal userId){
        return expenseDb.findAllByUserId(userId);
    }
}
