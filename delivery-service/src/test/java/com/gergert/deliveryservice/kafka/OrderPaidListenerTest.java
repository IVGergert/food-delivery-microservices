package com.gergert.deliveryservice.kafka;

import com.gergert.common.dto.kafka.OrderPaidEventDto;
import com.gergert.deliveryservice.service.DeliveryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderPaidListenerTest {

    @Mock
    private DeliveryService deliveryService;

    @InjectMocks
    private OrderPaidListener listener;

    @Test
    void handle_shouldDelegatePaidEventToDeliveryService() {
        OrderPaidEventDto event = new OrderPaidEventDto(
                50L,
                "Test address",
                new BigDecimal("25.00")
        );

        listener.handle(event);

        verify(deliveryService).createDelivery(event);
    }
}
