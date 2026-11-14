package br.com.pizzadelivery.domain;

import java.math.BigDecimal;

public enum PizzaType {
    MARGHERITA("Margherita", "Muçarela, tomate fresco e manjericão.", new BigDecimal("39.90")),
    PEPPERONI("Pepperoni", "Pepperoni artesanal e muçarela.", new BigDecimal("44.90")),
    FOUR_CHEESES("Quatro queijos", "Muçarela, gorgonzola, parmesão e provolone.", new BigDecimal("46.90")),
    PORTUGUESE("Portuguesa", "Presunto, ovo, cebola, azeitona e muçarela.", new BigDecimal("42.90")),
    CALABRESA("Calabresa", "Calabresa fatiada, cebola roxa e muçarela.", new BigDecimal("41.90")),
    CHICKEN_CATUPIRY("Frango com catupiry", "Frango temperado, catupiry e orégano.", new BigDecimal("45.90")),
    BACON("Bacon crocante", "Bacon crocante, muçarela e toque de barbecue.", new BigDecimal("47.90")),
    VEGETARIAN("Da horta", "Abobrinha, tomate, cogumelos e rúcula.", new BigDecimal("43.90")),
    FOUR_MEATS("Quatro carnes", "Pepperoni, calabresa, presunto e bacon.", new BigDecimal("49.90")),
    SLICER_BBQ("Slicer barbecue", "Frango, cebola caramelizada e molho barbecue.", new BigDecimal("46.90"));

    private final String displayName;
    private final String description;
    private final BigDecimal price;

    PizzaType(String displayName, String description, BigDecimal price) {
        this.displayName = displayName;
        this.description = description;
        this.price = price;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
