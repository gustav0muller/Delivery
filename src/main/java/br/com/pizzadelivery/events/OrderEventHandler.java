package br.com.pizzadelivery.events;

import br.com.pizzadelivery.domain.OrderStatus;
import br.com.pizzadelivery.domain.OrderNotFoundException;
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
    private final OrderStatusRegistry statusRegistry;

    public OrderEventHandler(OrderService orderService, OrderStatusRegistry statusRegistry) {
        this.orderService = orderService;
        this.statusRegistry = statusRegistry;
    }

    @EventListener
    @RabbitListener(queues = RabbitEventConfig.ORDER_STATUS_QUEUE)
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        log.info("Pedido {} atualizado para {}", event.getOrderId(), event.getStatus());
        statusRegistry.publish(event);
        if (event.getStatus() == OrderStatus.RECEIVED) {
            try {
                if (orderService.getOrder(event.getOrderId()).getStatus() == OrderStatus.RECEIVED) {
                    orderService.changeStatus(event.getOrderId(), OrderStatus.PREPARING);
                }
            } catch (OrderNotFoundException ex) {
                log.debug("Ignorando atualização para pedido inexistente {}", event.getOrderId());
            }
        }
    }
}
