package br.com.pizzadelivery.events;

import java.util.UUID;

public class OrderPlacedEvent {
    private final UUID orderId;

    public OrderPlacedEvent(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getOrderId() {
        return orderId;
    }
}
