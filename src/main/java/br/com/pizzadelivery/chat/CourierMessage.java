package br.com.pizzadelivery.chat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public class CourierMessage {
    private final UUID id;
    private final UUID orderId;
    private final ChatSender sender;
    private final String content;
    private final Instant sentAt;

    @JsonCreator
    public CourierMessage(@JsonProperty("id") UUID id,
                          @JsonProperty("orderId") UUID orderId,
                          @JsonProperty("sender") ChatSender sender,
                          @JsonProperty("content") String content,
                          @JsonProperty("sentAt") Instant sentAt) {
        this.id = id;
        this.orderId = orderId;
        this.sender = sender;
        this.content = content;
        this.sentAt = sentAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public ChatSender getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public Instant getSentAt() {
        return sentAt;
    }
}
