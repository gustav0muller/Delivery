package br.com.pizzadelivery.web;

import br.com.pizzadelivery.auth.AccountSession;
import br.com.pizzadelivery.auth.CustomerAccountService;
import br.com.pizzadelivery.auth.CustomerAccountService.AccountProfile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
public class AccountController {
    private final CustomerAccountService accountService;

    public AccountController(CustomerAccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginForm", new LoginForm());
        return "login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginForm") LoginForm form,
                        BindingResult bindingResult, Model model,
                        HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Informe um e-mail válido e sua senha.");
            return "login";
        }

        AccountProfile account = accountService.authenticate(form.getEmail(), form.getPassword())
                .orElse(null);
        if (account == null) {
            model.addAttribute("error", "E-mail ou senha incorretos.");
            return "login";
        }

        startSession(account, request);
        redirectAttributes.addFlashAttribute("success", "Bem-vindo à Slicer, " + account.getDisplayName() + "!");
        return "redirect:/";
    }

    @GetMapping("/cadastro")
    public String registrationPage(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "register";
    }

    @PostMapping("/cadastro")
    public String register(@Valid @ModelAttribute("registrationForm") RegistrationForm form,
                           BindingResult bindingResult, Model model,
                           HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Preencha os campos corretamente. A senha deve ter pelo menos 8 caracteres.");
            return "register";
        }

        AccountProfile account;
        try {
            account = accountService.register(form.getDisplayName(), form.getEmail(), form.getPassword());
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "register";
        }

        startSession(account, request);
        redirectAttributes.addFlashAttribute("success", "Conta criada! Bem-vindo à Slicer, " + account.getDisplayName() + ".");
        return "redirect:/";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    private void startSession(AccountProfile account, HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        request.changeSessionId();
        session.setAttribute(AccountSession.DISPLAY_NAME, account.getDisplayName());
        session.setAttribute(AccountSession.EMAIL, account.getEmail());
    }
}
