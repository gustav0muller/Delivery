package br.com.pizzadelivery.domain;

public enum OrderStatus {
    RECEIVED("Recebido"),
    PREPARING("Em preparo"),
    OUT_FOR_DELIVERY("Saiu para entrega"),
    DELIVERED("Entregue");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public OrderStatus next() {
        switch (this) {
            case RECEIVED:
                return PREPARING;
            case PREPARING:
                return OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY:
                return DELIVERED;
            default:
                return null;
        }
    }
}
