package com.stocksync.inventory.service;

import com.stocksync.inventory.dto.InventoryUpdatedEvent;
import com.stocksync.inventory.dto.OrderCreatedEvent;
import com.stocksync.inventory.kafka.InventoryEventProducer;
import com.stocksync.inventory.model.InventoryItem;
import com.stocksync.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryEventProducer eventProducer;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String CACHE_PREFIX = "stock:";

    /**
     * Core of the "no more overselling" guarantee: reserve stock for every
     * line item in the same DB transaction. If any SKU is short, the whole
     * order is rejected (no partial fulfillment) and the reason is reported
     * back on the InventoryUpdatedEvent.
     */
    @Transactional
    public void reserveStockForOrder(OrderCreatedEvent event) {
        for (OrderCreatedEvent.Item item : event.getItems()) {
            Optional<InventoryItem> maybeItem = inventoryRepository.findById(item.getSku());
            if (maybeItem.isEmpty() || maybeItem.get().getQuantityOnHand() < item.getQuantity()) {
                eventProducer.publish(new InventoryUpdatedEvent(
                        event.getOrderId(), event.getCustomerId(), "REJECTED",
                        "insufficient stock for SKU " + item.getSku()));
                return;
            }
        }

        for (OrderCreatedEvent.Item item : event.getItems()) {
            InventoryItem inventoryItem = inventoryRepository.findById(item.getSku()).orElseThrow();
            inventoryItem.setQuantityOnHand(inventoryItem.getQuantityOnHand() - item.getQuantity());
            inventoryRepository.save(inventoryItem);
            refreshCache(inventoryItem);
        }

        eventProducer.publish(new InventoryUpdatedEvent(
                event.getOrderId(), event.getCustomerId(), "CONFIRMED", null));
    }

    public int getQuantityOnHand(String sku) {
        String cached = redisTemplate.opsForValue().get(CACHE_PREFIX + sku);
        if (cached != null) {
            return Integer.parseInt(cached);
        }
        InventoryItem item = inventoryRepository.findById(sku)
                .orElseThrow(() -> new IllegalArgumentException("Unknown SKU: " + sku));
        refreshCache(item);
        return item.getQuantityOnHand();
    }

    public List<InventoryItem> listInventory() {
        return inventoryRepository.findAll();
    }

    public InventoryItem upsert(InventoryItem item) {
        InventoryItem saved = inventoryRepository.save(item);
        refreshCache(saved);
        return saved;
    }

    private void refreshCache(InventoryItem item) {
        redisTemplate.opsForValue().set(
                CACHE_PREFIX + item.getSku(),
                String.valueOf(item.getQuantityOnHand()),
                Duration.ofMinutes(10));
    }
}
