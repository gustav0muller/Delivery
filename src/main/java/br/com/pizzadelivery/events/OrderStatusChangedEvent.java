package br.com.pizzadelivery.events;

import br.com.pizzadelivery.domain.OrderStatus;

import java.util.UUID;

public class OrderStatusChangedEvent {
    private final UUID orderId;
    private final OrderStatus status;

    public OrderStatusChangedEvent(UUID orderId, OrderStatus status) {
        this.orderId = orderId;
        this.status = status;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public OrderStatus getStatus() {
        return status;
    }
}
