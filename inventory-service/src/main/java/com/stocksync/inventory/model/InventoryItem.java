package com.stocksync.inventory.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory_items")
@Data
@NoArgsConstructor
public class InventoryItem {

    @Id
    private String sku;

    private String description;

    private Integer quantityOnHand;

    @Version
    private Long version; // optimistic locking to avoid oversell under concurrent orders
}
