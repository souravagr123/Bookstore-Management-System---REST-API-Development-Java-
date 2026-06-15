package com.bookstore.service;

import com.bookstore.dto.request.OrderRequest;
import com.bookstore.dto.request.OrderStatusRequest;
import com.bookstore.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    Page<OrderResponse> getAllOrders(Pageable pageable);
    Page<OrderResponse> getOrdersByUser(String email, Pageable pageable);
    OrderResponse getOrderById(Long id);
    OrderResponse placeOrder(String email, OrderRequest request);
    OrderResponse updateOrderStatus(Long id, OrderStatusRequest request);
}
