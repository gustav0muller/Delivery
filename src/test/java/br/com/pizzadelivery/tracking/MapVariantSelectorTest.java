package br.com.pizzadelivery.tracking;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapVariantSelectorTest {
    @Test
    void assignsDifferentMapsInSequenceAndKeepsTheSelectionForEachOrder() {
        MapVariantSelector selector = new MapVariantSelector();
        UUID firstOrder = UUID.randomUUID();
        UUID secondOrder = UUID.randomUUID();
        UUID thirdOrder = UUID.randomUUID();
        UUID fourthOrder = UUID.randomUUID();

        assertEquals(0, selector.variantFor(firstOrder));
        assertEquals(1, selector.variantFor(secondOrder));
        assertEquals(2, selector.variantFor(thirdOrder));
        assertEquals(0, selector.variantFor(firstOrder));
        assertEquals(0, selector.variantFor(fourthOrder));
    }
}
