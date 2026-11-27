package br.com.pizzadelivery.service;

import br.com.pizzadelivery.domain.OrderNotFoundException;
import br.com.pizzadelivery.domain.OrderStatus;
import br.com.pizzadelivery.domain.DrinkType;
import br.com.pizzadelivery.domain.PizzaOrder;
import br.com.pizzadelivery.domain.PizzaType;
import br.com.pizzadelivery.domain.Promotion;
import br.com.pizzadelivery.events.OrderPlacedEvent;
import br.com.pizzadelivery.events.OrderStatusChangedEvent;
import br.com.pizzadelivery.repository.OrderRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository, ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    public PizzaOrder placeOrder(String customerName, String address, PizzaType pizzaType, int quantity) {
        return placeOrder(customerName, address, pizzaType, quantity, null, 0);
    }

    public PizzaOrder placeOrder(String customerName, String address, PizzaType pizzaType, int quantity,
                                 DrinkType drinkType, int drinkQuantity) {
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe o nome do cliente.");
        }
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe o endereço de entrega.");
        }
        if (pizzaType == null) {
            throw new IllegalArgumentException("Selecione uma pizza.");
        }
        if (quantity < 1 || quantity > 10) {
            throw new IllegalArgumentException("A quantidade deve ser de 1 a 10 pizzas.");
        }
        if (drinkQuantity < 0 || drinkQuantity > 10) {
            throw new IllegalArgumentException("A quantidade de bebidas deve ser de 0 a 10.");
        }
        if (drinkQuantity > 0 && drinkType == null) {
            throw new IllegalArgumentException("Selecione uma bebida ou deixe a quantidade em zero.");
        }

        Instant now = Instant.now();
        DrinkType selectedDrink = drinkQuantity == 0 ? null : drinkType;
        PizzaOrder order = new PizzaOrder(UUID.randomUUID(), customerName.trim(), address.trim(),
                pizzaType, quantity, selectedDrink, drinkQuantity, Promotion.SLICER_LAUNCH,
                OrderStatus.RECEIVED, now, now);
        orderRepository.save(order);
        eventPublisher.publishEvent(new OrderPlacedEvent(order.getId()));
        return orderRepository.findById(order.getId()).orElseThrow(
                () -> new OrderNotFoundException("Pedido não encontrado: " + order.getId()));
    }

    public synchronized PizzaOrder changeStatus(UUID orderId, OrderStatus newStatus) {
        PizzaOrder order = getOrder(orderId);
        if (order.getStatus() == newStatus) {
            return order;
        }
        if (newStatus == null || order.getStatus().next() != newStatus) {
            throw new IllegalStateException("Transição de status inválida.");
        }

        PizzaOrder updatedOrder = order.withStatus(newStatus);
        orderRepository.save(updatedOrder);
        eventPublisher.publishEvent(new OrderStatusChangedEvent(orderId, newStatus));
        return updatedOrder;
    }

    public PizzaOrder advanceOrder(UUID orderId) {
        PizzaOrder order = getOrder(orderId);
        OrderStatus nextStatus = order.getStatus().next();
        if (nextStatus == null) {
            throw new IllegalStateException("Este pedido já foi entregue.");
        }
        return changeStatus(orderId, nextStatus);
    }

    public PizzaOrder getOrder(UUID orderId) {
        return orderRepository.findById(orderId).orElseThrow(
                () -> new OrderNotFoundException("Pedido não encontrado: " + orderId));
    }

    public List<PizzaOrder> findAll() {
        return orderRepository.findAll();
    }
}
