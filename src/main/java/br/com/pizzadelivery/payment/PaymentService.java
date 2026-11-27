package br.com.pizzadelivery.payment;

import br.com.pizzadelivery.domain.OrderStatus;
import br.com.pizzadelivery.domain.PizzaOrder;
import br.com.pizzadelivery.events.PaymentRequestedEvent;
import br.com.pizzadelivery.service.OrderService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final ApplicationEventPublisher eventPublisher;

    public PaymentService(PaymentRepository paymentRepository, OrderService orderService,
                          ApplicationEventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.orderService = orderService;
        this.eventPublisher = eventPublisher;
    }

    public synchronized Payment createPendingPayment(UUID orderId) {
        Optional<Payment> existing = paymentRepository.findByOrderId(orderId);
        if (existing.isPresent()) {
            return existing.get();
        }

        PizzaOrder order = orderService.getOrder(orderId);
        if (order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            throw new IllegalStateException("O pedido não está aguardando pagamento.");
        }

        Instant now = Instant.now();
        Payment payment = new Payment(UUID.randomUUID(), orderId, order.getTotalPrice(),
                PaymentMethod.PIX_SIMULATED, PaymentStatus.PENDING, now, now);
        return paymentRepository.saveIfAbsent(payment);
    }

    public Optional<Payment> findByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    public Payment requestPayment(UUID orderId) {
        PaymentRequestedEvent event;
        synchronized (this) {
            Payment payment = getPayment(orderId);
            if (payment.getStatus() == PaymentStatus.APPROVED) {
                return payment;
            }
            if (orderService.getOrder(orderId).getStatus() != OrderStatus.PAYMENT_PENDING) {
                throw new IllegalStateException("O pedido não está aguardando pagamento.");
            }

            if (payment.getStatus() == PaymentStatus.PENDING) {
                paymentRepository.save(payment.withStatus(PaymentStatus.PROCESSING));
            }
            event = new PaymentRequestedEvent(orderId, payment.getAmount(), payment.getMethod());
        }
        eventPublisher.publishEvent(event);
        return getPayment(orderId);
    }

    public synchronized boolean approvePayment(UUID orderId, BigDecimal amount) {
        Payment payment = getPayment(orderId);
        if (payment.getStatus() != PaymentStatus.PROCESSING) {
            return false;
        }
        if (amount == null || payment.getAmount().compareTo(amount) != 0) {
            throw new IllegalArgumentException("O valor aprovado não corresponde ao total do pedido.");
        }
        paymentRepository.save(payment.withStatus(PaymentStatus.APPROVED));
        return true;
    }

    private Payment getPayment(UUID orderId) {
        return paymentRepository.findByOrderId(orderId).orElseThrow(
                () -> new IllegalStateException("Pagamento do pedido não encontrado."));
    }
}
