package br.com.pizzadelivery.auth;

import br.com.pizzadelivery.auth.CustomerAccountService.AccountProfile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerAccountServiceTest {
    @Test
    void registersAndAuthenticatesWithNormalizedEmail() {
        CustomerAccountService service = new CustomerAccountService();
        AccountProfile registered = service.register("  Ana  ", " Ana@Example.com ", "pizza1234");

        assertEquals("Ana", registered.getDisplayName());
        assertEquals("ana@example.com", registered.getEmail());
        assertTrue(service.authenticate("ANA@example.com", "pizza1234").isPresent());
        assertFalse(service.authenticate("ana@example.com", "wrong-pass").isPresent());
    }

    @Test
    void rejectsDuplicateEmailRegardlessOfCase() {
        CustomerAccountService service = new CustomerAccountService();
        service.register("Ana", "ana@example.com", "pizza1234");

        assertThrows(IllegalArgumentException.class,
                () -> service.register("Bia", "ANA@example.com", "pizza5678"));
    }
}
