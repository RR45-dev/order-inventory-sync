package com.stocksync.inventory.kafka;

import com.stocksync.inventory.dto.InventoryUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryEventProducer {

    public static final String TOPIC = "inventory-updated";

    private final KafkaTemplate<String, InventoryUpdatedEvent> kafkaTemplate;

    public void publish(InventoryUpdatedEvent event) {
        kafkaTemplate.send(TOPIC, event.getOrderId().toString(), event);
    }
}
