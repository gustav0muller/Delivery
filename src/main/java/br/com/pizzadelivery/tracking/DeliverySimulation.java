package br.com.pizzadelivery.tracking;

import br.com.pizzadelivery.domain.OrderStatus;
import br.com.pizzadelivery.events.DeliveryLocationUpdatedEvent;
import br.com.pizzadelivery.events.OrderStatusChangedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class DeliverySimulation {
    private static final int SIMULATION_DURATION_SECONDS = 30;

    private final ConcurrentMap<UUID, Integer> activeDeliveries = new ConcurrentHashMap<>();
    private final ApplicationEventPublisher eventPublisher;

    public DeliverySimulation(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        if (event.getStatus() == OrderStatus.OUT_FOR_DELIVERY) {
            UUID orderId = event.getOrderId();
            DeliveryLocation initial = new DeliveryLocation(orderId, 0, Instant.now());
            if (activeDeliveries.putIfAbsent(orderId, 0) == null) {
                eventPublisher.publishEvent(new DeliveryLocationUpdatedEvent(initial));
            }
        }
    }

    @Scheduled(fixedRate = 1000)
    void advanceDeliveries() {
        for (UUID orderId : activeDeliveries.keySet()) {
            activeDeliveries.computeIfPresent(orderId, (id, elapsed) -> {
                int nextElapsed = Math.min(SIMULATION_DURATION_SECONDS, elapsed + 1);
                double progress = 100.0 * nextElapsed / SIMULATION_DURATION_SECONDS;
                DeliveryLocation updated = new DeliveryLocation(id, progress, Instant.now());
                eventPublisher.publishEvent(new DeliveryLocationUpdatedEvent(updated));
                return nextElapsed < SIMULATION_DURATION_SECONDS ? nextElapsed : null;
            });
        }
    }
}
