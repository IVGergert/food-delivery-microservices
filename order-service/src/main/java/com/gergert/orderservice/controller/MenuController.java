package com.gergert.orderservice.controller;

import com.gergert.orderservice.dto.MenuItemDto;
import com.gergert.orderservice.dto.MenuMapper;
import com.gergert.orderservice.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;

    @GetMapping
    public ResponseEntity<List<MenuItemDto>> getAll() {
        return ResponseEntity.ok(menuService.getAllItems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItemDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.getItemById(id));
    }

}
