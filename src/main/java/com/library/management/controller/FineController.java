package com.library.management.controller;

import com.library.management.dto.FineDto;
import com.library.management.service.FineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fines")
@CrossOrigin(origins = "*")
public class FineController {

    private final FineService fineService;

    @Autowired
    public FineController(FineService fineService) {
        this.fineService = fineService;
    }

    @GetMapping
    public ResponseEntity<List<FineDto>> getAllFines() {
        List<FineDto> fines = fineService.getAllFines();
        return ResponseEntity.ok(fines);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FineDto> getFineById(@PathVariable Long id) {
        FineDto fine = fineService.getFineById(id);
        return ResponseEntity.ok(fine);
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<FineDto>> getFinesByMemberId(@PathVariable Long memberId) {
        List<FineDto> fines = fineService.getFinesByMemberId(memberId);
        return ResponseEntity.ok(fines);
    }

    @GetMapping("/member/{memberId}/unpaid")
    public ResponseEntity<List<FineDto>> getUnpaidFinesByMemberId(@PathVariable Long memberId) {
        List<FineDto> fines = fineService.getUnpaidFinesByMemberId(memberId);
        return ResponseEntity.ok(fines);
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<FineDto> payFine(@PathVariable Long id) {
        FineDto paidFine = fineService.payFine(id);
        return ResponseEntity.ok(paidFine);
    }
}
