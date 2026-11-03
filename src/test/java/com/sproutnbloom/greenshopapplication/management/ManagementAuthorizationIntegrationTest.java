package com.sproutnbloom.greenshopapplication.management;

import com.sproutnbloom.greenshopapplication.AbstractIntegrationTest;
import com.sproutnbloom.greenshopapplication.enums.Role;
import com.sproutnbloom.greenshopapplication.model.Cart;
import com.sproutnbloom.greenshopapplication.model.User;
import com.sproutnbloom.greenshopapplication.repository.CartRepository;
import com.sproutnbloom.greenshopapplication.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Regression tests for the SecurityConfig matcher-ordering fix and the
// ADMIN_READ.getPermission() vs .name() authority-string fix.
class ManagementAuthorizationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private Cart createCartFor(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
    }

    private String registerAdminAndGetToken(String email, String password) throws Exception {
        User admin = User.builder()
                .firstname("Admin").lastname("Admin")
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(Role.ADMIN)
                .build();
        userRepository.save(admin);
        return authenticateAndGetToken(email, password);
    }

    @Test
    void managementCart_plainUser_isForbidden() throws Exception {
        String token = registerAndGetToken("plainuser@test.com", "Password1!");
        Cart cart = createCartFor("plainuser@test.com");

        mockMvc.perform(get("/api/v1/management/carts/" + cart.getId() + "/cart")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void managementCart_admin_isAllowed() throws Exception {
        String token = registerAdminAndGetToken("admin1@test.com", "Password1!");
        Cart cart = createCartFor("admin1@test.com");

        mockMvc.perform(get("/api/v1/management/carts/" + cart.getId() + "/cart")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void managementEndpoint_unauthenticated_isForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/management/carts/999/cart"))
                .andExpect(status().isForbidden());
    }
}
