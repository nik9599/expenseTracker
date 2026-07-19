package com.example.expenseTracker.expenseTracker.services;

import com.example.expenseTracker.expenseTracker.bankParser.HDFCBankStatementParser;
import com.example.expenseTracker.expenseTracker.dto.ApiResponse;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class ExpenseServices {

    @Autowired
    HDFCBankStatementParser hdfcParser;

    private static final Pattern BANK_PATTERN = Pattern.compile(
            "\\b(HDFC\\s*BANK|ICICI\\s*BANK|STATE\\s*BANK\\s*OF\\s*INDIA|SBI|AXIS\\s*BANK|KOTAK\\s*MAHINDRA\\s*BANK)\\b",
            Pattern.CASE_INSENSITIVE
    );

    public ApiResponse<Void> uploadStatement(MultipartFile file) {
        return switch (detectBankName(file)) {
            case "HDFC BANK" -> hdfcParser.parsePdfFile(file);
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


}
