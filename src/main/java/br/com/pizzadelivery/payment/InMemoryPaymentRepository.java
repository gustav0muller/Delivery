package br.com.pizzadelivery.payment;

import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InMemoryPaymentRepository implements PaymentRepository {
    private final ConcurrentMap<UUID, Payment> paymentsByOrder = new ConcurrentHashMap<>();

    @Override
    public Optional<Payment> findByOrderId(UUID orderId) {
        return Optional.ofNullable(paymentsByOrder.get(orderId));
    }

    @Override
    public Payment saveIfAbsent(Payment payment) {
        Payment existing = paymentsByOrder.putIfAbsent(payment.getOrderId(), payment);
        return existing == null ? payment : existing;
    }

    @Override
    public Payment save(Payment payment) {
        paymentsByOrder.put(payment.getOrderId(), payment);
        return payment;
    }
}
