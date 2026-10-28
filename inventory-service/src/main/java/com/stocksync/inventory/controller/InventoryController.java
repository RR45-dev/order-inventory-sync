package com.stocksync.inventory.controller;

import com.stocksync.inventory.model.InventoryItem;
import com.stocksync.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public List<InventoryItem> listInventory() {
        return inventoryService.listInventory();
    }

    @GetMapping("/{sku}/quantity")
    public Map<String, Object> getQuantity(@PathVariable String sku) {
        return Map.of("sku", sku, "quantityOnHand", inventoryService.getQuantityOnHand(sku));
    }

    @PutMapping
    public InventoryItem upsert(@RequestBody InventoryItem item) {
        return inventoryService.upsert(item);
    }
}
