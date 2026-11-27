package br.com.pizzadelivery.payment;

import br.com.pizzadelivery.events.PaymentApprovedEvent;
import br.com.pizzadelivery.events.PaymentRequestedEvent;
import br.com.pizzadelivery.events.RabbitEventConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SimulatedPaymentProcessor {
    private final ApplicationEventPublisher eventPublisher;

    public SimulatedPaymentProcessor(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    @RabbitListener(queues = RabbitEventConfig.PAYMENT_REQUESTED_QUEUE)
    public void onPaymentRequested(PaymentRequestedEvent event) {
        eventPublisher.publishEvent(
                new PaymentApprovedEvent(event.getOrderId(), event.getAmount()));
    }
}
