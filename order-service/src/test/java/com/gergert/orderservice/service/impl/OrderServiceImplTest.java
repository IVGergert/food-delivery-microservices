package com.gergert.orderservice.service.impl;

import com.gergert.common.dto.CreatePaymentRequestDto;
import com.gergert.common.dto.CreatePaymentResponseDto;
import com.gergert.common.dto.OrderPaymentRequestDto;
import com.gergert.common.dto.kafka.OrderCancelledEventDto;
import com.gergert.common.dto.kafka.OrderPaidEventDto;
import com.gergert.common.enums.PaymentMethod;
import com.gergert.common.enums.PaymentStatus;
import com.gergert.orderservice.client.PaymentHttpClient;
import com.gergert.orderservice.dto.CreateOrderRequestDto;
import com.gergert.orderservice.dto.OrderDto;
import com.gergert.orderservice.dto.OrderItemRequestDto;
import com.gergert.orderservice.dto.OrderMapper;
import com.gergert.orderservice.entity.MenuCategory;
import com.gergert.orderservice.entity.MenuItem;
import com.gergert.orderservice.entity.Order;
import com.gergert.orderservice.entity.OrderItem;
import com.gergert.orderservice.entity.OrderStatus;
import com.gergert.orderservice.exception.InvalidOrderStatusException;
import com.gergert.orderservice.exception.MenuItemNotFoundException;
import com.gergert.orderservice.exception.OrderAccessDeniedException;
import com.gergert.orderservice.exception.OrderNotFoundException;
import com.gergert.orderservice.repository.MenuItemRepository;
import com.gergert.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private PaymentHttpClient paymentHttpClient;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private OrderServiceImpl service;

    private Order order;
    private OrderDto response;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                service,
                "orderPaidEventTopic",
                "order-paid-topic"
        );
        ReflectionTestUtils.setField(
                service,
                "orderCancelledEventTopic",
                "order-cancelled-topic"
        );

        order = new Order();
        order.setId(50L);
        order.setCustomerId(100L);
        order.setAddress("Test address");
        order.setTotalAmount(new BigDecimal("25.00"));
        order.setOrderStatus(OrderStatus.PENDING_PAYMENT);
        order.setItems(new LinkedHashSet<>());

        response = new OrderDto(
                50L,
                100L,
                "Test address",
                new BigDecimal("25.00"),
                OrderStatus.PENDING_PAYMENT,
                null,
                null,
                Set.of()
        );
    }

    @Test
    void createOrder_shouldCalculatePriceSetCustomerAndPendingStatus() {
        OrderItem firstItem = new OrderItem();
        firstItem.setItemId(1L);
        firstItem.setQuantity(2);

        OrderItem secondItem = new OrderItem();
        secondItem.setItemId(2L);
        secondItem.setQuantity(1);

        order.setItems(new LinkedHashSet<>(List.of(firstItem, secondItem)));

        MenuItem pizza = new MenuItem(
                1L,
                "Pizza",
                new BigDecimal("10.00"),
                "Pizza description",
                null,
                MenuCategory.PIZZA
        );

        MenuItem sushi = new MenuItem(
                2L,
                "Sushi",
                new BigDecimal("5.00"),
                "Sushi description",
                null,
                MenuCategory.SUSHI
        );

        CreateOrderRequestDto request = new CreateOrderRequestDto(
                "Test address",
                Set.of(
                        new OrderItemRequestDto(1L, 2),
                        new OrderItemRequestDto(2L, 1)
                )
        );

        when(orderMapper.toEntity(request)).thenReturn(order);
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(pizza));
        when(menuItemRepository.findById(2L)).thenReturn(Optional.of(sushi));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toOrderDto(order)).thenReturn(response);

        OrderDto result = service.createOrder(request, 100L);

        assertThat(result).isSameAs(response);
        assertThat(order.getCustomerId()).isEqualTo(100L);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("25.00");

        assertThat(firstItem.getItemName()).isEqualTo("Pizza");
        assertThat(firstItem.getPriceAtPurchase()).isEqualByComparingTo("10.00");
        assertThat(firstItem.getOrder()).isSameAs(order);

        assertThat(secondItem.getItemName()).isEqualTo("Sushi");
        assertThat(secondItem.getPriceAtPurchase()).isEqualByComparingTo("5.00");
        assertThat(secondItem.getOrder()).isSameAs(order);

        verify(orderRepository).save(order);
        verify(orderMapper).toOrderDto(order);
    }

    @Test
    void createOrder_shouldThrowWhenMenuItemDoesNotExist() {
        OrderItem item = new OrderItem();
        item.setItemId(99L);
        item.setQuantity(1);
        order.setItems(new LinkedHashSet<>(Set.of(item)));

        CreateOrderRequestDto request = new CreateOrderRequestDto(
                "Test address",
                Set.of(new OrderItemRequestDto(99L, 1))
        );

        when(orderMapper.toEntity(request)).thenReturn(order);
        when(menuItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createOrder(request, 100L))
                .isInstanceOf(MenuItemNotFoundException.class)
                .hasMessage("Menu item with id `99` not found");

        verify(orderRepository, never()).save(any(Order.class));
        verifyNoInteractions(kafkaTemplate);
        verify(orderMapper, never()).toOrderDto(any(Order.class));
    }

    @Test
    void processPayment_shouldHandleCashPayment() {
        OrderPaymentRequestDto request = new OrderPaymentRequestDto(PaymentMethod.CASH);

        when(orderRepository.findById(50L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toOrderDto(order)).thenReturn(response);

        OrderDto result = service.processPayment(50L, request, 100L);

        assertThat(result).isSameAs(response);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CASH_ON_DELIVERY);

        verify(orderRepository).save(order);
        verifyNoInteractions(paymentHttpClient);
        verify(kafkaTemplate).send(
                eq("order-paid-topic"),
                eq("50"),
                any(OrderPaidEventDto.class)
        );
    }

    @Test
    void processPayment_shouldHandleSuccessfulCardPayment() {
        OrderPaymentRequestDto request = new OrderPaymentRequestDto(PaymentMethod.CARD);

        CreatePaymentResponseDto paymentResponse = new CreatePaymentResponseDto(
                1L,
                50L,
                new BigDecimal("25.00"),
                PaymentStatus.PAYMENT_SUCCEEDED,
                PaymentMethod.CARD
        );

        when(orderRepository.findById(50L)).thenReturn(Optional.of(order));
        when(paymentHttpClient.createPayment(any(CreatePaymentRequestDto.class)))
                .thenReturn(paymentResponse);
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toOrderDto(order)).thenReturn(response);

        OrderDto result = service.processPayment(50L, request, 100L);

        assertThat(result).isSameAs(response);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAID);

        ArgumentCaptor<CreatePaymentRequestDto> paymentCaptor =
                ArgumentCaptor.forClass(CreatePaymentRequestDto.class);

        verify(paymentHttpClient).createPayment(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().orderId()).isEqualTo(50L);
        assertThat(paymentCaptor.getValue().paymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(paymentCaptor.getValue().amount()).isEqualByComparingTo("25.00");

        verify(kafkaTemplate).send(
                eq("order-paid-topic"),
                eq("50"),
                any(OrderPaidEventDto.class)
        );
    }

    @Test
    void processPayment_shouldMarkOrderAsPaymentFailed() {
        OrderPaymentRequestDto request = new OrderPaymentRequestDto(PaymentMethod.CARD);

        CreatePaymentResponseDto paymentResponse = new CreatePaymentResponseDto(
                1L,
                50L,
                new BigDecimal("25.00"),
                PaymentStatus.PAYMENT_FAILED,
                PaymentMethod.CARD
        );

        when(orderRepository.findById(50L)).thenReturn(Optional.of(order));
        when(paymentHttpClient.createPayment(any(CreatePaymentRequestDto.class)))
                .thenReturn(paymentResponse);
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toOrderDto(order)).thenReturn(response);

        service.processPayment(50L, request, 100L);

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAYMENT_FAILED);
        verify(kafkaTemplate, never()).send(
                eq("order-paid-topic"),
                eq("50"),
                any(OrderPaidEventDto.class)
        );
    }

    @Test
    void processPayment_shouldRejectPaymentForAnotherCustomer() {
        when(orderRepository.findById(50L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.processPayment(
                50L,
                new OrderPaymentRequestDto(PaymentMethod.CARD),
                999L
        ))
                .isInstanceOf(OrderAccessDeniedException.class)
                .hasMessage("You can only pay for your own orders");

        verifyNoInteractions(paymentHttpClient);
        verify(orderRepository, never()).save(any(Order.class));
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void processPayment_shouldRejectWrongOrderStatus() {
        order.setOrderStatus(OrderStatus.PAID);
        when(orderRepository.findById(50L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.processPayment(
                50L,
                new OrderPaymentRequestDto(PaymentMethod.CARD),
                100L
        ))
                .isInstanceOf(InvalidOrderStatusException.class)
                .hasMessage("Order must be in orderStatus PENDING_PAYMENT");

        verifyNoInteractions(paymentHttpClient);
        verify(orderRepository, never()).save(any(Order.class));
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void processPayment_shouldThrowWhenOrderDoesNotExist() {
        when(orderRepository.findById(50L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.processPayment(
                50L,
                new OrderPaymentRequestDto(PaymentMethod.CARD),
                100L
        ))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Entity with id `50` not found");

        verifyNoInteractions(paymentHttpClient);
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void getAllOrdersByUserId_shouldReturnMappedOrders() {
        Order secondOrder = new Order();
        secondOrder.setId(51L);
        secondOrder.setCustomerId(100L);
        secondOrder.setOrderStatus(OrderStatus.PAID);
        secondOrder.setItems(new LinkedHashSet<>());

        List<Order> orders = List.of(order, secondOrder);
        List<OrderDto> dtoList = List.of(response);

        when(orderRepository.findAllByCustomerId(100L)).thenReturn(orders);
        when(orderMapper.toOrderDto(orders)).thenReturn(dtoList);

        List<OrderDto> result = service.getAllOrdersByUserId(100L);

        assertThat(result).containsExactlyElementsOf(dtoList);
        verify(orderMapper).toOrderDto(orders);
    }

    @Test
    void getAllOrdersByUserId_shouldReturnEmptyList() {
        when(orderRepository.findAllByCustomerId(100L)).thenReturn(List.of());
        when(orderMapper.toOrderDto(List.of())).thenReturn(List.of());

        assertThat(service.getAllOrdersByUserId(100L)).isEmpty();
    }

    @Test
    void cancelOrder_shouldCancelPendingPaymentOrderAndPublishEvent() {
        when(orderRepository.findById(50L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toOrderDto(order)).thenReturn(response);

        OrderDto result = service.cancelOrder(50L, 100L);

        assertThat(result).isSameAs(response);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);

        ArgumentCaptor<OrderCancelledEventDto> eventCaptor =
                ArgumentCaptor.forClass(OrderCancelledEventDto.class);

        verify(kafkaTemplate).send(
                eq("order-cancelled-topic"),
                eq("50"),
                eventCaptor.capture()
        );

        assertThat(eventCaptor.getValue().orderId()).isEqualTo(50L);
        verify(orderRepository).save(order);
        verify(orderMapper).toOrderDto(order);
    }

    @Test
    void cancelOrder_shouldCancelCashOnDeliveryOrderAndPublishEvent() {
        order.setOrderStatus(OrderStatus.CASH_ON_DELIVERY);

        when(orderRepository.findById(50L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toOrderDto(order)).thenReturn(response);

        service.cancelOrder(50L, 100L);

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderRepository).save(order);
        verify(kafkaTemplate).send(
                eq("order-cancelled-topic"),
                eq("50"),
                any(OrderCancelledEventDto.class)
        );
    }

    @Test
    void cancelOrder_shouldRejectOrderBelongingToAnotherCustomer() {
        when(orderRepository.findById(50L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.cancelOrder(50L, 999L))
                .isInstanceOf(OrderAccessDeniedException.class)
                .hasMessage("You can only cancel your own orders");

        verify(orderRepository, never()).save(any(Order.class));
        verifyNoInteractions(kafkaTemplate);
        verifyNoInteractions(orderMapper);
    }

    @ParameterizedTest
    @EnumSource(
            value = OrderStatus.class,
            names = {
                    "PAID",
                    "DELIVERY_ASSIGNED",
                    "IN_DELIVERY",
                    "DELIVERED",
                    "PAYMENT_FAILED",
                    "CANCELLED"
            }
    )
    void cancelOrder_shouldRejectNonCancellableStatuses(OrderStatus status) {
        order.setOrderStatus(status);
        when(orderRepository.findById(50L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.cancelOrder(50L, 100L))
                .isInstanceOf(InvalidOrderStatusException.class)
                .hasMessage("Order cannot be cancelled in current status");

        assertThat(order.getOrderStatus()).isEqualTo(status);
        verify(orderRepository, never()).save(any(Order.class));
        verifyNoInteractions(kafkaTemplate);
        verifyNoInteractions(orderMapper);
    }

    @Test
    void cancelOrder_shouldThrowWhenOrderDoesNotExist() {
        when(orderRepository.findById(50L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancelOrder(50L, 100L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Entity with id `50` not found");

        verify(orderRepository, never()).save(any(Order.class));
        verifyNoInteractions(kafkaTemplate);
        verifyNoInteractions(orderMapper);
    }
}
