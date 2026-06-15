package com.bookstore.dto.request;

import com.bookstore.entity.Order;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderStatusRequest {

    @NotNull(message = "Order status is required")
    private Order.OrderStatus orderStatus;

    private Order.PaymentStatus paymentStatus;
}
