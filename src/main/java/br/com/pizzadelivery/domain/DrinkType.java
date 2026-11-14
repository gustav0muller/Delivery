package br.com.pizzadelivery.domain;

import java.math.BigDecimal;

public enum DrinkType {
    COCA_COLA("Coca-Cola 350 ml", new BigDecimal("4.90"), true),
    COCA_ZERO("Coca-Cola Zero 350 ml", new BigDecimal("4.90"), true),
    GUARANA("Guaraná 350 ml", new BigDecimal("4.90"), true),
    LEMON_SODA("Soda limonada 350 ml", new BigDecimal("5.90"), false),
    ORANGE_JUICE("Suco de laranja 300 ml", new BigDecimal("7.90"), false),
    SPARKLING_WATER("Água com gás 500 ml", new BigDecimal("4.50"), false);

    private final String displayName;
    private final BigDecimal price;
    private final boolean comboPrice;

    DrinkType(String displayName, BigDecimal price, boolean comboPrice) {
        this.displayName = displayName;
        this.price = price;
        this.comboPrice = comboPrice;
    }

    public String getDisplayName() {
        return displayName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public boolean isComboPrice() {
        return comboPrice;
    }
}
