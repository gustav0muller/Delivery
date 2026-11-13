package br.com.pizzadelivery.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;
import br.com.pizzadelivery.service.OrderService;
import br.com.pizzadelivery.domain.OrderStatus;
import br.com.pizzadelivery.domain.PizzaOrder;
import br.com.pizzadelivery.domain.PizzaType;
import br.com.pizzadelivery.tracking.DeliveryTrackingRegistry;
import br.com.pizzadelivery.tracking.MapVariantSelector;

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

    @Autowired
    private DeliveryTrackingRegistry trackingRegistry;

    @Autowired
    private MapVariantSelector mapVariantSelector;

    @Test
    void homeShowsMenuAndOrderForm() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Monte seu pedido")))
                .andExpect(content().string(containsString("Margherita")));
    }

    @Test
    void placingAnOrderRedirectsAndShowsItsEventDrivenStatus() throws Exception {
        MvcResult result = mockMvc.perform(post("/orders")
                        .param("customerName", "Ana")
                        .param("address", "Rua das Flores, 10")
                        .param("pizzaType", "PEPPERONI")
                        .param("quantity", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("success", containsString("Pedido enviado")))
                .andReturn();

        mockMvc.perform(get("/").session((MockHttpSession) result.getRequest().getSession(false)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ana")))
                .andExpect(content().string(containsString("Em preparo")))
                .andExpect(content().string(containsString("Pedido enviado para a cozinha")));
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
    void trackingPageRendersTheIllustratedMapForAnOrderInDelivery() throws Exception {
        PizzaOrder order = orderService.placeOrder(
                "João", "Avenida Central, 20", PizzaType.MARGHERITA, 1);
        orderService.advanceOrder(order.getId());
        orderService.advanceOrder(order.getId());

        String response = mockMvc.perform(get("/orders/{id}/tracking", order.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SIMULAÇÃO EM TEMPO REAL")))
                .andExpect(content().string(containsString("Mapa ilustrativo com trajeto")))
                .andExpect(content().string(containsString("delivery-route")))
                .andExpect(content().string(containsString("courier")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        int mapVariant = mapVariantSelector.variantFor(order.getId());
        org.junit.jupiter.api.Assertions.assertTrue(
                response.contains("data-map-variant=\"" + mapVariant + "\""));
        org.junit.jupiter.api.Assertions.assertTrue(
                response.contains("id=\"delivery-route-" + mapVariant + "\""));
        for (int otherVariant = 0; otherVariant < 3; otherVariant++) {
            if (otherVariant != mapVariant) {
                org.junit.jupiter.api.Assertions.assertFalse(
                        response.contains("id=\"delivery-route-" + otherVariant + "\""));
            }
        }

        org.junit.jupiter.api.Assertions.assertTrue(
                trackingRegistry.findLocation(order.getId()).isPresent());
    }

    @Test
    void trackingPageIsNotAvailableBeforeTheOrderLeavesTheKitchen() throws Exception {
        PizzaOrder order = orderService.placeOrder(
                "João", "Avenida Central, 20", PizzaType.MARGHERITA, 1);

        mockMvc.perform(get("/orders/{id}/tracking", order.getId()))
                .andExpect(status().isConflict());
    }

    @Test
    void trackingPageReturnsNotFoundForAnUnknownOrder() throws Exception {
        mockMvc.perform(get("/orders/{id}/tracking", java.util.UUID.randomUUID()))
                .andExpect(status().isNotFound());
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
