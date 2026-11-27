package br.com.pizzadelivery.events;

import br.com.pizzadelivery.chat.CourierMessage;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CourierMessageSentEvent {
    private final CourierMessage message;

    @JsonCreator
    public CourierMessageSentEvent(@JsonProperty("message") CourierMessage message) {
        this.message = message;
    }

    public CourierMessage getMessage() {
        return message;
    }
}
