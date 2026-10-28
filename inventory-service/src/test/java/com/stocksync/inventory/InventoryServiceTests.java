package com.stocksync.inventory;

import com.stocksync.inventory.dto.OrderCreatedEvent;
import com.stocksync.inventory.kafka.InventoryEventProducer;
import com.stocksync.inventory.model.InventoryItem;
import com.stocksync.inventory.repository.InventoryRepository;
import com.stocksync.inventory.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryServiceTests {

    private InventoryRepository repository;
    private InventoryEventProducer producer;
    private RedisTemplate<String, String> redisTemplate;
    private InventoryService service;

    @BeforeEach
    void setUp() {
        repository = mock(InventoryRepository.class);
        producer = mock(InventoryEventProducer.class);
        redisTemplate = mock(RedisTemplate.class);
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        service = new InventoryService(repository, producer, redisTemplate);
    }

    @Test
    void rejectsOrderWhenStockInsufficient() {
        InventoryItem item = new InventoryItem();
        item.setSku("SKU-1");
        item.setQuantityOnHand(1);
        when(repository.findById("SKU-1")).thenReturn(Optional.of(item));

        OrderCreatedEvent event = new OrderCreatedEvent(
                1L, "cust-1", "WEB",
                List.of(new OrderCreatedEvent.Item("SKU-1", 5)));

        service.reserveStockForOrder(event);

        verify(repository, never()).save(any());
        verify(producer).publish(argThat(e -> e.getStatus().equals("REJECTED")));
    }

    @Test
    void confirmsOrderAndDecrementsStockWhenAvailable() {
        InventoryItem item = new InventoryItem();
        item.setSku("SKU-1");
        item.setQuantityOnHand(10);
        when(repository.findById("SKU-1")).thenReturn(Optional.of(item));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderCreatedEvent event = new OrderCreatedEvent(
                2L, "cust-2", "WEB",
                List.of(new OrderCreatedEvent.Item("SKU-1", 3)));

        service.reserveStockForOrder(event);

        assertEquals(7, item.getQuantityOnHand());
        verify(producer).publish(argThat(e -> e.getStatus().equals("CONFIRMED")));
    }
}
