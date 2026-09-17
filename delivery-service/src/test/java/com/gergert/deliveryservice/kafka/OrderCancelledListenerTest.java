package com.gergert.deliveryservice.kafka;

import com.gergert.common.dto.kafka.OrderCancelledEventDto;
import com.gergert.deliveryservice.service.DeliveryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderCancelledListenerTest {

    @Mock
    private DeliveryService deliveryService;

    @InjectMocks
    private OrderCancelledListener listener;

    @Test
    void handle_shouldDelegateCancelledEventToDeliveryService() {
        OrderCancelledEventDto event = new OrderCancelledEventDto(50L);

        listener.handle(event);

        verify(deliveryService).cancelDelivery(event);
    }
}
