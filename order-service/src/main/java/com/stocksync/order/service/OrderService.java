package com.stocksync.order.service;

import com.stocksync.order.dto.OrderCreatedEvent;
import com.stocksync.order.kafka.OrderEventProducer;
import com.stocksync.order.model.Order;
import com.stocksync.order.model.OrderItem;
import com.stocksync.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer eventProducer;

    @Transactional
    public Order placeOrder(Order order) {
        order.getItems().forEach(item -> item.setOrder(order));
        Order saved = orderRepository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(
                saved.getId(),
                saved.getCustomerId(),
                saved.getChannel().name(),
                saved.getItems().stream()
                        .map(i -> new OrderCreatedEvent.Item(i.getSku(), i.getQuantity()))
                        .collect(Collectors.toList())
        );
        eventProducer.publish(event);
        return saved;
    }

    public List<Order> listOrders() {
        return orderRepository.findAll();
    }

    public Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
    }

    @Transactional
    public Order updateStatus(Long orderId, Order.Status status) {
        Order order = getOrder(orderId);
        order.setStatus(status);
        return orderRepository.save(order);
    }
}
