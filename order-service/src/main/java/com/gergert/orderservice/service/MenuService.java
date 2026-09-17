package com.gergert.orderservice.service;

import com.gergert.orderservice.dto.MenuItemDto;

import java.util.List;

public interface MenuService {
    List<MenuItemDto> getAllItems();
    MenuItemDto getItemById(Long id);
}
