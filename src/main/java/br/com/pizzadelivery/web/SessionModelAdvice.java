package br.com.pizzadelivery.web;

import br.com.pizzadelivery.auth.AccountSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpSession;

@ControllerAdvice
public class SessionModelAdvice {
    @ModelAttribute("signedInName")
    public String signedInName(HttpSession session) {
        Object displayName = session.getAttribute(AccountSession.DISPLAY_NAME);
        return displayName instanceof String ? (String) displayName : null;
    }
}
