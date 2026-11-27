package br.com.pizzadelivery.events;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitEventConfig {
    public static final String EVENTS_EXCHANGE = "pizzadelivery.events";
    public static final String DEAD_LETTER_EXCHANGE = "pizzadelivery.dlx";
    public static final String ORDER_PLACED_QUEUE = "pizzadelivery.order.placed";
    public static final String ORDER_STATUS_QUEUE = "pizzadelivery.order.status";
    public static final String DELIVERY_LOCATION_QUEUE = "pizzadelivery.delivery.location";
    public static final String COURIER_MESSAGE_QUEUE = "pizzadelivery.courier.message";
    public static final String PAYMENT_REQUESTED_QUEUE = "pizzadelivery.payment.requested";
    public static final String PAYMENT_APPROVED_QUEUE = "pizzadelivery.payment.approved";
    public static final String DEAD_LETTER_QUEUE = "pizzadelivery.dead-letter";

    @Bean
    TopicExchange eventsExchange() {
        return new TopicExchange(EVENTS_EXCHANGE, true, false);
    }

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    @Bean
    Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("#");
    }

    @Bean
    Queue orderPlacedQueue() {
        return eventQueue(ORDER_PLACED_QUEUE);
    }

    @Bean
    Queue orderStatusQueue() {
        return eventQueue(ORDER_STATUS_QUEUE);
    }

    @Bean
    Queue deliveryLocationQueue() {
        return eventQueue(DELIVERY_LOCATION_QUEUE);
    }

    @Bean
    Queue courierMessageQueue() {
        return eventQueue(COURIER_MESSAGE_QUEUE);
    }

    @Bean
    Queue paymentRequestedQueue() {
        return eventQueue(PAYMENT_REQUESTED_QUEUE);
    }

    @Bean
    Queue paymentApprovedQueue() {
        return eventQueue(PAYMENT_APPROVED_QUEUE);
    }

    @Bean
    Binding orderPlacedBinding() {
        return BindingBuilder.bind(orderPlacedQueue())
                .to(eventsExchange())
                .with("order.placed");
    }

    @Bean
    Binding orderStatusBinding() {
        return BindingBuilder.bind(orderStatusQueue())
                .to(eventsExchange())
                .with("order.status.changed");
    }

    @Bean
    Binding deliveryLocationBinding() {
        return BindingBuilder.bind(deliveryLocationQueue())
                .to(eventsExchange())
                .with("delivery.location.updated");
    }

    @Bean
    Binding courierMessageBinding() {
        return BindingBuilder.bind(courierMessageQueue())
                .to(eventsExchange())
                .with("courier.message.sent");
    }

    @Bean
    Binding paymentRequestedBinding() {
        return BindingBuilder.bind(paymentRequestedQueue())
                .to(eventsExchange())
                .with("payment.requested");
    }

    @Bean
    Binding paymentApprovedBinding() {
        return BindingBuilder.bind(paymentApprovedQueue())
                .to(eventsExchange())
                .with("payment.approved");
    }

    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    private Queue eventQueue(String name) {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE);
        arguments.put("x-dead-letter-routing-key", "#");
        return QueueBuilder.durable(name).withArguments(arguments).build();
    }
}
