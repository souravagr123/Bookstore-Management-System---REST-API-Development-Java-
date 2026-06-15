package com.bookstore.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BookResponse {
    private Long id;
    private String title;
    private String authors;
    private String genre;
    private String isbn;
    private BigDecimal price;
    private String description;
    private Integer stockQuantity;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
