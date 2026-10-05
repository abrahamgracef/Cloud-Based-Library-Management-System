package com.library.management.service;

import com.library.management.dto.StatsSummaryDto;
import com.library.management.model.BorrowStatus;
import com.library.management.model.FineStatus;
import com.library.management.model.MemberStatus;
import com.library.management.repository.BookRepository;
import com.library.management.repository.BorrowRecordRepository;
import com.library.management.repository.FineRepository;
import com.library.management.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final FineRepository fineRepository;

    @Autowired
    public StatsServiceImpl(BookRepository bookRepository,
                            MemberRepository memberRepository,
                            BorrowRecordRepository borrowRecordRepository,
                            FineRepository fineRepository) {
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.borrowRecordRepository = borrowRecordRepository;
        this.fineRepository = fineRepository;
    }

    @Override
    public StatsSummaryDto getStatsSummary() {
        Long totalBooks = bookRepository.sumTotalQuantity();
        Long availableBooks = bookRepository.sumAvailableQuantity();
        long issuedBooks = borrowRecordRepository.countByStatus(BorrowStatus.ISSUED);
        long activeMembers = memberRepository.countByStatus(MemberStatus.ACTIVE);
        long overdueCount = borrowRecordRepository.countOverdueRecords(LocalDate.now());

        BigDecimal totalFinesCollected = fineRepository.sumAmountByStatus(FineStatus.PAID);
        BigDecimal pendingFinesAmount = fineRepository.sumAmountByStatus(FineStatus.UNPAID);

        return StatsSummaryDto.builder()
                .totalBooks(totalBooks != null ? totalBooks : 0)
                .availableBooks(availableBooks != null ? availableBooks : 0)
                .issuedBooks(issuedBooks)
                .activeMembers(activeMembers)
                .overdueCount(overdueCount)
                .totalFinesCollected(totalFinesCollected != null ? totalFinesCollected : BigDecimal.ZERO)
                .pendingFinesAmount(pendingFinesAmount != null ? pendingFinesAmount : BigDecimal.ZERO)
                .build();
    }
}
