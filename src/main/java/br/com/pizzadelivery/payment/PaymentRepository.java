package br.com.pizzadelivery.payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    Optional<Payment> findByOrderId(UUID orderId);

    Payment saveIfAbsent(Payment payment);

    Payment save(Payment payment);
}
