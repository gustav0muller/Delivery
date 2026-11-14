package br.com.pizzadelivery.web;

import br.com.pizzadelivery.chat.ChatSender;
import br.com.pizzadelivery.chat.CourierChatService;
import br.com.pizzadelivery.chat.CourierMessage;
import br.com.pizzadelivery.domain.OrderNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.UUID;

@Controller
@Validated
public class CourierChatController {
    private final CourierChatService chatService;

    public CourierChatController(CourierChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping(path = "/orders/{id}/chat/messages", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public CourierMessage sendCustomerMessage(@PathVariable UUID id,
                                              @Valid @RequestBody CourierMessageForm form) {
        return sendMessage(id, ChatSender.CUSTOMER, form);
    }

    @PostMapping(path = "/orders/{id}/chat/courier-messages",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public CourierMessage sendCourierMessage(@PathVariable UUID id,
                                             @Valid @RequestBody CourierMessageForm form) {
        return sendMessage(id, ChatSender.COURIER, form);
    }

    private CourierMessage sendMessage(UUID id, ChatSender sender, CourierMessageForm form) {
        try {
            return chatService.sendMessage(id, sender, form.getContent());
        } catch (OrderNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado.", ex);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }
}
