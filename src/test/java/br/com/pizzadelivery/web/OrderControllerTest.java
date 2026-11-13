package br.com.pizzadelivery.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import br.com.pizzadelivery.service.OrderService;
import br.com.pizzadelivery.domain.OrderStatus;
import br.com.pizzadelivery.domain.PizzaOrder;
import br.com.pizzadelivery.domain.PizzaType;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderService orderService;

    @Test
    void homeShowsMenuAndOrderForm() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Monte seu pedido")))
                .andExpect(content().string(containsString("Margherita")));
    }

    @Test
    void placingAnOrderRedirectsAndShowsItsEventDrivenStatus() throws Exception {
        mockMvc.perform(post("/orders")
                        .param("customerName", "Ana")
                        .param("address", "Rua das Flores, 10")
                        .param("pizzaType", "PEPPERONI")
                        .param("quantity", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ana")))
                .andExpect(content().string(containsString("Em preparo")));
    }

    @Test
    void advancesAnOrderToTheNextStatus() throws Exception {
        PizzaOrder order = orderService.placeOrder(
                "João", "Avenida Central, 20", PizzaType.MARGHERITA, 1);

        mockMvc.perform(post("/orders/{id}/advance", order.getId()))
                .andExpect(status().is3xxRedirection());

        org.junit.jupiter.api.Assertions.assertEquals(
                OrderStatus.OUT_FOR_DELIVERY, orderService.getOrder(order.getId()).getStatus());
    }

    @Test
    void rejectsInvalidOrderWithoutCreatingIt() throws Exception {
        int ordersBefore = orderService.findAll().size();

        mockMvc.perform(post("/orders")
                        .param("customerName", "")
                        .param("address", "Rua das Flores, 10")
                        .param("pizzaType", "PEPPERONI")
                        .param("quantity", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", containsString("Confira os dados do pedido")));

        org.junit.jupiter.api.Assertions.assertEquals(ordersBefore, orderService.findAll().size());
    }
}
