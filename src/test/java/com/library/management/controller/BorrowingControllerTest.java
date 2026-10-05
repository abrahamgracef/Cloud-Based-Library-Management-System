package com.library.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.management.dto.BorrowRecordDto;
import com.library.management.dto.IssueBookRequest;
import com.library.management.model.BorrowStatus;
import com.library.management.service.BorrowingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BorrowingController.class)
class BorrowingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BorrowingService borrowingService;

    private BorrowRecordDto sampleRecordDto;
    private IssueBookRequest issueRequest;

    @BeforeEach
    void setUp() {
        sampleRecordDto = BorrowRecordDto.builder()
                .id(100L)
                .memberId(1L)
                .memberCode("MEM001")
                .memberName("John Doe")
                .bookId(5L)
                .bookTitle("Clean Architecture")
                .bookIsbn("978-0134494166")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .status(BorrowStatus.ISSUED)
                .fineAmount(BigDecimal.ZERO)
                .build();

        issueRequest = IssueBookRequest.builder()
                .memberId(1L)
                .bookId(5L)
                .borrowDays(14)
                .build();
    }

    @Test
    @DisplayName("POST /api/borrowing/issue - Success")
    void issueBook_Success() throws Exception {
        when(borrowingService.issueBook(any(IssueBookRequest.class))).thenReturn(sampleRecordDto);

        mockMvc.perform(post("/api/borrowing/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(issueRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(100)))
                .andExpect(jsonPath("$.memberName", is("John Doe")))
                .andExpect(jsonPath("$.bookTitle", is("Clean Architecture")));
    }

    @Test
    @DisplayName("POST /api/borrowing/issue - Validation Error")
    void issueBook_ValidationError() throws Exception {
        IssueBookRequest invalidRequest = IssueBookRequest.builder().build(); // missing memberId and bookId

        mockMvc.perform(post("/api/borrowing/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/borrowing/return/{id} - Success")
    void returnBook_Success() throws Exception {
        sampleRecordDto.setStatus(BorrowStatus.RETURNED);
        sampleRecordDto.setReturnDate(LocalDate.now());
        when(borrowingService.returnBook(100L)).thenReturn(sampleRecordDto);

        mockMvc.perform(post("/api/borrowing/return/100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RETURNED")))
                .andExpect(jsonPath("$.id", is(100)));
    }

    @Test
    @DisplayName("GET /api/borrowing/history/member/{memberId} - Success")
    void getMemberBorrowHistory_Success() throws Exception {
        List<BorrowRecordDto> history = Arrays.asList(sampleRecordDto);
        when(borrowingService.getMemberBorrowHistory(1L)).thenReturn(history);

        mockMvc.perform(get("/api/borrowing/history/member/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].memberName", is("John Doe")));
    }

    @Test
    @DisplayName("GET /api/borrowing/active - Success")
    void getActiveBorrows_Success() throws Exception {
        when(borrowingService.getActiveBorrows()).thenReturn(Arrays.asList(sampleRecordDto));

        mockMvc.perform(get("/api/borrowing/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("ISSUED")));
    }

    @Test
    @DisplayName("GET /api/borrowing/overdue - Success")
    void getOverdueBorrows_Success() throws Exception {
        sampleRecordDto.setStatus(BorrowStatus.OVERDUE);
        when(borrowingService.getOverdueBorrows()).thenReturn(Arrays.asList(sampleRecordDto));

        mockMvc.perform(get("/api/borrowing/overdue")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("OVERDUE")));
    }

    @Test
    @DisplayName("GET /api/borrowing - Success")
    void getAllBorrowRecords_Success() throws Exception {
        when(borrowingService.getAllBorrowRecords()).thenReturn(Arrays.asList(sampleRecordDto));

        mockMvc.perform(get("/api/borrowing")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
