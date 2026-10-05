package com.library.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatsSummaryDto {

    private long totalBooks;
    private long availableBooks;
    private long issuedBooks;
    private long activeMembers;
    private long overdueCount;
    private BigDecimal totalFinesCollected;
    private BigDecimal pendingFinesAmount;
}
