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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowingServiceTest {

    @Mock
    private BorrowRecordRepository borrowRecordRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private FineRepository fineRepository;

    @InjectMocks
    private BorrowingServiceImpl borrowingService;

    private Member sampleMember;
    private Book sampleBook;
    private BorrowRecord sampleBorrowRecord;
    private IssueBookRequest issueRequest;

    @BeforeEach
    void setUp() {
        sampleMember = Member.builder()
                .id(1L)
                .memberCode("MEM001")
                .name("Alice Smith")
                .email("alice@example.com")
                .role(Role.STUDENT)
                .status(MemberStatus.ACTIVE)
                .build();

        sampleBook = Book.builder()
                .id(10L)
                .title("Effective Java")
                .author("Joshua Bloch")
                .isbn("978-0134685991")
                .category("Java Programming")
                .totalQuantity(5)
                .availableQuantity(3)
                .build();

        sampleBorrowRecord = BorrowRecord.builder()
                .id(100L)
                .member(sampleMember)
                .book(sampleBook)
                .issueDate(LocalDate.now().minusDays(5))
                .dueDate(LocalDate.now().plusDays(9))
                .status(BorrowStatus.ISSUED)
                .fineAmount(BigDecimal.ZERO)
                .build();

        issueRequest = IssueBookRequest.builder()
                .memberId(1L)
                .bookId(10L)
                .borrowDays(14)
                .build();
    }

    @Test
    @DisplayName("issueBook - Success")
    void issueBook_Success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(sampleBook));
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenReturn(sampleBorrowRecord);

        BorrowRecordDto result = borrowingService.issueBook(issueRequest);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("Alice Smith", result.getMemberName());
        assertEquals("Effective Java", result.getBookTitle());
        verify(bookRepository, times(1)).save(sampleBook);
        verify(borrowRecordRepository, times(1)).save(any(BorrowRecord.class));
    }

    @Test
    @DisplayName("issueBook - Throws Exception when Member is Inactive")
    void issueBook_InactiveMember_ThrowsException() {
        sampleMember.setStatus(MemberStatus.INACTIVE);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));

        assertThrows(BadRequestException.class, () -> borrowingService.issueBook(issueRequest));
        verify(borrowRecordRepository, never()).save(any(BorrowRecord.class));
    }

    @Test
    @DisplayName("issueBook - Throws Exception when Book Out of Stock")
    void issueBook_OutOfStock_ThrowsException() {
        sampleBook.setAvailableQuantity(0);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(sampleBook));

        assertThrows(BadRequestException.class, () -> borrowingService.issueBook(issueRequest));
        verify(borrowRecordRepository, never()).save(any(BorrowRecord.class));
    }

    @Test
    @DisplayName("returnBook - Normal On-Time Return (No Fine)")
    void returnBook_OnTime_Success() {
        when(borrowRecordRepository.findById(100L)).thenReturn(Optional.of(sampleBorrowRecord));
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenAnswer(i -> i.getArguments()[0]);

        BorrowRecordDto result = borrowingService.returnBook(100L);

        assertNotNull(result);
        assertEquals(BorrowStatus.RETURNED, result.getStatus());
        assertEquals(BigDecimal.ZERO, result.getFineAmount());
        assertEquals(4, sampleBook.getAvailableQuantity()); // Incremented from 3
        verify(fineRepository, never()).save(any(Fine.class));
    }

    @Test
    @DisplayName("returnBook - Overdue Return (Fine Calculated)")
    void returnBook_Overdue_CalculatesFine() {
        // Due date was 5 days ago
        sampleBorrowRecord.setDueDate(LocalDate.now().minusDays(5));
        sampleBorrowRecord.setIssueDate(LocalDate.now().minusDays(19));

        when(borrowRecordRepository.findById(100L)).thenReturn(Optional.of(sampleBorrowRecord));
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenAnswer(i -> i.getArguments()[0]);

        BorrowRecordDto result = borrowingService.returnBook(100L);

        assertNotNull(result);
        assertEquals(BorrowStatus.RETURNED, result.getStatus());
        assertEquals(BigDecimal.valueOf(5.0), result.getFineAmount()); // 5 days overdue * $1/day
        verify(fineRepository, times(1)).save(any(Fine.class));
    }

    @Test
    @DisplayName("returnBook - Already Returned Throws Exception")
    void returnBook_AlreadyReturned_ThrowsException() {
        sampleBorrowRecord.setStatus(BorrowStatus.RETURNED);
        when(borrowRecordRepository.findById(100L)).thenReturn(Optional.of(sampleBorrowRecord));

        assertThrows(BadRequestException.class, () -> borrowingService.returnBook(100L));
    }

    @Test
    @DisplayName("getMemberBorrowHistory - Success")
    void getMemberBorrowHistory_Success() {
        when(memberRepository.existsById(1L)).thenReturn(true);
        when(borrowRecordRepository.findByMemberId(1L)).thenReturn(Arrays.asList(sampleBorrowRecord));

        List<BorrowRecordDto> history = borrowingService.getMemberBorrowHistory(1L);

        assertNotNull(history);
        assertEquals(1, history.size());
        assertEquals("Alice Smith", history.get(0).getMemberName());
    }

    @Test
    @DisplayName("getOverdueBorrows - Success")
    void getOverdueBorrows_Success() {
        when(borrowRecordRepository.findOverdueRecords(any(LocalDate.class))).thenReturn(Arrays.asList(sampleBorrowRecord));

        List<BorrowRecordDto> overdue = borrowingService.getOverdueBorrows();

        assertNotNull(overdue);
        assertEquals(1, overdue.size());
    }
}
