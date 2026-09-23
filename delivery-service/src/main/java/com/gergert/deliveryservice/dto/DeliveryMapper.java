package com.gergert.deliveryservice.dto;

import com.gergert.deliveryservice.entity.Courier;
import com.gergert.deliveryservice.entity.Delivery;
import org.mapstruct.*;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING
)

public interface DeliveryMapper {

    @Mapping(source = "courier.id", target = "courierId")
    @Mapping(source = "courier",
            target = "courierName",
            qualifiedByName = "buildCourierName"
    )
    DeliveryResponseDto toDeliveryDto(Delivery delivery);

    @Named("buildCourierName")
    default String buildCourierName(Courier courier) {
        if (courier == null) {
            return null;
        }

        String firstName = courier.getFirstName();
        String lastName = courier.getLastName();

        if (firstName == null || firstName.isBlank()) {
            return lastName;
        }

        if (lastName == null || lastName.isBlank()) {
            return firstName;
        }

        return firstName + " " + lastName;
    }
}
