package com.gergert.orderservice.validation;

import com.gergert.common.dto.OrderPaymentRequestDto;
import com.gergert.common.enums.PaymentMethod;
import com.gergert.orderservice.dto.CreateOrderRequestDto;
import com.gergert.orderservice.dto.OrderItemRequestDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RequestDtoValidationTest {

    private Validator validator;
    private jakarta.validation.ValidatorFactory validatorFactory;

    @BeforeAll
    void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    void tearDown() {
        validatorFactory.close();
    }

    @Test
    void createOrder_shouldAcceptValidRequest() {
        CreateOrderRequestDto request = new CreateOrderRequestDto(
                "Test address",
                Set.of(new OrderItemRequestDto(1L, 2))
        );

        Set<ConstraintViolation<CreateOrderRequestDto>> violations =
                validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void createOrder_shouldRejectBlankAddress() {
        CreateOrderRequestDto request = new CreateOrderRequestDto(
                "",
                Set.of(new OrderItemRequestDto(1L, 1))
        );

        assertThat(validator.validate(request))
                .extracting(ConstraintViolation::getMessage)
                .contains("Address cannot be empty");
    }

    @Test
    void createOrder_shouldRejectAddressLongerThan255Characters() {
        String longAddress = "a".repeat(256);

        CreateOrderRequestDto request = new CreateOrderRequestDto(
                longAddress,
                Set.of(new OrderItemRequestDto(1L, 1))
        );

        assertThat(validator.validate(request))
                .extracting(ConstraintViolation::getMessage)
                .contains("Address must not exceed 255 characters");
    }

    @Test
    void createOrder_shouldRejectEmptyItems() {
        CreateOrderRequestDto request = new CreateOrderRequestDto(
                "Test address",
                Set.of()
        );

        assertThat(validator.validate(request))
                .extracting(ConstraintViolation::getMessage)
                .contains("Order must contain at least one item");
    }

    @Test
    void createOrder_shouldRejectInvalidItemIdAndQuantity() {
        CreateOrderRequestDto request = new CreateOrderRequestDto(
                "Test address",
                Set.of(new OrderItemRequestDto(0L, 0))
        );

        assertThat(validator.validate(request))
                .extracting(ConstraintViolation::getMessage)
                .contains(
                        "Item ID must be positive",
                        "Quantity must be positive"
                );
    }

    @Test
    void createOrder_shouldRejectNullItemIdAndQuantity() {
        CreateOrderRequestDto request = new CreateOrderRequestDto(
                "Test address",
                Set.of(new OrderItemRequestDto(null, null))
        );

        assertThat(validator.validate(request))
                .extracting(ConstraintViolation::getMessage)
                .contains(
                        "Item ID must not be null",
                        "Quantity must not be null"
                );
    }

    @Test
    void createOrder_shouldValidateNestedItemDto() {
        CreateOrderRequestDto request = new CreateOrderRequestDto(
                "Test address",
                Set.of(new OrderItemRequestDto(-1L, -1))
        );

        assertThat(validator.validate(request))
                .extracting(ConstraintViolation::getMessage)
                .contains(
                        "Item ID must be positive",
                        "Quantity must be positive"
                );
    }

    @Test
    void orderPayment_shouldAcceptPaymentMethod() {
        OrderPaymentRequestDto request = new OrderPaymentRequestDto(PaymentMethod.CARD);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void orderPayment_shouldRejectNullPaymentMethod() {
        OrderPaymentRequestDto request = new OrderPaymentRequestDto(null);

        assertThat(validator.validate(request))
                .extracting(ConstraintViolation::getMessage)
                .contains("Payment method must not be null");
    }

}
