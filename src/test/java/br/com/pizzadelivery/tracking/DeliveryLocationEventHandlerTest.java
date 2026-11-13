package br.com.pizzadelivery.tracking;

import br.com.pizzadelivery.domain.OrderStatus;
import br.com.pizzadelivery.domain.PizzaOrder;
import br.com.pizzadelivery.domain.PizzaType;
import br.com.pizzadelivery.events.DeliveryLocationEventHandler;
import br.com.pizzadelivery.events.DeliveryLocationUpdatedEvent;
import br.com.pizzadelivery.repository.InMemoryOrderRepository;
import br.com.pizzadelivery.service.OrderService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeliveryLocationEventHandlerTest {
    @Test
    void arrivingAtDestinationUpdatesTheOrderAndPublishesItsLocation() {
        OrderService orderService = new OrderService(new InMemoryOrderRepository(), event -> { });
        PizzaOrder order = orderService.placeOrder(
                "Ana", "Rua das Flores, 10", PizzaType.PEPPERONI, 1);
        orderService.changeStatus(order.getId(), OrderStatus.PREPARING);
        orderService.changeStatus(order.getId(), OrderStatus.OUT_FOR_DELIVERY);
        DeliveryTrackingRegistry registry = new DeliveryTrackingRegistry();
        DeliveryLocationEventHandler handler =
                new DeliveryLocationEventHandler(registry, orderService);
        DeliveryLocation arrived = new DeliveryLocation(order.getId(), 100, Instant.now());

        handler.onLocationUpdated(new DeliveryLocationUpdatedEvent(arrived));

        assertEquals(OrderStatus.DELIVERED, orderService.getOrder(order.getId()).getStatus());
        assertTrue(registry.findLocation(order.getId()).get().isCompleted());
    }
}
