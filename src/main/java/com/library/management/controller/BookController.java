package com.library.management.controller;

import com.library.management.dto.BookDto;
import com.library.management.service.BookService;
import com.library.management.service.S3StorageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
public class BookController {

    private final BookService bookService;
    private final S3StorageService s3StorageService;

    @Autowired
    public BookController(BookService bookService, S3StorageService s3StorageService) {
        this.bookService = bookService;
        this.s3StorageService = s3StorageService;
    }

    @GetMapping
    public ResponseEntity<List<BookDto>> getAllBooks() {
        List<BookDto> books = bookService.getAllBooks();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDto> getBookById(@PathVariable Long id) {
        BookDto book = bookService.getBookById(id);
        return ResponseEntity.ok(book);
    }

    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<BookDto> getBookByIsbn(@PathVariable String isbn) {
        BookDto book = bookService.getBookByIsbn(isbn);
        return ResponseEntity.ok(book);
    }

    @GetMapping("/search")
    public ResponseEntity<List<BookDto>> searchBooks(@RequestParam(required = false, defaultValue = "") String query) {
        List<BookDto> books = bookService.searchBooks(query);
        return ResponseEntity.ok(books);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookDto> createBook(@Valid @RequestBody BookDto bookDto) {
        BookDto createdBook = bookService.createBook(bookDto);
        return new ResponseEntity<>(createdBook, HttpStatus.CREATED);
    }

    @PostMapping(value = "/with-cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BookDto> createBookWithCover(
            @RequestParam("title") String title,
            @RequestParam("author") String author,
            @RequestParam("isbn") String isbn,
            @RequestParam("category") String category,
            @RequestParam("totalQuantity") Integer totalQuantity,
            @RequestParam(value = "coverFile", required = false) MultipartFile coverFile) {

        String coverUrl = "https://images.unsplash.com/photo-1532012197267-da84d127e765?w=400";
        if (coverFile != null && !coverFile.isEmpty()) {
            coverUrl = s3StorageService.uploadCoverImage(coverFile);
        }

        BookDto bookDto = BookDto.builder()
                .title(title)
                .author(author)
                .isbn(isbn)
                .category(category)
                .totalQuantity(totalQuantity)
                .availableQuantity(totalQuantity)
                .coverImageUrl(coverUrl)
                .build();

        BookDto createdBook = bookService.createBook(bookDto);
        return new ResponseEntity<>(createdBook, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookDto> updateBook(@PathVariable Long id, @Valid @RequestBody BookDto bookDto) {
        BookDto updatedBook = bookService.updateBook(id, bookDto);
        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok(Map.of("message", "Book successfully deleted from database and system."));
    }
}
