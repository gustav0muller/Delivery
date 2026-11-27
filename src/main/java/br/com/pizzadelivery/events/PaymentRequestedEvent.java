package br.com.pizzadelivery.events;

import br.com.pizzadelivery.payment.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentRequestedEvent {
    private final UUID orderId;
    private final BigDecimal amount;
    private final PaymentMethod method;

    @JsonCreator
    public PaymentRequestedEvent(@JsonProperty("orderId") UUID orderId,
                                 @JsonProperty("amount") BigDecimal amount,
                                 @JsonProperty("method") PaymentMethod method) {
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
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
}
