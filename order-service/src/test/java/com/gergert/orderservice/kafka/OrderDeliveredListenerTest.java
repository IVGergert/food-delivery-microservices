package com.gergert.orderservice.kafka;

import com.gergert.common.dto.kafka.OrderDeliveredEventDto;
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
class OrderDeliveredListenerTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderDeliveredListener listener;

    @Test
    void handle_shouldUpdateOrderToDelivered() {
        Order order = new Order();
        order.setId(50L);
        order.setOrderStatus(OrderStatus.IN_DELIVERY);

        when(orderRepository.findById(50L)).thenReturn(Optional.of(order));

        listener.handle(new OrderDeliveredEventDto(50L));

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.DELIVERED);
        verify(orderRepository).save(order);
    }

    @Test
    void handle_shouldIgnoreAlreadyDeliveredOrder() {
        Order order = new Order();
        order.setId(50L);
        order.setOrderStatus(OrderStatus.DELIVERED);

        when(orderRepository.findById(50L)).thenReturn(Optional.of(order));

        listener.handle(new OrderDeliveredEventDto(50L));

        verify(orderRepository, never()).save(order);
    }

    @Test
    void handle_shouldThrowWhenOrderDoesNotExist() {
        when(orderRepository.findById(50L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listener.handle(new OrderDeliveredEventDto(50L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order with id 50 not found");

        verify(orderRepository, never()).save(any(Order.class));
    }
}
