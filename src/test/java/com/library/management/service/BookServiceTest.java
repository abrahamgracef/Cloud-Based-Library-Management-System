package com.library.management.service;

import com.library.management.dto.BookDto;
import com.library.management.exception.BadRequestException;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.model.Book;
import com.library.management.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book sampleBook;
    private BookDto sampleBookDto;

    @BeforeEach
    void setUp() {
        sampleBook = Book.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("978-0132350884")
                .category("Software Engineering")
                .totalQuantity(10)
                .availableQuantity(10)
                .coverImageUrl("http://example.com/cover.jpg")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleBookDto = BookDto.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("978-0132350884")
                .category("Software Engineering")
                .totalQuantity(10)
                .availableQuantity(10)
                .coverImageUrl("http://example.com/cover.jpg")
                .build();
    }

    @Test
    @DisplayName("createBook - Success")
    void createBook_Success() {
        when(bookRepository.existsByIsbn(sampleBookDto.getIsbn())).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

        BookDto created = bookService.createBook(sampleBookDto);

        assertNotNull(created);
        assertEquals("Clean Code", created.getTitle());
        assertEquals("978-0132350884", created.getIsbn());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    @DisplayName("createBook - Throws BadRequestException when ISBN already exists")
    void createBook_DuplicateIsbn_ThrowsException() {
        when(bookRepository.existsByIsbn(sampleBookDto.getIsbn())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> bookService.createBook(sampleBookDto));
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("getBookById - Success")
    void getBookById_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));

        BookDto found = bookService.getBookById(1L);

        assertNotNull(found);
        assertEquals(1L, found.getId());
        assertEquals("Clean Code", found.getTitle());
    }

    @Test
    @DisplayName("getBookById - Throws ResourceNotFoundException when not found")
    void getBookById_NotFound_ThrowsException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookService.getBookById(99L));
    }

    @Test
    @DisplayName("getBookByIsbn - Success")
    void getBookByIsbn_Success() {
        when(bookRepository.findByIsbn("978-0132350884")).thenReturn(Optional.of(sampleBook));

        BookDto found = bookService.getBookByIsbn("978-0132350884");

        assertNotNull(found);
        assertEquals("978-0132350884", found.getIsbn());
    }

    @Test
    @DisplayName("getAllBooks - Success")
    void getAllBooks_Success() {
        when(bookRepository.findAll()).thenReturn(Arrays.asList(sampleBook));

        List<BookDto> books = bookService.getAllBooks();

        assertNotNull(books);
        assertEquals(1, books.size());
        assertEquals("Clean Code", books.get(0).getTitle());
    }

    @Test
    @DisplayName("searchBooks - With Query Success")
    void searchBooks_WithQuery_Success() {
        when(bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrCategoryContainingIgnoreCaseOrIsbnContainingIgnoreCase(
                "Clean", "Clean", "Clean", "Clean")).thenReturn(Arrays.asList(sampleBook));

        List<BookDto> results = bookService.searchBooks("Clean");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Clean Code", results.get(0).getTitle());
    }

    @Test
    @DisplayName("searchBooks - Empty Query Returns All Books")
    void searchBooks_EmptyQuery_ReturnsAll() {
        when(bookRepository.findAll()).thenReturn(Arrays.asList(sampleBook));

        List<BookDto> results = bookService.searchBooks("");

        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("updateBook - Success")
    void updateBook_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

        BookDto updated = bookService.updateBook(1L, sampleBookDto);

        assertNotNull(updated);
        assertEquals("Clean Code", updated.getTitle());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    @DisplayName("deleteBook - Success")
    void deleteBook_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        doNothing().when(bookRepository).delete(sampleBook);

        assertDoesNotThrow(() -> bookService.deleteBook(1L));
        verify(bookRepository, times(1)).delete(sampleBook);
    }
}
