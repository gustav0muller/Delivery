package br.com.pizzadelivery.events;

import br.com.pizzadelivery.domain.OrderStatus;
import br.com.pizzadelivery.domain.PizzaOrder;
import br.com.pizzadelivery.service.OrderService;
import br.com.pizzadelivery.tracking.DeliveryLocation;
import br.com.pizzadelivery.tracking.DeliveryTrackingRegistry;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DeliveryLocationEventHandler {
    private final DeliveryTrackingRegistry trackingRegistry;
    private final OrderService orderService;

    public DeliveryLocationEventHandler(DeliveryTrackingRegistry trackingRegistry,
                                        OrderService orderService) {
        this.trackingRegistry = trackingRegistry;
        this.orderService = orderService;
    }

    @EventListener
    @RabbitListener(queues = RabbitEventConfig.DELIVERY_LOCATION_QUEUE)
    public void onLocationUpdated(DeliveryLocationUpdatedEvent event) {
        DeliveryLocation location = event.getLocation();
        trackingRegistry.publish(location);

        if (location.isCompleted()) {
            PizzaOrder order = orderService.getOrder(location.getOrderId());
            if (order.getStatus() == OrderStatus.OUT_FOR_DELIVERY) {
                orderService.changeStatus(order.getId(), OrderStatus.DELIVERED);
            }
        }
    }
}
