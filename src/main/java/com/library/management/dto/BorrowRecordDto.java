package com.library.management.dto;

import com.library.management.model.BorrowStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowRecordDto {

    private Long id;

    private Long memberId;
    private String memberCode;
    private String memberName;

    private Long bookId;
    private String bookTitle;
    private String bookIsbn;

    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private BorrowStatus status;
    private BigDecimal fineAmount;
}
