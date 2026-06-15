package com.bookstore.service.impl;

import com.bookstore.dto.request.BookRequest;
import com.bookstore.dto.response.BookResponse;
import com.bookstore.entity.Book;
import com.bookstore.exception.BadRequestException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.repository.BookRepository;
import com.bookstore.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    @Override
    public Page<BookResponse> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    public Page<BookResponse> searchBooks(String query, Pageable pageable) {
        return bookRepository.searchByTitleOrAuthor(query, pageable).map(this::toResponse);
    }

    @Override
    public Page<BookResponse> getBooksByGenre(String genre, Pageable pageable) {
        return bookRepository.findByGenreIgnoreCase(genre, pageable).map(this::toResponse);
    }

    @Override
    public BookResponse getBookById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public BookResponse createBook(BookRequest request) {
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BadRequestException("A book with ISBN '" + request.getIsbn() + "' already exists");
        }
        Book book = Book.builder()
                .title(request.getTitle())
                .authors(request.getAuthors())
                .genre(request.getGenre())
                .isbn(request.getIsbn())
                .price(request.getPrice())
                .description(request.getDescription())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .build();
        return toResponse(bookRepository.save(book));
    }

    @Override
    @Transactional
    public BookResponse updateBook(Long id, BookRequest request) {
        Book book = findOrThrow(id);
        // Allow same ISBN for the same book, reject if another book has it
        bookRepository.findByIsbn(request.getIsbn())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new BadRequestException("ISBN '" + request.getIsbn() + "' is already used by another book");
                    }
                });
        book.setTitle(request.getTitle());
        book.setAuthors(request.getAuthors());
        book.setGenre(request.getGenre());
        book.setIsbn(request.getIsbn());
        book.setPrice(request.getPrice());
        book.setDescription(request.getDescription());
        book.setStockQuantity(request.getStockQuantity());
        book.setImageUrl(request.getImageUrl());
        return toResponse(bookRepository.save(book));
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Book", id);
        }
        bookRepository.deleteById(id);
    }

    private Book findOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", id));
    }

    private BookResponse toResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .authors(book.getAuthors())
                .genre(book.getGenre())
                .isbn(book.getIsbn())
                .price(book.getPrice())
                .description(book.getDescription())
                .stockQuantity(book.getStockQuantity())
                .imageUrl(book.getImageUrl())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }
}
