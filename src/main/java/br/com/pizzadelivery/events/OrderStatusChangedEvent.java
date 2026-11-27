package br.com.pizzadelivery.events;

import br.com.pizzadelivery.domain.OrderStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class OrderStatusChangedEvent {
    private final UUID orderId;
    private final OrderStatus status;

    @JsonCreator
    public OrderStatusChangedEvent(@JsonProperty("orderId") UUID orderId,
                                   @JsonProperty("status") OrderStatus status) {
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
