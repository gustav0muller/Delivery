package br.com.pizzadelivery.domain;

import java.math.BigDecimal;

public enum PizzaType {
    MARGHERITA("Margherita", new BigDecimal("39.90")),
    PEPPERONI("Pepperoni", new BigDecimal("44.90")),
    FOUR_CHEESES("Quatro queijos", new BigDecimal("46.90")),
    PORTUGUESE("Portuguesa", new BigDecimal("42.90"));

    private final String displayName;
    private final BigDecimal price;

    PizzaType(String displayName, BigDecimal price) {
        this.displayName = displayName;
        this.price = price;
    }

    public String getDisplayName() {
        return displayName;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
