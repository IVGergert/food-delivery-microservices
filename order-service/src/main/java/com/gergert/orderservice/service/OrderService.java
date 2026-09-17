package com.gergert.orderservice.service;

import com.gergert.common.dto.OrderPaymentRequestDto;
import com.gergert.orderservice.dto.CreateOrderRequestDto;
import com.gergert.orderservice.dto.OrderDto;

import java.util.List;

public interface OrderService {
    OrderDto processPayment(Long id, OrderPaymentRequestDto requestDto, Long customerId);
    OrderDto createOrder(CreateOrderRequestDto request, Long customerId);
    List<OrderDto> getAllOrdersByUserId(Long customerId);
    OrderDto cancelOrder(Long id, Long customerId);

}
