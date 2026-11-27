package br.com.pizzadelivery.events;

import br.com.pizzadelivery.domain.OrderStatus;
import br.com.pizzadelivery.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventHandler {
    private static final Logger log = LoggerFactory.getLogger(OrderEventHandler.class);
    private final OrderService orderService;

    public OrderEventHandler(OrderService orderService) {
        this.orderService = orderService;
    }

    @EventListener
    @RabbitListener(queues = RabbitEventConfig.ORDER_PLACED_QUEUE)
    public void onOrderPlaced(OrderPlacedEvent event) {
        orderService.changeStatus(event.getOrderId(), OrderStatus.PREPARING);
    }

    @EventListener
    @RabbitListener(queues = RabbitEventConfig.ORDER_STATUS_QUEUE)
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        log.info("Pedido {} atualizado para {}", event.getOrderId(), event.getStatus());
    }
}
