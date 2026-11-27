package br.com.pizzadelivery.tracking;

import br.com.pizzadelivery.domain.OrderStatus;
import br.com.pizzadelivery.domain.PizzaOrder;
import br.com.pizzadelivery.domain.PizzaType;
import br.com.pizzadelivery.events.OrderStatusChangedEvent;
import br.com.pizzadelivery.repository.InMemoryOrderRepository;
import br.com.pizzadelivery.service.OrderService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeliveryAutomationTest {
    @Test
    void automaticallyMovesPreparingOrderOutForDeliveryAfterPreparationTime() {
        InMemoryOrderRepository repository = new InMemoryOrderRepository();
        List<Object> events = new ArrayList<>();
        OrderService orderService = new OrderService(repository, events::add);
        DeliveryAutomation automation = new DeliveryAutomation(orderService);

        PizzaOrder order = orderService.placeOrder(
                "Ana", "Rua das Flores, 10", PizzaType.MARGHERITA, 1);
        orderService.changeStatus(order.getId(), OrderStatus.RECEIVED);
        orderService.changeStatus(order.getId(), OrderStatus.PREPARING);
        Instant startedAt = Instant.now();
        automation.onOrderStatusChanged(
                new OrderStatusChangedEvent(order.getId(), OrderStatus.PREPARING));

        automation.advancePreparations(startedAt.plusSeconds(14));
        assertEquals(OrderStatus.PREPARING, orderService.getOrder(order.getId()).getStatus());

        automation.advancePreparations(startedAt.plusSeconds(15));
        assertEquals(OrderStatus.OUT_FOR_DELIVERY,
                orderService.getOrder(order.getId()).getStatus());
    }
}
