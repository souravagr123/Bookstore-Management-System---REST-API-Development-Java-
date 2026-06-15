package com.bookstore.repository;

import com.bookstore.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    // Search by title or author (case-insensitive)
    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.authors) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Book> searchByTitleOrAuthor(@Param("query") String query, Pageable pageable);

    // Filter by genre
    Page<Book> findByGenreIgnoreCase(String genre, Pageable pageable);

    // Search by title only
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
