package com.stocksync.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Event published to the "order-created" Kafka topic whenever a new order
 * is placed. inventory-service consumes this to reserve stock.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
    private Long orderId;
    private String customerId;
    private String channel;
    private List<Item> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private String sku;
        private Integer quantity;
    }
}
