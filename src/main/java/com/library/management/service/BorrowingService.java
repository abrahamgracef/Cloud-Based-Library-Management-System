package com.library.management.service;

import com.library.management.dto.BorrowRecordDto;
import com.library.management.dto.IssueBookRequest;

import java.util.List;

public interface BorrowingService {
    BorrowRecordDto issueBook(IssueBookRequest request);
    BorrowRecordDto returnBook(Long borrowRecordId);
    List<BorrowRecordDto> getMemberBorrowHistory(Long memberId);
    List<BorrowRecordDto> getActiveBorrows();
    List<BorrowRecordDto> getOverdueBorrows();
    List<BorrowRecordDto> getAllBorrowRecords();
}
