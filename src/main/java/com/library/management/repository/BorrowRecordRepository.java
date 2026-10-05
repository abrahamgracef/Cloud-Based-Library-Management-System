package com.library.management.repository;

import com.library.management.model.BorrowRecord;
import com.library.management.model.BorrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    List<BorrowRecord> findByMemberId(Long memberId);

    List<BorrowRecord> findByMemberIdAndStatus(Long memberId, BorrowStatus status);

    List<BorrowRecord> findByStatus(BorrowStatus status);

    List<BorrowRecord> findByBookId(Long bookId);

    List<BorrowRecord> findByStatusAndDueDateBefore(BorrowStatus status, LocalDate date);

    @Query("SELECT b FROM BorrowRecord b WHERE b.status = 'ISSUED' AND b.dueDate < :currentDate")
    List<BorrowRecord> findOverdueRecords(@Param("currentDate") LocalDate currentDate);

    long countByStatus(BorrowStatus status);

    @Query("SELECT COUNT(b) FROM BorrowRecord b WHERE b.status = 'ISSUED' AND b.dueDate < :currentDate")
    long countOverdueRecords(@Param("currentDate") LocalDate currentDate);
}
