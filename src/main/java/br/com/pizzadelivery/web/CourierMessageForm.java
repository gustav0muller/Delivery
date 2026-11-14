package br.com.pizzadelivery.web;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class CourierMessageForm {
    @NotBlank(message = "Digite uma mensagem.")
    @Size(max = 500, message = "A mensagem deve ter no máximo 500 caracteres.")
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
