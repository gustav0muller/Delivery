package br.com.pizzadelivery.payment;

public enum PaymentStatus {
    PENDING("Aguardando confirmação"),
    PROCESSING("Processando"),
    APPROVED("Aprovado");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
