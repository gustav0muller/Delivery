package br.com.pizzadelivery.web;

import br.com.pizzadelivery.domain.OrderNotFoundException;
import br.com.pizzadelivery.domain.PizzaOrder;
import br.com.pizzadelivery.domain.DrinkType;
import br.com.pizzadelivery.domain.PizzaType;
import br.com.pizzadelivery.domain.Promotion;
import br.com.pizzadelivery.auth.AccountSession;
import br.com.pizzadelivery.events.OrderStatusRegistry;
import br.com.pizzadelivery.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Controller
public class OrderController {
    private final OrderService orderService;
    private final OrderStatusRegistry statusRegistry;

    public OrderController(OrderService orderService, OrderStatusRegistry statusRegistry) {
        this.orderService = orderService;
        this.statusRegistry = statusRegistry;
    }

    @ModelAttribute("pizzaTypes")
    public PizzaType[] pizzaTypes() {
        return PizzaType.values();
    }

    @ModelAttribute("drinkTypes")
    public DrinkType[] drinkTypes() {
        return DrinkType.values();
    }

    @ModelAttribute("promotions")
    public Promotion[] promotions() {
        return Promotion.values();
    }

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        model.addAttribute("orders", orderService.findAll());
        OrderForm form = new OrderForm();
        Object signedInName = session.getAttribute(AccountSession.DISPLAY_NAME);
        if (signedInName instanceof String) {
            form.setCustomerName((String) signedInName);
        }
        model.addAttribute("orderForm", form);
        return "index";
    }

    @PostMapping("/orders")
    public String placeOrder(@Valid @ModelAttribute("orderForm") OrderForm form,
                             BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Confira os dados do pedido e tente novamente.");
            return "redirect:/";
        }
        if (form.getDrinkQuantity() > 0 && form.getDrinkType() == null) {
            redirectAttributes.addFlashAttribute("error", "Selecione uma bebida ou deixe a quantidade em zero.");
            return "redirect:/";
        }

        PizzaOrder order = orderService.placeOrder(form.getCustomerName(), form.getAddress(),
                form.getPizzaType(), form.getQuantity(), form.getDrinkType(), form.getDrinkQuantity());
        redirectAttributes.addFlashAttribute("success",
                "Pedido criado. Conclua o pagamento para enviar à cozinha.");
        return "redirect:/orders/" + order.getId() + "/payment";
    }

    @PostMapping("/orders/{id}/advance")
    public String advanceOrder(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            orderService.advanceOrder(UUID.fromString(id));
            redirectAttributes.addFlashAttribute("success", "Status do pedido atualizado.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        } catch (OrderNotFoundException ex) {
            redirectAttributes.addFlashAttribute("error", "Pedido não encontrado.");
        }
        return "redirect:/";
    }

    @GetMapping("/orders/{id}/status")
    public ResponseEntity<Map<String, String>> orderStatus(@PathVariable UUID id) {
        try {
            br.com.pizzadelivery.domain.PizzaOrder order = orderService.getOrder(id);
            Map<String, String> status = new LinkedHashMap<>();
            status.put("status", order.getStatus().name());
            status.put("displayName", order.getStatus().getDisplayName());
            return ResponseEntity.ok(status);
        } catch (OrderNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(path = "/orders/{id}/status/events", produces = "text/event-stream")
    public SseEmitter orderStatusEvents(@PathVariable UUID id) {
        if (orderService.findAll().stream().noneMatch(order -> order.getId().equals(id))) {
            throw new OrderNotFoundException("Pedido não encontrado: " + id);
        }
        return statusRegistry.subscribe(id);
    }
}
