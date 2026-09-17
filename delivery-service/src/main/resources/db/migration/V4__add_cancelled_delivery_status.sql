ALTER TABLE deliveries
DROP CONSTRAINT deliveries_delivery_status_check;

ALTER TABLE deliveries
    ADD CONSTRAINT deliveries_delivery_status_check
        CHECK (
            delivery_status IN (
                                'WAITING_FOR_COURIER',
                                'COURIER_ASSIGNED',
                                'PICKED_UP',
                                'DELIVERED',
                                'CANCELLED'
                )
            );