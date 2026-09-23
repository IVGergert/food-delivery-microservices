package com.gergert.orderservice.service.impl;

import com.gergert.orderservice.dto.MenuItemDto;
import com.gergert.orderservice.dto.MenuMapper;
import com.gergert.orderservice.entity.MenuItem;
import com.gergert.orderservice.exception.MenuItemNotFoundException;
import com.gergert.orderservice.repository.MenuItemRepository;
import com.gergert.orderservice.service.MenuService;
import org.springframework.cache.annotation.Cacheable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {
    private final MenuItemRepository menuItemRepository;
    private final MenuMapper menuMapper;

    @Override
    @Cacheable("menu")
    @Transactional(readOnly = true)
    public List<MenuItemDto> getAllItems() {
        return menuItemRepository.findAll()
                .stream()
                .map(menuMapper::toMenuDto)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    @Cacheable(value = "menu", key = "#id")
    @Transactional(readOnly = true)
    public MenuItemDto getItemById(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() ->
                        new MenuItemNotFoundException(
                                "Menu item with id `%s` not found"
                                        .formatted(id)
                        )
                );

        return menuMapper.toMenuDto(menuItem);
    }

}
