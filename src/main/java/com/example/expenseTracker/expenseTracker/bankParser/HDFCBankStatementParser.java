package com.example.expenseTracker.expenseTracker.bankParser;

import com.example.expenseTracker.expenseTracker.utils.BankRegex;
import com.example.expenseTracker.expenseTracker.utils.ExpenseCategorizer;
import com.example.expenseTracker.expenseTracker.dto.ApiResponse;
import com.example.expenseTracker.expenseTracker.modal.Expense;
import com.example.expenseTracker.expenseTracker.repository.ExpenseRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.expenseTracker.expenseTracker.utils.BankRegex.TRANSACTION_PATTERN;


@Service
public class HDFCBankStatementParser {

    @Autowired
    private ExpenseRepository expenseRepository;
    @Autowired
    private  BankRegex bankerUtil;
    @Autowired
    private ExpenseCategorizer expenseCategorizer;

    Expense pendingExpense = null;
    List<Expense> recordsList = new ArrayList<>();
    StringBuilder descriptionAccumulator = new StringBuilder();
    boolean inNoiseZone = false;

    // Every page break emits: <last row of page N> "HDFC BANK LIMITED" <footer text> <page N+1 header text>
    // "...Statement of account". Everything between these two markers is noise; a genuine continuation
    // of the interrupted description resumes on the very next line after the end marker.
    private static final String NOISE_ZONE_START = "HDFC BANK LIMITED";
    private static final String NOISE_ZONE_END = "Statement of account";
    // The final page has no trailing header block, only a summary section running to EOF.
    private static final String TRAILING_SUMMARY_START = "STATEMENT SUMMARY";

    // "Opening Balance Dr Count Cr Count Debits Credits Closing Bal" is followed by a data row like:
    // "14,904.45 166 21 241,669.21 229,093.00 2,328.24" — group(1) is the opening balance.
    private static final Pattern OPENING_BALANCE_PATTERN = Pattern.compile(
            "^([\\d,]+\\.\\d{2})\\s+\\d+\\s+\\d+\\s+[\\d,]+\\.\\d{2}\\s+[\\d,]+\\.\\d{2}\\s+[\\d,]+\\.\\d{2}$"
    );


    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yy");

    public ApiResponse<Void> parsePdfFile(MultipartFile file, BigDecimal userId){
        try (InputStream is = file.getInputStream();
             PDDocument document = Loader.loadPDF(is.readAllBytes())) {

            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.setSortByPosition(true);
            String rawText = pdfStripper.getText(document);
            String[] lines = rawText.split("\\r?\\n");

            BigDecimal runningBalance = null;
            for (String line : lines) {
                Matcher openingBalanceMatcher = OPENING_BALANCE_PATTERN.matcher(line.trim());
                if (openingBalanceMatcher.matches()) {
                    runningBalance = new BigDecimal(openingBalanceMatcher.group(1).replace(",", ""));
                    break;
                }
            }

            for (String line : lines) {
                String trimmedLine = line.trim();

                if (trimmedLine.isEmpty()) {
                    continue;
                }

                if (inNoiseZone) {
                    if (trimmedLine.contains(NOISE_ZONE_END)) {
                        inNoiseZone = false;
                    }
                    continue;
                }

                if (trimmedLine.contains(NOISE_ZONE_START) || trimmedLine.contains(TRAILING_SUMMARY_START)) {
                    inNoiseZone = true;
                    continue;
                }

                if (trimmedLine.contains("Narration") && trimmedLine.contains("Withdrawal")) {
                    continue;
                }

                Matcher matcher = TRANSACTION_PATTERN.matcher(trimmedLine);

                if (matcher.matches()) {
                    // 1. Process and save the PREVIOUS pending transaction
                    if (pendingExpense != null) {
                        String completeDesc = descriptionAccumulator.toString().trim();
                        if (completeDesc.length() > 1990) {
                            completeDesc = completeDesc.substring(0, 1990) + "...";
                        }
                        pendingExpense.setDescription(completeDesc);
                        pendingExpense.setCategory(expenseCategorizer.categorize(completeDesc).name());

                        bankerUtil.saveIfUnique(pendingExpense, recordsList);
                    }

                    // 2. Extract column data for the NEW transaction
                    LocalDate transactionDate = LocalDate.parse(matcher.group(1), DATE_FORMATTER);
                    String initialDescription = matcher.group(2);
                    String referenceNumber = matcher.group(3);
                    LocalDate valueDate = LocalDate.parse(matcher.group(4), DATE_FORMATTER);
                    BigDecimal amountValue = new BigDecimal(matcher.group(5).replace(",", ""));
                    BigDecimal closingBalance = new BigDecimal(matcher.group(6).replace(",", ""));

                    // Only one amount column is ever present per row; classify it as withdrawal or
                    // deposit by whether it moved the balance down or up versus the prior transaction
                    // (or the statement's opening balance, for the very first row).
                    BigDecimal withdrawalAmount = null;
                    BigDecimal depositAmount = null;
                    if (runningBalance != null && closingBalance.compareTo(runningBalance) > 0) {
                        depositAmount = amountValue;
                    } else {
                        withdrawalAmount = amountValue;
                    }
                    runningBalance = closingBalance;

                    // 3. FIX: Completely clear the accumulator and start fresh for the NEW row!
                    descriptionAccumulator = new StringBuilder(initialDescription);

                    pendingExpense = new Expense(transactionDate, "", referenceNumber, valueDate,
                            withdrawalAmount, depositAmount, closingBalance, "UNASSIGNED", userId);

                } else {
                    // Append continuation lines to the current transaction description
                    if (pendingExpense != null) {
                        descriptionAccumulator.append(" ").append(trimmedLine);
                    }
                }
            }

            // 4. Flush the final transaction line remaining out of the loop bounds
            if (pendingExpense != null) {
                String finalDesc = descriptionAccumulator.toString().trim();
                if (finalDesc.length() > 1990) {
                    finalDesc = finalDesc.substring(0, 1990) + "...";
                }
                pendingExpense.setDescription(finalDesc);
                pendingExpense.setCategory(expenseCategorizer.categorize(finalDesc).name());
                bankerUtil.saveIfUnique(pendingExpense, recordsList);
            }

            // 5. Ultimate Safety Check: Verify lengths one last time right before batch operations
            for (Expense exp : recordsList) {
                if (exp.getDescription() != null && exp.getDescription().length() > 1990) {
                    exp.setDescription(exp.getDescription().substring(0, 1990) + "...");
                }
            }

            if (!recordsList.isEmpty()) {
                expenseRepository.saveAll(recordsList);
            }

            return new ApiResponse<>(true, "File uploaded and processed successfully. Rows added: " + recordsList.size(), null, LocalDateTime.now());

        } catch (Exception e) {
            System.err.println("File Processing Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Could not process the bank statement safely", e);
        }
    }

}
