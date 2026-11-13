package br.com.pizzadelivery.tracking;

import br.com.pizzadelivery.domain.OrderStatus;
import br.com.pizzadelivery.events.DeliveryLocationUpdatedEvent;
import br.com.pizzadelivery.events.OrderStatusChangedEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeliverySimulationTest {
    @Test
    void movesCourierToDestinationAndPublishesLocationEvents() {
        List<Object> events = new ArrayList<>();
        DeliverySimulation simulation = new DeliverySimulation(events::add);
        UUID orderId = UUID.randomUUID();

        simulation.onOrderStatusChanged(new OrderStatusChangedEvent(
                orderId, OrderStatus.OUT_FOR_DELIVERY));

        DeliveryLocation initial = ((DeliveryLocationUpdatedEvent) events.get(0)).getLocation();
        assertEquals(0, initial.getProgress(), 0.001);
        assertEquals(30, initial.getEtaSeconds());
        assertEquals(2.4, initial.getDistanceKm(), 0.001);

        simulation.advanceDeliveries();
        DeliveryLocation moving = ((DeliveryLocationUpdatedEvent) events.get(1)).getLocation();
        assertTrue(moving.getProgress() > initial.getProgress());
        assertTrue(moving.getDistanceKm() < initial.getDistanceKm());

        for (int tick = 1; tick < 30; tick++) {
            simulation.advanceDeliveries();
        }

        List<DeliveryLocation> updates = events.stream()
                .filter(DeliveryLocationUpdatedEvent.class::isInstance)
                .map(DeliveryLocationUpdatedEvent.class::cast)
                .map(DeliveryLocationUpdatedEvent::getLocation)
                .collect(Collectors.toList());
        assertEquals(31, updates.size());
        assertTrue(updates.get(updates.size() - 1).isCompleted());
        assertEquals(0, updates.get(updates.size() - 1).getEtaSeconds());

        simulation.advanceDeliveries();
        assertEquals(31, events.size());
    }

    @Test
    void doesNotStartTrackingBeforeTheOrderIsOutForDelivery() {
        List<Object> events = new ArrayList<>();
        DeliverySimulation simulation = new DeliverySimulation(events::add);

        simulation.onOrderStatusChanged(new OrderStatusChangedEvent(
                UUID.randomUUID(), OrderStatus.PREPARING));
        simulation.advanceDeliveries();

        assertTrue(events.isEmpty());
    }

    @Test
    void locationMarksTheRideCompleteOnlyAtItsDestination() {
        UUID orderId = UUID.randomUUID();

        assertFalse(new DeliveryLocation(orderId, 99, java.time.Instant.now()).isCompleted());
        assertTrue(new DeliveryLocation(orderId, 100, java.time.Instant.now()).isCompleted());
    }
}
