package br.com.pizzadelivery.events;

import br.com.pizzadelivery.tracking.DeliveryLocation;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DeliveryLocationUpdatedEvent {
    private final DeliveryLocation location;

    @JsonCreator
    public DeliveryLocationUpdatedEvent(@JsonProperty("location") DeliveryLocation location) {
        this.location = location;
    }

    public DeliveryLocation getLocation() {
        return location;
    }
}
