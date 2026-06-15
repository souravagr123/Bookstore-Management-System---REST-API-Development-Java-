package com.bookstore.service.impl;

import com.bookstore.dto.request.OrderRequest;
import com.bookstore.dto.request.OrderStatusRequest;
import com.bookstore.dto.response.OrderResponse;
import com.bookstore.entity.*;
import com.bookstore.exception.BadRequestException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.repository.*;
import com.bookstore.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Override
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    public Page<OrderResponse> getOrdersByUser(String email, Pageable pageable) {
        User user = findUserByEmail(email);
        return orderRepository.findByUserId(user.getId(), pageable).map(this::toResponse);
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        return toResponse(findOrderOrThrow(id));
    }

    @Override
    @Transactional
    public OrderResponse placeOrder(String email, OrderRequest request) {
        User user = findUserByEmail(email);

        List<OrderItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // Build order
        Order order = Order.builder()
                .user(user)
                .orderStatus(Order.OrderStatus.PENDING)
                .paymentStatus(Order.PaymentStatus.PENDING)
                .build();

        for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Book book = bookRepository.findById(itemReq.getBookId())
                    .orElseThrow(() -> new ResourceNotFoundException("Book", itemReq.getBookId()));

            if (book.getStockQuantity() < itemReq.getQuantity()) {
                throw new BadRequestException("Insufficient stock for book: '" + book.getTitle()
                        + "'. Available: " + book.getStockQuantity());
            }

            BigDecimal itemTotal = book.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .book(book)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(book.getPrice())
                    .totalPrice(itemTotal)
                    .build();
            items.add(orderItem);

            // Deduct stock
            book.setStockQuantity(book.getStockQuantity() - itemReq.getQuantity());
            bookRepository.save(book);
        }

        order.setOrderItems(items);
        order.setTotalAmount(totalAmount);
        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatusRequest request) {
        Order order = findOrderOrThrow(id);
        order.setOrderStatus(request.getOrderStatus());
        if (request.getPaymentStatus() != null) {
            order.setPaymentStatus(request.getPaymentStatus());
        }
        return toResponse(orderRepository.save(order));
    }

    private Order findOrderOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private OrderResponse toResponse(Order order) {
        List<OrderResponse.OrderItemResponse> itemResponses = order.getOrderItems() == null
                ? List.of()
                : order.getOrderItems().stream().map(item ->
                    OrderResponse.OrderItemResponse.builder()
                            .bookId(item.getBook().getId())
                            .bookTitle(item.getBook().getTitle())
                            .isbn(item.getBook().getIsbn())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .totalPrice(item.getTotalPrice())
                            .build()
                ).collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .customerName(order.getUser().getName())
                .customerEmail(order.getUser().getEmail())
                .items(itemResponses)
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
