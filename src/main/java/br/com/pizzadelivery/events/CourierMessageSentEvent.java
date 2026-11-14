package br.com.pizzadelivery.events;

import br.com.pizzadelivery.chat.CourierMessage;

public class CourierMessageSentEvent {
    private final CourierMessage message;

    public CourierMessageSentEvent(CourierMessage message) {
        this.message = message;
    }

    public CourierMessage getMessage() {
        return message;
    }
}
