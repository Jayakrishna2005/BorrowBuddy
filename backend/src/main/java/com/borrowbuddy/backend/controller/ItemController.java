package com.borrowbuddy.backend.controller;
import com.borrowbuddy.backend.model.Item;
import com.borrowbuddy.backend.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/items")
public class ItemController {
    @Autowired
    private ItemRepository itemRepository;

    @GetMapping
    public List<Item> getAvailableItems() {
        return itemRepository.findByIsAvailableTrue();
    }
    
    @PostMapping
    public Item createItem(@RequestBody Item item) {
        return itemRepository.save(item);
    }
}