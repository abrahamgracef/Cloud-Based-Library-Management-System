package com.library.management.service;

import com.library.management.dto.BookDto;

import java.util.List;

public interface BookService {
    BookDto createBook(BookDto bookDto);
    BookDto updateBook(Long id, BookDto bookDto);
    BookDto getBookById(Long id);
    BookDto getBookByIsbn(String isbn);
    List<BookDto> getAllBooks();
    List<BookDto> searchBooks(String query);
    void deleteBook(Long id);
}
