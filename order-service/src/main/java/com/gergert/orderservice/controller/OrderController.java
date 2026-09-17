package com.gergert.orderservice.controller;

import com.gergert.common.dto.jwt.JwtClaimsDto;
import com.gergert.orderservice.dto.CreateOrderRequestDto;
import com.gergert.orderservice.dto.OrderDto;
import com.gergert.common.dto.OrderPaymentRequestDto;
import com.gergert.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDto> create(@Valid @RequestBody CreateOrderRequestDto requestDto,
                                           @AuthenticationPrincipal JwtClaimsDto claims) {

        log.info("Create order request by user userId={}", claims.userId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.createOrder(requestDto, claims.userId()));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<OrderDto> payOrder(@PathVariable Long id,
                                             @Valid @RequestBody OrderPaymentRequestDto requestDto,
                                             @AuthenticationPrincipal JwtClaimsDto claims) {

        log.info("Paying order with id={}, request={}", id, requestDto);

        return ResponseEntity.ok(orderService.processPayment(id, requestDto, claims.userId()));
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderDto>> getMyOrder(@AuthenticationPrincipal JwtClaimsDto claims) {

        log.info("View orders for user with id = {}", claims.userId());

        return ResponseEntity.ok(orderService.getAllOrdersByUserId(claims.userId()));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderDto> cancelOrder(@PathVariable Long id,
                                                @AuthenticationPrincipal JwtClaimsDto claims) {

        log.info("Cancelling order with id={} by userId={}", id, claims.userId());

        return ResponseEntity.ok(orderService.cancelOrder(id, claims.userId()));
    }
}
