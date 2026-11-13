package br.com.pizzadelivery.tracking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class DeliveryTrackingRegistry {
    private static final Logger log = LoggerFactory.getLogger(DeliveryTrackingRegistry.class);

    private final ConcurrentMap<UUID, DeliveryLocation> locations = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, CopyOnWriteArrayList<SseEmitter>> subscribers =
            new ConcurrentHashMap<>();

    public Optional<DeliveryLocation> findLocation(UUID orderId) {
        return Optional.ofNullable(locations.get(orderId));
    }

    public void publish(DeliveryLocation location) {
        locations.put(location.getOrderId(), location);
        List<SseEmitter> emitters = subscribers.get(location.getOrderId());
        if (emitters == null) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            send(location.getOrderId(), emitter, location);
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

        findLocation(orderId).ifPresent(location -> send(orderId, emitter, location));
        return emitter;
    }

    private void send(UUID orderId, SseEmitter emitter, DeliveryLocation location) {
        try {
            emitter.send(SseEmitter.event().name("location").data(location));
        } catch (IOException | IllegalStateException ex) {
            log.debug("Removendo conexão de rastreamento encerrada do pedido {}", orderId, ex);
            remove(orderId, emitter);
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
