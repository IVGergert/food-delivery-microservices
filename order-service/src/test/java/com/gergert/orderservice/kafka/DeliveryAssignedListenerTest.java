package com.gergert.orderservice.kafka;

import com.gergert.common.dto.kafka.DeliveryAssignedEventDto;
import com.gergert.orderservice.entity.Order;
import com.gergert.orderservice.entity.OrderStatus;
import com.gergert.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryAssignedListenerTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private DeliveryAssignedListener listener;

    @Test
    void handle_shouldUpdateOrderWithCourierData() {
        Order order = new Order();
        order.setId(50L);
        order.setOrderStatus(OrderStatus.PAID);

        DeliveryAssignedEventDto event = DeliveryAssignedEventDto.builder()
                .orderId(50L)
                .courierId(7L)
                .courierName("Alex")
                .address("Test address")
                .etaMinutes(30)
                .build();

        when(orderRepository.findById(50L)).thenReturn(Optional.of(order));

        listener.handle(event);

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.DELIVERY_ASSIGNED);
        assertThat(order.getCourierName()).isEqualTo("Alex");
        assertThat(order.getEtaMinutes()).isEqualTo(30);
        verify(orderRepository).save(order);
    }

    @Test
    void handle_shouldIgnoreAlreadyAssignedOrder() {
        Order order = new Order();
        order.setId(50L);
        order.setOrderStatus(OrderStatus.DELIVERY_ASSIGNED);

        DeliveryAssignedEventDto event = DeliveryAssignedEventDto.builder()
                .orderId(50L)
                .courierId(7L)
                .courierName("Alex")
                .etaMinutes(30)
                .build();

        when(orderRepository.findById(50L)).thenReturn(Optional.of(order));

        listener.handle(event);

        verify(orderRepository, never()).save(order);
    }

    @Test
    void handle_shouldThrowWhenOrderDoesNotExist() {
        DeliveryAssignedEventDto event = DeliveryAssignedEventDto.builder()
                .orderId(50L)
                .courierId(7L)
                .courierName("Alex")
                .etaMinutes(30)
                .build();

        when(orderRepository.findById(50L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listener.handle(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order with id 50 not found");

        verify(orderRepository, never()).save(any(Order.class));
    }
}
