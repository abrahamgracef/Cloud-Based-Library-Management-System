package com.library.management.service;

import com.library.management.dto.BorrowRecordDto;
import com.library.management.dto.IssueBookRequest;
import com.library.management.exception.BadRequestException;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.model.*;
import com.library.management.repository.BookRepository;
import com.library.management.repository.BorrowRecordRepository;
import com.library.management.repository.FineRepository;
import com.library.management.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BorrowingServiceImpl implements BorrowingService {

    private static final Logger log = LoggerFactory.getLogger(BorrowingServiceImpl.class);
    private static final int DEFAULT_BORROW_DAYS = 14;
    private static final BigDecimal DAILY_FINE_RATE = BigDecimal.valueOf(1.00); // $1 per day

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final FineRepository fineRepository;

    @Autowired
    public BorrowingServiceImpl(BorrowRecordRepository borrowRecordRepository,
                                BookRepository bookRepository,
                                MemberRepository memberRepository,
                                FineRepository fineRepository) {
        this.borrowRecordRepository = borrowRecordRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.fineRepository = fineRepository;
    }

    @Override
    public BorrowRecordDto issueBook(IssueBookRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + request.getMemberId()));

        if (member.getStatus() != MemberStatus.ACTIVE) {
            log.warn("Failed borrow attempt: Member {} (ID: {}) is inactive.", member.getName(), member.getId());
            throw new BadRequestException("Member is inactive and cannot borrow books.");
        }

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + request.getBookId()));

        if (book.getAvailableQuantity() <= 0) {
            log.warn("Failed borrow attempt: Book '{}' (ID: {}) is out of stock.", book.getTitle(), book.getId());
            throw new BadRequestException("Book '" + book.getTitle() + "' is currently out of stock.");
        }

        // Decrement available book quantity
        book.setAvailableQuantity(book.getAvailableQuantity() - 1);
        bookRepository.save(book);

        int days = (request.getBorrowDays() != null && request.getBorrowDays() > 0)
                ? request.getBorrowDays()
                : DEFAULT_BORROW_DAYS;

        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(days);

        BorrowRecord record = BorrowRecord.builder()
                .member(member)
                .book(book)
                .issueDate(issueDate)
                .dueDate(dueDate)
                .status(BorrowStatus.ISSUED)
                .fineAmount(BigDecimal.ZERO)
                .build();

        BorrowRecord savedRecord = borrowRecordRepository.save(record);

        log.info("==================================================");
        log.info("📚 AUDIT LOG: BOOK ISSUED SUCCESSFULLY");
        log.info(" - Transaction ID : #{}", savedRecord.getId());
        log.info(" - Book Title     : \"{}\"", book.getTitle());
        log.info(" - Borrowed By    : {} (Member ID: {})", member.getName(), member.getId());
        log.info(" - Issue Date     : {}", issueDate);
        log.info(" - Due Date       : {}", dueDate);
        log.info("==================================================");

        return mapToDto(savedRecord);
    }

    @Override
    public BorrowRecordDto returnBook(Long borrowRecordId) {
        BorrowRecord record = borrowRecordRepository.findById(borrowRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow record not found with id: " + borrowRecordId));

        if (record.getStatus() == BorrowStatus.RETURNED) {
            throw new BadRequestException("Book has already been returned.");
        }

        LocalDate returnDate = LocalDate.now();
        record.setReturnDate(returnDate);

        BigDecimal fineAmount = BigDecimal.ZERO;
        // Auto-calculate overdue fine if returnDate > dueDate ($1/day)
        if (returnDate.isAfter(record.getDueDate())) {
            long overdueDays = ChronoUnit.DAYS.between(record.getDueDate(), returnDate);
            fineAmount = DAILY_FINE_RATE.multiply(BigDecimal.valueOf(overdueDays));
            record.setFineAmount(fineAmount);

            // Create fine entry if fine amount > 0
            if (fineAmount.compareTo(BigDecimal.ZERO) > 0) {
                Fine fine = Fine.builder()
                        .member(record.getMember())
                        .borrowRecord(record)
                        .amount(fineAmount)
                        .status(FineStatus.UNPAID)
                        .reason("Overdue return: " + overdueDays + " day(s) late ($1/day)")
                        .build();
                fineRepository.save(fine);
                log.warn("⚠️ OVERDUE FINE ASSESSED: ${} for {} (Record #{})", fineAmount, record.getMember().getName(), record.getId());
            }
        }

        record.setStatus(BorrowStatus.RETURNED);

        // Increment available book quantity
        Book book = record.getBook();
        book.setAvailableQuantity(book.getAvailableQuantity() + 1);
        bookRepository.save(book);

        BorrowRecord savedRecord = borrowRecordRepository.save(record);

        log.info("==================================================");
        log.info("🔄 AUDIT LOG: BOOK RETURNED SUCCESSFULLY");
        log.info(" - Transaction ID : #{}", savedRecord.getId());
        log.info(" - Book Title     : \"{}\"", book.getTitle());
        log.info(" - Returned By    : {} (Member ID: {})", record.getMember().getName(), record.getMember().getId());
        log.info(" - Return Date    : {}", returnDate);
        log.info(" - Assessed Fine  : ${}", fineAmount);
        log.info("==================================================");

        return mapToDto(savedRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowRecordDto> getMemberBorrowHistory(Long memberId) {
        return borrowRecordRepository.findByMemberId(memberId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowRecordDto> getActiveBorrows() {
        return borrowRecordRepository.findByStatus(BorrowStatus.ISSUED).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowRecordDto> getOverdueBorrows() {
        return borrowRecordRepository.findByStatusAndDueDateBefore(BorrowStatus.ISSUED, LocalDate.now()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowRecordDto> getAllBorrowRecords() {
        return borrowRecordRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private BorrowRecordDto mapToDto(BorrowRecord record) {
        return BorrowRecordDto.builder()
                .id(record.getId())
                .bookId(record.getBook().getId())
                .bookTitle(record.getBook().getTitle())
                .memberId(record.getMember().getId())
                .memberName(record.getMember().getName())
                .issueDate(record.getIssueDate())
                .dueDate(record.getDueDate())
                .returnDate(record.getReturnDate())
                .status(record.getStatus())
                .fineAmount(record.getFineAmount())
                .build();
    }
}
