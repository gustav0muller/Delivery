package br.com.pizzadelivery.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitEventBridge {
    private static final Logger log = LoggerFactory.getLogger(RabbitEventBridge.class);
    private final RabbitTemplate rabbitTemplate;

    public RabbitEventBridge(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @EventListener
    public void bridgeOrderPlaced(OrderPlacedEvent event) {
        publish("order.placed", event);
    }

    @EventListener
    public void bridgeOrderStatusChanged(OrderStatusChangedEvent event) {
        publish("order.status.changed", event);
    }

    @EventListener
    public void bridgeDeliveryLocationUpdated(DeliveryLocationUpdatedEvent event) {
        publish("delivery.location.updated", event);
    }

    @EventListener
    public void bridgeCourierMessageSent(CourierMessageSentEvent event) {
        publish("courier.message.sent", event);
    }

    @EventListener
    public void bridgePaymentRequested(PaymentRequestedEvent event) {
        publish("payment.requested", event);
    }

    @EventListener
    public void bridgePaymentApproved(PaymentApprovedEvent event) {
        publish("payment.approved", event);
    }

    private void publish(String routingKey, Object event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitEventConfig.EVENTS_EXCHANGE, routingKey, event);
        } catch (AmqpException ex) {
            log.warn("RabbitMQ indisponivel; evento {} mantido apenas no Spring Events.", routingKey);
        }
    }
}
