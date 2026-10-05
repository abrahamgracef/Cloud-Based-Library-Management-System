package com.library.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.management.dto.BookDto;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    private BookDto sampleBookDto;

    @BeforeEach
    void setUp() {
        sampleBookDto = BookDto.builder()
                .id(1L)
                .title("Design Patterns")
                .author("Erich Gamma")
                .isbn("978-0201633610")
                .category("Software Design")
                .totalQuantity(5)
                .availableQuantity(5)
                .build();
    }

    @Test
    @DisplayName("GET /api/books - Success")
    void getAllBooks_ReturnsBookList() throws Exception {
        List<BookDto> books = Arrays.asList(sampleBookDto);
        when(bookService.getAllBooks()).thenReturn(books);

        mockMvc.perform(get("/api/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Design Patterns")))
                .andExpect(jsonPath("$[0].isbn", is("978-0201633610")));
    }

    @Test
    @DisplayName("GET /api/books/{id} - Success")
    void getBookById_Success() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(sampleBookDto);

        mockMvc.perform(get("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Design Patterns")));
    }

    @Test
    @DisplayName("GET /api/books/{id} - Not Found")
    void getBookById_NotFound() throws Exception {
        when(bookService.getBookById(99L)).thenThrow(new ResourceNotFoundException("Book not found with id: 99"));

        mockMvc.perform(get("/api/books/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/books/isbn/{isbn} - Success")
    void getBookByIsbn_Success() throws Exception {
        when(bookService.getBookByIsbn("978-0201633610")).thenReturn(sampleBookDto);

        mockMvc.perform(get("/api/books/isbn/978-0201633610")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn", is("978-0201633610")));
    }

    @Test
    @DisplayName("GET /api/books/search?query=Design - Success")
    void searchBooks_Success() throws Exception {
        when(bookService.searchBooks("Design")).thenReturn(Arrays.asList(sampleBookDto));

        mockMvc.perform(get("/api/books/search")
                        .param("query", "Design")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Design Patterns")));
    }

    @Test
    @DisplayName("POST /api/books - Success")
    void createBook_Success() throws Exception {
        when(bookService.createBook(any(BookDto.class))).thenReturn(sampleBookDto);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleBookDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Design Patterns")))
                .andExpect(jsonPath("$.author", is("Erich Gamma")));
    }

    @Test
    @DisplayName("POST /api/books - Validation Error")
    void createBook_ValidationError() throws Exception {
        BookDto invalidBook = BookDto.builder().title("").build(); // Blank title, missing fields

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBook)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/books/{id} - Success")
    void updateBook_Success() throws Exception {
        when(bookService.updateBook(eq(1L), any(BookDto.class))).thenReturn(sampleBookDto);

        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleBookDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Design Patterns")));
    }

    @Test
    @DisplayName("DELETE /api/books/{id} - Success")
    void deleteBook_Success() throws Exception {
        doNothing().when(bookService).deleteBook(1L);

        mockMvc.perform(delete("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(bookService, times(1)).deleteBook(1L);
    }
}
