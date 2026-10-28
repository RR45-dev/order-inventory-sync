package com.stocksync.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Published after an order is processed against stock. notification-service
 * consumes this to tell the customer whether their order was confirmed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUpdatedEvent {
    private Long orderId;
    private String customerId;
    private String status; // CONFIRMED or REJECTED
    private String reason; // populated when REJECTED, e.g. "insufficient stock for SKU-123"
}
