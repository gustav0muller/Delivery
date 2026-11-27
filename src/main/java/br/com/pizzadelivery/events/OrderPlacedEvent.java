package br.com.pizzadelivery.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class OrderPlacedEvent {
    private final UUID orderId;

    @JsonCreator
    public OrderPlacedEvent(@JsonProperty("orderId") UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getOrderId() {
        return orderId;
    }
}
