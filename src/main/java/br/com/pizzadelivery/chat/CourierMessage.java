package br.com.pizzadelivery.chat;

import java.time.Instant;
import java.util.UUID;

public class CourierMessage {
    private final UUID id;
    private final UUID orderId;
    private final ChatSender sender;
    private final String content;
    private final Instant sentAt;

    public CourierMessage(UUID id, UUID orderId, ChatSender sender, String content, Instant sentAt) {
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
