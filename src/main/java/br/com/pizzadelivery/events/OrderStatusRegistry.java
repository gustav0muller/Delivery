package br.com.pizzadelivery.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class OrderStatusRegistry {
    private static final Logger log = LoggerFactory.getLogger(OrderStatusRegistry.class);

    private final ConcurrentMap<UUID, CopyOnWriteArrayList<SseEmitter>> subscribers =
            new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, OrderStatusChangedEvent> latestStatuses =
            new ConcurrentHashMap<>();

    public void publish(OrderStatusChangedEvent event) {
        latestStatuses.put(event.getOrderId(), event);
        CopyOnWriteArrayList<SseEmitter> emitters = subscribers.get(event.getOrderId());
        if (emitters == null) {
            return;
        }
        for (SseEmitter emitter : emitters) {
            send(event, emitter);
        }
    }

    public SseEmitter subscribe(UUID orderId) {
        SseEmitter emitter = new SseEmitter(0L);
        CopyOnWriteArrayList<SseEmitter> orderSubscribers =
                subscribers.computeIfAbsent(orderId, id -> new CopyOnWriteArrayList<>());
        orderSubscribers.add(emitter);
        emitter.onCompletion(() -> remove(orderId, emitter));
        emitter.onTimeout(() -> {
            remove(orderId, emitter);
            emitter.complete();
        });
        emitter.onError(error -> remove(orderId, emitter));

        OrderStatusChangedEvent latest = latestStatuses.get(orderId);
        if (latest != null) {
            send(latest, emitter);
        }
        return emitter;
    }

    private void send(OrderStatusChangedEvent event, SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().name("status").data(event));
        } catch (IOException | IllegalStateException ex) {
            log.debug("Removendo conexão de status encerrada do pedido {}", event.getOrderId(), ex);
            remove(event.getOrderId(), emitter);
            emitter.complete();
        }
    }

    private void remove(UUID orderId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitters = subscribers.get(orderId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                subscribers.remove(orderId, emitters);
            }
        }
    }
}
