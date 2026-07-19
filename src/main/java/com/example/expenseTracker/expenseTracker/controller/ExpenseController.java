package com.example.expenseTracker.expenseTracker.controller;

import com.example.expenseTracker.expenseTracker.services.ExpenseServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/expense")
public class ExpenseController {

    @Autowired
    private ExpenseServices expenseServices;

    @PostMapping("/uploadStatement")
    public ResponseEntity<String> uploadStatement(@RequestParam("file") MultipartFile file){
        System.out.println("file"+ file);
        if(file.isEmpty()){
            return ResponseEntity.badRequest().body("Please Select a valid file");
        }
        String fileParsedResponse = String.valueOf(expenseServices.uploadStatement(file));
        return ResponseEntity.ok(fileParsedResponse);
    }
}
