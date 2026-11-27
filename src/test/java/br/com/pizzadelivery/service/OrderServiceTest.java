package br.com.pizzadelivery.service;

import br.com.pizzadelivery.domain.OrderStatus;
import br.com.pizzadelivery.domain.DrinkType;
import br.com.pizzadelivery.domain.PizzaOrder;
import br.com.pizzadelivery.domain.PizzaType;
import br.com.pizzadelivery.events.OrderPlacedEvent;
import br.com.pizzadelivery.events.OrderStatusChangedEvent;
import br.com.pizzadelivery.repository.InMemoryOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderServiceTest {
    private InMemoryOrderRepository repository;
    private List<Object> publishedEvents;
    private OrderService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryOrderRepository();
        publishedEvents = new ArrayList<>();
        service = new OrderService(repository, publishedEvents::add);
    }

    @Test
    void placesOrderAndPublishesDomainEvent() {
        PizzaOrder order = service.placeOrder(" Ana ", " Rua das Flores, 10 ", PizzaType.PEPPERONI, 2);

        assertEquals("Ana", order.getCustomerName());
        assertEquals(OrderStatus.PAYMENT_PENDING, repository.findById(order.getId()).get().getStatus());
        assertTrue(publishedEvents.get(0) instanceof OrderPlacedEvent);
    }

    @Test
    void onlyAllowsTheNextStatusInTheDeliveryFlow() {
        PizzaOrder order = service.placeOrder("Ana", "Rua das Flores, 10", PizzaType.MARGHERITA, 1);

        PizzaOrder received = service.changeStatus(order.getId(), OrderStatus.RECEIVED);
        assertEquals(OrderStatus.RECEIVED, received.getStatus());
        PizzaOrder preparing = service.changeStatus(order.getId(), OrderStatus.PREPARING);
        assertEquals(OrderStatus.PREPARING, preparing.getStatus());
        assertTrue(publishedEvents.get(2) instanceof OrderStatusChangedEvent);

        assertThrows(IllegalStateException.class,
                () -> service.changeStatus(order.getId(), OrderStatus.DELIVERED));
    }

    @Test
    void rejectsQuantityOutsideAllowedRange() {
        assertThrows(IllegalArgumentException.class,
                () -> service.placeOrder("Ana", "Rua das Flores, 10", PizzaType.MARGHERITA, 11));
    }

    @Test
    void appliesPizzaPromotionAndAddsDrinkToOrderTotal() {
        PizzaOrder order = service.placeOrder("Ana", "Rua das Flores, 10",
                PizzaType.MARGHERITA, 2, DrinkType.COCA_COLA, 2);

        assertEquals(0, order.getPizzaSubtotal().compareTo(new BigDecimal("79.80")));
        assertEquals(0, order.getDiscount().compareTo(new BigDecimal("7.98")));
        assertEquals(0, order.getDrinkSubtotal().compareTo(new BigDecimal("9.80")));
        assertEquals(0, order.getTotalPrice().compareTo(new BigDecimal("81.62")));
    }

    @Test
    void rejectsDrinkQuantityWithoutASelectedDrink() {
        assertThrows(IllegalArgumentException.class,
                () -> service.placeOrder("Ana", "Rua das Flores, 10",
                        PizzaType.MARGHERITA, 1, null, 1));
    }
}
