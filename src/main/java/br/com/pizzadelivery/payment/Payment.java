package br.com.pizzadelivery.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Payment {
    private final UUID id;
    private final UUID orderId;
    private final BigDecimal amount;
    private final PaymentMethod method;
    private final PaymentStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Payment(UUID id, UUID orderId, BigDecimal amount, PaymentMethod method,
                   PaymentStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Payment withStatus(PaymentStatus newStatus) {
        return new Payment(id, orderId, amount, method, newStatus, createdAt, Instant.now());
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
