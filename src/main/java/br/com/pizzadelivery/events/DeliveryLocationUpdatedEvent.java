package br.com.pizzadelivery.events;

import br.com.pizzadelivery.tracking.DeliveryLocation;

public class DeliveryLocationUpdatedEvent {
    private final DeliveryLocation location;

    public DeliveryLocationUpdatedEvent(DeliveryLocation location) {
        this.location = location;
    }

    public DeliveryLocation getLocation() {
        return location;
    }
}
