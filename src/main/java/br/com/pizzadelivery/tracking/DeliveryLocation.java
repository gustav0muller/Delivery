package br.com.pizzadelivery.tracking;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public class DeliveryLocation {
    private static final double ROUTE_DISTANCE_KM = 2.4;
    private static final int SIMULATION_DURATION_SECONDS = 30;

    private final UUID orderId;
    private final double progress;
    private final int etaSeconds;
    private final double distanceKm;
    private final Instant updatedAt;

    @JsonCreator
    public DeliveryLocation(@JsonProperty("orderId") UUID orderId,
                            @JsonProperty("progress") double progress,
                            @JsonProperty("updatedAt") Instant updatedAt) {
        this.orderId = orderId;
        this.progress = Math.max(0, Math.min(100, progress));
        this.etaSeconds = (int) Math.ceil(
                SIMULATION_DURATION_SECONDS * (100 - this.progress) / 100);
        this.distanceKm = ROUTE_DISTANCE_KM * (100 - this.progress) / 100;
        this.updatedAt = updatedAt;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public double getProgress() {
        return progress;
    }

    public int getEtaSeconds() {
        return etaSeconds;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean isCompleted() {
        return progress >= 100;
    }
}
