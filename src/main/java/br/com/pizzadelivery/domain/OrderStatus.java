package br.com.pizzadelivery.domain;

public enum OrderStatus {
    PAYMENT_PENDING("Aguardando pagamento"),
    RECEIVED("Pagamento aprovado"),
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
            case PAYMENT_PENDING:
                return RECEIVED;
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
