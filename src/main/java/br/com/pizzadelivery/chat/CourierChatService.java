package br.com.pizzadelivery.chat;

import br.com.pizzadelivery.domain.OrderStatus;
import br.com.pizzadelivery.domain.PizzaOrder;
import br.com.pizzadelivery.events.CourierMessageSentEvent;
import br.com.pizzadelivery.service.OrderService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class CourierChatService {
    private static final int MAX_MESSAGE_LENGTH = 500;

    private final OrderService orderService;
    private final ApplicationEventPublisher eventPublisher;

    public CourierChatService(OrderService orderService, ApplicationEventPublisher eventPublisher) {
        this.orderService = orderService;
        this.eventPublisher = eventPublisher;
    }

    public CourierMessage sendMessage(UUID orderId, ChatSender sender, String content) {
        PizzaOrder order = orderService.getOrder(orderId);
        if (order.getStatus() != OrderStatus.OUT_FOR_DELIVERY) {
            throw new IllegalStateException("A conversa fica disponível enquanto o pedido está a caminho.");
        }
        if (sender == null) {
            throw new IllegalArgumentException("Informe quem enviou a mensagem.");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Digite uma mensagem.");
        }
        String normalizedContent = content.trim();
        if (normalizedContent.length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("A mensagem deve ter no máximo 500 caracteres.");
        }

        CourierMessage message = new CourierMessage(
                UUID.randomUUID(), orderId, sender, normalizedContent, Instant.now());
        eventPublisher.publishEvent(new CourierMessageSentEvent(message));
        return message;
    }
}
