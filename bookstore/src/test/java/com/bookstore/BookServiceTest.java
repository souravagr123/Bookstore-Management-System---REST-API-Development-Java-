package com.bookstore;

import com.bookstore.dto.request.BookRequest;
import com.bookstore.dto.response.BookResponse;
import com.bookstore.entity.Book;
import com.bookstore.exception.BadRequestException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.repository.BookRepository;
import com.bookstore.service.impl.BookServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book sampleBook;
    private BookRequest sampleRequest;

    @BeforeEach
    void setup() {
        sampleBook = Book.builder()
                .id(1L)
                .title("Clean Code")
                .authors("Robert C. Martin")
                .genre("Programming")
                .isbn("9780132350884")
                .price(new BigDecimal("29.99"))
                .stockQuantity(10)
                .build();

        sampleRequest = BookRequest.builder()
                .title("Clean Code")
                .authors("Robert C. Martin")
                .genre("Programming")
                .isbn("9780132350884")
                .price(new BigDecimal("29.99"))
                .stockQuantity(10)
                .build();
    }

    @Test
    @DisplayName("Should get book by ID successfully")
    void getBookById_success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        BookResponse response = bookService.getBookById(1L);
        assertThat(response.getTitle()).isEqualTo("Clean Code");
        assertThat(response.getIsbn()).isEqualTo("9780132350884");
    }

    @Test
    @DisplayName("Should throw exception when book not found")
    void getBookById_notFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> bookService.getBookById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book not found with id: 99");
    }

    @Test
    @DisplayName("Should create book successfully")
    void createBook_success() {
        when(bookRepository.existsByIsbn(anyString())).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

        BookResponse response = bookService.createBook(sampleRequest);
        assertThat(response.getTitle()).isEqualTo("Clean Code");
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    @DisplayName("Should throw exception when creating book with duplicate ISBN")
    void createBook_duplicateIsbn() {
        when(bookRepository.existsByIsbn(anyString())).thenReturn(true);
        assertThatThrownBy(() -> bookService.createBook(sampleRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should delete book successfully")
    void deleteBook_success() {
        when(bookRepository.existsById(1L)).thenReturn(true);
        doNothing().when(bookRepository).deleteById(1L);
        assertThatCode(() -> bookService.deleteBook(1L)).doesNotThrowAnyException();
        verify(bookRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw when deleting non-existent book")
    void deleteBook_notFound() {
        when(bookRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> bookService.deleteBook(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should get all books with pagination")
    void getAllBooks_pagination() {
        Page<Book> page = new PageImpl<>(java.util.List.of(sampleBook));
        when(bookRepository.findAll(any(Pageable.class))).thenReturn(page);
        Page<BookResponse> result = bookService.getAllBooks(PageRequest.of(0, 10));
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Clean Code");
    }
}
