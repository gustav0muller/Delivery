package br.com.pizzadelivery.events;

import br.com.pizzadelivery.chat.CourierChatRegistry;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CourierMessageEventHandler {
    private final CourierChatRegistry chatRegistry;

    public CourierMessageEventHandler(CourierChatRegistry chatRegistry) {
        this.chatRegistry = chatRegistry;
    }

    @EventListener
    public void onMessageSent(CourierMessageSentEvent event) {
        chatRegistry.publish(event.getMessage());
    }
}
