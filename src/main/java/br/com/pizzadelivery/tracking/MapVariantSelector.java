package br.com.pizzadelivery.tracking;

import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MapVariantSelector {
    private static final int VARIANT_COUNT = 3;

    private final AtomicInteger nextVariant = new AtomicInteger();
    private final ConcurrentMap<UUID, Integer> variantsByOrder = new ConcurrentHashMap<>();

    public int variantFor(UUID orderId) {
        return variantsByOrder.computeIfAbsent(
                orderId, id -> Math.floorMod(nextVariant.getAndIncrement(), VARIANT_COUNT));
    }
}
