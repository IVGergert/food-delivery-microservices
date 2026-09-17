package com.gergert.orderservice.service.impl;

import com.gergert.orderservice.dto.MenuItemDto;
import com.gergert.orderservice.dto.MenuMapper;
import com.gergert.orderservice.entity.MenuCategory;
import com.gergert.orderservice.entity.MenuItem;
import com.gergert.orderservice.exception.MenuItemNotFoundException;
import com.gergert.orderservice.repository.MenuItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceImplTest {

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private MenuMapper menuMapper;

    @InjectMocks
    private MenuServiceImpl service;

    @Test
    void getAllItems_shouldReturnMappedMenuItems() {
        MenuItem pizza = menuItem(1L, "Pizza", MenuCategory.PIZZA);
        MenuItem burger = menuItem(2L, "Burger", MenuCategory.BURGERS);

        MenuItemDto pizzaDto = new MenuItemDto(
                1L, "Pizza", new BigDecimal("10.00"), "Pizza description",
                MenuCategory.PIZZA, "Пицца", null
        );
        MenuItemDto burgerDto = new MenuItemDto(
                2L, "Burger", new BigDecimal("8.00"), "Burger description",
                MenuCategory.BURGERS, "Бургеры", null
        );

        when(menuItemRepository.findAll()).thenReturn(List.of(pizza, burger));
        when(menuMapper.toMenuDto(pizza)).thenReturn(pizzaDto);
        when(menuMapper.toMenuDto(burger)).thenReturn(burgerDto);

        List<MenuItemDto> result = service.getAllItems();

        assertThat(result).containsExactly(pizzaDto, burgerDto);
        verify(menuMapper).toMenuDto(pizza);
        verify(menuMapper).toMenuDto(burger);
    }

    @Test
    void getAllItems_shouldReturnEmptyListWhenMenuIsEmpty() {
        when(menuItemRepository.findAll()).thenReturn(List.of());

        assertThat(service.getAllItems()).isEmpty();
        verifyNoInteractions(menuMapper);
    }

    @Test
    void getItemById_shouldReturnMappedItem() {
        MenuItem item = menuItem(1L, "Pizza", MenuCategory.PIZZA);
        MenuItemDto dto = new MenuItemDto(
                1L, "Pizza", new BigDecimal("10.00"), "Pizza description",
                MenuCategory.PIZZA, "Пицца", null
        );

        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(menuMapper.toMenuDto(item)).thenReturn(dto);

        MenuItemDto result = service.getItemById(1L);

        assertThat(result).isSameAs(dto);
        verify(menuMapper).toMenuDto(item);
    }

    @Test
    void getItemById_shouldThrowWhenItemDoesNotExist() {
        when(menuItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getItemById(99L))
                .isInstanceOf(MenuItemNotFoundException.class)
                .hasMessage("Menu item with id `99` not found");

        verifyNoInteractions(menuMapper);
    }

    private MenuItem menuItem(Long id, String name, MenuCategory category) {
        return new MenuItem(
                id,
                name,
                new BigDecimal("10.00"),
                name + " description",
                null,
                category
        );
    }
}
