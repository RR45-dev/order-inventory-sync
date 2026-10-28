package com.stocksync.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUpdatedEvent {
    private Long orderId;
    private String customerId;
    private String status;
    private String reason;
}
