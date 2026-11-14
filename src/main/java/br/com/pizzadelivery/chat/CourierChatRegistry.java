package br.com.pizzadelivery.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class CourierChatRegistry {
    private static final Logger log = LoggerFactory.getLogger(CourierChatRegistry.class);

    private final ConcurrentMap<UUID, ChatThread> threads = new ConcurrentHashMap<>();

    public List<CourierMessage> findMessages(UUID orderId) {
        ChatThread thread = threads.get(orderId);
        if (thread == null) {
            return Collections.emptyList();
        }
        synchronized (thread) {
            return Collections.unmodifiableList(new ArrayList<>(thread.messages));
        }
    }

    public void publish(CourierMessage message) {
        ChatThread thread = threadFor(message.getOrderId());
        synchronized (thread) {
            thread.messages.add(message);
            for (SseEmitter emitter : thread.subscribers) {
                send(message.getOrderId(), emitter, message);
            }
        }
    }

    public SseEmitter subscribe(UUID orderId) {
        ChatThread thread = threadFor(orderId);
        SseEmitter emitter = new SseEmitter(0L);
        emitter.onCompletion(() -> remove(thread, emitter));
        emitter.onTimeout(() -> {
            remove(thread, emitter);
            emitter.complete();
        });
        emitter.onError(error -> remove(thread, emitter));

        synchronized (thread) {
            for (CourierMessage message : thread.messages) {
                if (!send(orderId, emitter, message)) {
                    return emitter;
                }
            }
            thread.subscribers.add(emitter);
        }
        return emitter;
    }

    private ChatThread threadFor(UUID orderId) {
        return threads.computeIfAbsent(orderId, id -> new ChatThread());
    }

    private boolean send(UUID orderId, SseEmitter emitter, CourierMessage message) {
        try {
            emitter.send(SseEmitter.event().name("message").data(message));
            return true;
        } catch (IOException | IllegalStateException ex) {
            log.debug("Removendo conexão de chat encerrada do pedido {}", orderId, ex);
            remove(threadFor(orderId), emitter);
            emitter.complete();
            return false;
        }
    }

    private void remove(ChatThread thread, SseEmitter emitter) {
        thread.subscribers.remove(emitter);
    }

    private static class ChatThread {
        private final List<CourierMessage> messages = new ArrayList<>();
        private final CopyOnWriteArrayList<SseEmitter> subscribers = new CopyOnWriteArrayList<>();
    }
}
