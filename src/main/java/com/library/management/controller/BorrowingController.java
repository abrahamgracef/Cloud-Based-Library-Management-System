package com.library.management.controller;

import com.library.management.dto.BorrowRecordDto;
import com.library.management.dto.IssueBookRequest;
import com.library.management.service.BorrowingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrowing")
@CrossOrigin(origins = "*")
public class BorrowingController {

    private final BorrowingService borrowingService;

    @Autowired
    public BorrowingController(BorrowingService borrowingService) {
        this.borrowingService = borrowingService;
    }

    @PostMapping("/issue")
    public ResponseEntity<BorrowRecordDto> issueBook(@Valid @RequestBody IssueBookRequest request) {
        BorrowRecordDto record = borrowingService.issueBook(request);
        return new ResponseEntity<>(record, HttpStatus.CREATED);
    }

    @PostMapping("/return/{id}")
    public ResponseEntity<BorrowRecordDto> returnBook(@PathVariable Long id) {
        BorrowRecordDto record = borrowingService.returnBook(id);
        return ResponseEntity.ok(record);
    }

    @GetMapping("/history/member/{memberId}")
    public ResponseEntity<List<BorrowRecordDto>> getMemberBorrowHistory(@PathVariable Long memberId) {
        List<BorrowRecordDto> history = borrowingService.getMemberBorrowHistory(memberId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/active")
    public ResponseEntity<List<BorrowRecordDto>> getActiveBorrows() {
        List<BorrowRecordDto> activeBorrows = borrowingService.getActiveBorrows();
        return ResponseEntity.ok(activeBorrows);
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<BorrowRecordDto>> getOverdueBorrows() {
        List<BorrowRecordDto> overdueBorrows = borrowingService.getOverdueBorrows();
        return ResponseEntity.ok(overdueBorrows);
    }

    @GetMapping
    public ResponseEntity<List<BorrowRecordDto>> getAllBorrowRecords() {
        List<BorrowRecordDto> records = borrowingService.getAllBorrowRecords();
        return ResponseEntity.ok(records);
    }
}
