package com.stocksync.notification.kafka;

import com.stocksync.notification.dto.InventoryUpdatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * In production this would call an email/SMS/push provider. For this
 * project it logs the outcome so the event flow is easy to demo end-to-end
 * with `docker compose logs -f notification-service`.
 */
@Slf4j
@Component
public class InventoryEventConsumer {

    @KafkaListener(topics = "inventory-updated", groupId = "notification-service")
    public void onInventoryUpdated(InventoryUpdatedEvent event) {
        if ("CONFIRMED".equals(event.getStatus())) {
            log.info("Notify customer {}: order {} CONFIRMED", event.getCustomerId(), event.getOrderId());
        } else {
            log.info("Notify customer {}: order {} REJECTED ({})",
                    event.getCustomerId(), event.getOrderId(), event.getReason());
        }
    }
}
