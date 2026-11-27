package br.com.pizzadelivery.events;

import br.com.pizzadelivery.domain.OrderStatus;
import br.com.pizzadelivery.domain.OrderNotFoundException;
import br.com.pizzadelivery.payment.PaymentService;
import br.com.pizzadelivery.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.event.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventHandler {
    private static final Logger log = LoggerFactory.getLogger(PaymentEventHandler.class);
    private final PaymentService paymentService;
    private final OrderService orderService;

    public PaymentEventHandler(PaymentService paymentService, OrderService orderService) {
        this.paymentService = paymentService;
        this.orderService = orderService;
    }

    @EventListener
    @RabbitListener(queues = RabbitEventConfig.ORDER_PLACED_QUEUE)
    public void onOrderPlaced(OrderPlacedEvent event) {
        try {
            if (orderService.getOrder(event.getOrderId()).getStatus() == OrderStatus.PAYMENT_PENDING) {
                paymentService.createPendingPayment(event.getOrderId());
            }
        } catch (OrderNotFoundException ex) {
            log.debug("Ignorando pedido inexistente ao iniciar pagamento {}", event.getOrderId());
        }
    }

    @EventListener
    @RabbitListener(queues = RabbitEventConfig.PAYMENT_APPROVED_QUEUE)
    public void onPaymentApproved(PaymentApprovedEvent event) {
        if (paymentService.findByOrderId(event.getOrderId()).isPresent()
                && paymentService.approvePayment(event.getOrderId(), event.getAmount())
                && orderService.getOrder(event.getOrderId()).getStatus() == OrderStatus.PAYMENT_PENDING) {
            orderService.changeStatus(event.getOrderId(), OrderStatus.RECEIVED);
        }
    }
}
