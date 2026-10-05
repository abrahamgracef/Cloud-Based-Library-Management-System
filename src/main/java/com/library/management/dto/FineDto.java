package com.library.management.dto;

import com.library.management.model.FineStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FineDto {

    private Long id;

    private Long memberId;
    private String memberName;
    private String memberCode;

    private Long borrowRecordId;
    private String bookTitle;

    private BigDecimal amount;
    private FineStatus status;
    private LocalDateTime paidAt;
    private String reason;
}
