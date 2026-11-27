package br.com.pizzadelivery.payment;

import br.com.pizzadelivery.domain.OrderStatus;
import br.com.pizzadelivery.domain.PizzaOrder;
import br.com.pizzadelivery.domain.PizzaType;
import br.com.pizzadelivery.events.OrderPlacedEvent;
import br.com.pizzadelivery.events.PaymentRequestedEvent;
import br.com.pizzadelivery.repository.InMemoryOrderRepository;
import br.com.pizzadelivery.service.OrderService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentServiceTest {
    @Test
    void requestsAndApprovesPaymentOnlyForItsExactOrderTotal() {
        List<Object> events = new ArrayList<>();
        OrderService orderService = new OrderService(new InMemoryOrderRepository(), events::add);
        PaymentService paymentService = new PaymentService(
                new InMemoryPaymentRepository(), orderService, events::add);
        PizzaOrder order = orderService.placeOrder(
                "Ana", "Rua das Flores, 10", PizzaType.MARGHERITA, 1);
        paymentService.createPendingPayment(order.getId());

        Payment processing = paymentService.requestPayment(order.getId());
        assertEquals(PaymentStatus.PROCESSING, processing.getStatus());
        assertEquals(OrderStatus.PAYMENT_PENDING, orderService.getOrder(order.getId()).getStatus());
        assertTrue(events.get(0) instanceof OrderPlacedEvent);
        assertTrue(events.get(1) instanceof PaymentRequestedEvent);

        assertThrows(IllegalArgumentException.class,
                () -> paymentService.approvePayment(order.getId(), new BigDecimal("0.01")));
        assertTrue(paymentService.approvePayment(order.getId(), processing.getAmount()));
        assertFalse(paymentService.approvePayment(order.getId(), processing.getAmount()));
        assertEquals(PaymentStatus.APPROVED,
                paymentService.findByOrderId(order.getId()).get().getStatus());
    }

    @Test
    void doesNotAllowPaymentForAnOrderThatHasAlreadyEnteredPreparation() {
        List<Object> events = new ArrayList<>();
        OrderService orderService = new OrderService(new InMemoryOrderRepository(), events::add);
        PaymentService paymentService = new PaymentService(
                new InMemoryPaymentRepository(), orderService, events::add);
        PizzaOrder order = orderService.placeOrder(
                "Ana", "Rua das Flores, 10", PizzaType.MARGHERITA, 1);
        paymentService.createPendingPayment(order.getId());
        orderService.changeStatus(order.getId(), OrderStatus.RECEIVED);

        assertThrows(IllegalStateException.class,
                () -> paymentService.requestPayment(order.getId()));
    }
}
