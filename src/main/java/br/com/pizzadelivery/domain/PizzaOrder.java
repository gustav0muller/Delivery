package br.com.pizzadelivery.domain;

import java.time.Instant;
import java.util.UUID;

public class PizzaOrder {
    private final UUID id;
    private final String customerName;
    private final String address;
    private final PizzaType pizzaType;
    private final int quantity;
    private final OrderStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;

    public PizzaOrder(UUID id, String customerName, String address, PizzaType pizzaType,
                      int quantity, OrderStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.customerName = customerName;
        this.address = address;
        this.pizzaType = pizzaType;
        this.quantity = quantity;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public PizzaOrder withStatus(OrderStatus newStatus) {
        return new PizzaOrder(id, customerName, address, pizzaType, quantity, newStatus,
                createdAt, Instant.now());
    }

    public UUID getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getAddress() {
        return address;
    }

    public PizzaType getPizzaType() {
        return pizzaType;
    }

    public int getQuantity() {
        return quantity;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
