package com.example.expenseTracker.expenseTracker.controller;

import com.example.expenseTracker.expenseTracker.services.ExpenseServices;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.example.expenseTracker.expenseTracker.dto.UserSearchCriteria;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;


@RestController
@RequestMapping("api/expense")
public class ExpenseController {

    private final ExpenseServices expenseServices;
    private final ObjectMapper objectMapper;

    public ExpenseController(ExpenseServices expenseServices, ObjectMapper objectMapper) {
        this.expenseServices = expenseServices;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/uploadStatement")
    public ResponseEntity<String> uploadStatement(@RequestParam("file") MultipartFile file){
        System.out.println("file"+ file);
        if(file.isEmpty()){
            return ResponseEntity.badRequest().body("Please Select a valid file");
        }
        String fileParsedResponse = String.valueOf(expenseServices.uploadStatement(file));
        return ResponseEntity.ok(fileParsedResponse);
    }

    @PostMapping("/createEntry")
    public ResponseEntity<String> createEntry(@RequestParam("payload") String payloadJSON) {
        UserSearchCriteria criteria = objectMapper.readValue(payloadJSON, UserSearchCriteria.class);
        String response = expenseServices.addStatement(criteria);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateEntry(
            @PathVariable Long id,
            @RequestParam("payload") String payloadJSON) {
        UserSearchCriteria criteria = objectMapper.readValue(payloadJSON, UserSearchCriteria.class);
        return expenseServices.updateStatement(id, criteria)
                .map(expense -> ResponseEntity.ok("Updated expense with id " + expense.getId()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable Long id) {
        if (expenseServices.deleteEntity(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(JacksonException.class)
    public ResponseEntity<String> handleInvalidJson(JacksonException exception) {
        return ResponseEntity.badRequest().body("Invalid JSON format");
    }
}
