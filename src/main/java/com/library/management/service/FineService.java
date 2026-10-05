package com.library.management.service;

import com.library.management.dto.FineDto;

import java.util.List;

public interface FineService {
    List<FineDto> getFinesByMemberId(Long memberId);
    List<FineDto> getUnpaidFinesByMemberId(Long memberId);
    List<FineDto> getAllFines();
    FineDto payFine(Long fineId);
    FineDto getFineById(Long fineId);
}
