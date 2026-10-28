package com.stocksync.inventory.kafka;

import com.stocksync.inventory.dto.OrderCreatedEvent;
import com.stocksync.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final InventoryService inventoryService;

    @KafkaListener(topics = "order-created", groupId = "inventory-service")
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Received order-created event for order {}", event.getOrderId());
        inventoryService.reserveStockForOrder(event);
    }
}
