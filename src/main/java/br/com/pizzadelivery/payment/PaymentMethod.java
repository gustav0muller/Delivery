package br.com.pizzadelivery.payment;

public enum PaymentMethod {
    PIX_SIMULATED("Pix (simulado)");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
