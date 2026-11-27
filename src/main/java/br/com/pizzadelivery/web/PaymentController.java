package br.com.pizzadelivery.web;

import br.com.pizzadelivery.domain.OrderNotFoundException;
import br.com.pizzadelivery.domain.OrderStatus;
import br.com.pizzadelivery.domain.PizzaOrder;
import br.com.pizzadelivery.payment.Payment;
import br.com.pizzadelivery.payment.PaymentService;
import br.com.pizzadelivery.payment.PaymentStatus;
import br.com.pizzadelivery.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
public class PaymentController {
    private final OrderService orderService;
    private final PaymentService paymentService;

    public PaymentController(OrderService orderService, PaymentService paymentService) {
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    @GetMapping("/orders/{id}/payment")
    public String paymentPage(@PathVariable UUID id, Model model,
                              RedirectAttributes redirectAttributes) {
        PizzaOrder order = findOrder(id);
        if (order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            redirectAttributes.addFlashAttribute("success", "Este pedido já foi pago.");
            return "redirect:/";
        }

        Payment payment = paymentService.findByOrderId(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pagamento não encontrado."));
        model.addAttribute("order", order);
        model.addAttribute("payment", payment);
        return "payment";
    }

    @PostMapping("/orders/{id}/payment")
    public String confirmPayment(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            Payment payment = paymentService.requestPayment(id);
            if (payment.getStatus() == PaymentStatus.APPROVED) {
                redirectAttributes.addFlashAttribute(
                        "success", "Pagamento simulado aprovado. Pedido enviado para a cozinha!");
            } else {
                redirectAttributes.addFlashAttribute("success", "Pagamento em processamento.");
            }
        } catch (OrderNotFoundException ex) {
            redirectAttributes.addFlashAttribute("error", "Pedido não encontrado.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/";
    }

    private PizzaOrder findOrder(UUID id) {
        try {
            return orderService.getOrder(id);
        } catch (OrderNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado.", ex);
        }
    }
}
