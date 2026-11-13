package br.com.pizzadelivery.web;

import br.com.pizzadelivery.domain.PizzaType;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class OrderForm {
    @NotBlank
    @Size(max = 80)
    private String customerName;

    @NotBlank
    @Size(max = 200)
    private String address;

    @NotNull
    private PizzaType pizzaType;

    @Min(1)
    @Max(10)
    private int quantity = 1;

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public PizzaType getPizzaType() {
        return pizzaType;
    }

    public void setPizzaType(PizzaType pizzaType) {
        this.pizzaType = pizzaType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
