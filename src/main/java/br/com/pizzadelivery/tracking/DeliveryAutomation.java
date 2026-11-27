package br.com.pizzadelivery.tracking;

import br.com.pizzadelivery.domain.OrderStatus;
import br.com.pizzadelivery.events.OrderStatusChangedEvent;
import br.com.pizzadelivery.events.RabbitEventConfig;
import br.com.pizzadelivery.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class DeliveryAutomation {
    private static final long PREPARATION_TIME_SECONDS = 15;

    private final OrderService orderService;
    private final ConcurrentMap<UUID, Instant> preparingOrders = new ConcurrentHashMap<>();

    public DeliveryAutomation(OrderService orderService) {
        this.orderService = orderService;
    }

    @EventListener
    @RabbitListener(queues = RabbitEventConfig.ORDER_STATUS_QUEUE)
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        if (event.getStatus() == OrderStatus.PREPARING) {
            preparingOrders.putIfAbsent(event.getOrderId(), Instant.now());
        } else {
            preparingOrders.remove(event.getOrderId());
        }
    }

    @Scheduled(fixedRate = 5000)
    void advancePreparations() {
        advancePreparations(Instant.now());
    }

    void advancePreparations(Instant now) {
        for (UUID orderId : preparingOrders.keySet()) {
            Instant startedAt = preparingOrders.get(orderId);
            if (startedAt != null
                    && now.getEpochSecond() - startedAt.getEpochSecond() >= PREPARATION_TIME_SECONDS) {
                preparingOrders.remove(orderId);
                try {
                    orderService.advanceOrder(orderId);
                } catch (IllegalStateException | br.com.pizzadelivery.domain.OrderNotFoundException ex) {
                    // The order may have been updated or removed while the timer was running.
                }
            }
        }
    }
}
