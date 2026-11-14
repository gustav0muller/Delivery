package br.com.pizzadelivery.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public enum Promotion {
    SLICER_LAUNCH("Mordida de inauguração", "10% OFF em todas as pizzas",
            new BigDecimal("0.10"));

    private final String displayName;
    private final String description;
    private final BigDecimal discountRate;

    Promotion(String displayName, String description, BigDecimal discountRate) {
        this.displayName = displayName;
        this.description = description;
        this.discountRate = discountRate;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal discountFor(BigDecimal pizzaSubtotal) {
        return pizzaSubtotal.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
    }

}
