package br.com.pizzadelivery.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class PizzaOrder {
    private final UUID id;
    private final String customerName;
    private final String address;
    private final PizzaType pizzaType;
    private final int quantity;
    private final DrinkType drinkType;
    private final int drinkQuantity;
    private final Promotion promotion;
    private final OrderStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;

    public PizzaOrder(UUID id, String customerName, String address, PizzaType pizzaType,
                      int quantity, DrinkType drinkType, int drinkQuantity, Promotion promotion,
                      OrderStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.customerName = customerName;
        this.address = address;
        this.pizzaType = pizzaType;
        this.quantity = quantity;
        this.drinkType = drinkType;
        this.drinkQuantity = drinkQuantity;
        this.promotion = promotion;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public PizzaOrder withStatus(OrderStatus newStatus) {
        return new PizzaOrder(id, customerName, address, pizzaType, quantity, drinkType,
                drinkQuantity, promotion, newStatus, createdAt, Instant.now());
    }

    public UUID getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getAddress() {
        return address;
    }

    public PizzaType getPizzaType() {
        return pizzaType;
    }

    public int getQuantity() {
        return quantity;
    }

    public DrinkType getDrinkType() {
        return drinkType;
    }

    public int getDrinkQuantity() {
        return drinkQuantity;
    }

    public Promotion getPromotion() {
        return promotion;
    }

    public BigDecimal getPizzaSubtotal() {
        return pizzaType.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public BigDecimal getDrinkSubtotal() {
        if (drinkType == null || drinkQuantity == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return drinkType.getPrice().multiply(BigDecimal.valueOf(drinkQuantity));
    }

    public BigDecimal getDiscount() {
        return promotion.discountFor(getPizzaSubtotal());
    }

    public BigDecimal getTotalPrice() {
        return getPizzaSubtotal().subtract(getDiscount()).add(getDrinkSubtotal());
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
