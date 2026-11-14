package br.com.pizzadelivery.web;

import br.com.pizzadelivery.chat.CourierChatRegistry;
import br.com.pizzadelivery.domain.OrderStatus;
import br.com.pizzadelivery.domain.OrderNotFoundException;
import br.com.pizzadelivery.domain.PizzaOrder;
import br.com.pizzadelivery.service.OrderService;
import br.com.pizzadelivery.tracking.DeliveryLocation;
import br.com.pizzadelivery.tracking.DeliveryTrackingRegistry;
import br.com.pizzadelivery.tracking.MapVariantSelector;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@Controller
public class DeliveryTrackingController {
    private final OrderService orderService;
    private final DeliveryTrackingRegistry trackingRegistry;
    private final CourierChatRegistry chatRegistry;
    private final MapVariantSelector mapVariantSelector;

    public DeliveryTrackingController(OrderService orderService,
                                      DeliveryTrackingRegistry trackingRegistry,
                                      CourierChatRegistry chatRegistry,
                                      MapVariantSelector mapVariantSelector) {
        this.orderService = orderService;
        this.trackingRegistry = trackingRegistry;
        this.chatRegistry = chatRegistry;
        this.mapVariantSelector = mapVariantSelector;
    }

    @GetMapping("/orders/{id}/tracking")
    public String trackingPage(@PathVariable UUID id, Model model) {
        PizzaOrder order = findTrackableOrder(id);
        model.addAttribute("order", order);
        model.addAttribute("location", trackingRegistry.findLocation(id).orElse(null));
        model.addAttribute("mapVariant", mapVariantSelector.variantFor(id));
        model.addAttribute("chatMessages", chatRegistry.findMessages(id));
        return "tracking";
    }

    @GetMapping(path = "/orders/{id}/tracking/events", produces = "text/event-stream")
    @ResponseBody
    public SseEmitter locationEvents(@PathVariable UUID id) {
        findTrackableOrder(id);
        return trackingRegistry.subscribe(id);
    }

    @GetMapping(path = "/orders/{id}/chat/events", produces = "text/event-stream")
    @ResponseBody
    public SseEmitter chatEvents(@PathVariable UUID id) {
        findTrackableOrder(id);
        return chatRegistry.subscribe(id);
    }

    private PizzaOrder findTrackableOrder(UUID id) {
        PizzaOrder order;
        try {
            order = orderService.getOrder(id);
        } catch (OrderNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado.", ex);
        }
        if (order.getStatus() != OrderStatus.OUT_FOR_DELIVERY
                && order.getStatus() != OrderStatus.DELIVERED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "O pedido ainda está sendo preparado.");
        }
        return order;
    }
}
