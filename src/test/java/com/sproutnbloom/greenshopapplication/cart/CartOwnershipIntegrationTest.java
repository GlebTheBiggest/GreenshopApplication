package com.sproutnbloom.greenshopapplication.cart;

import com.sproutnbloom.greenshopapplication.AbstractIntegrationTest;
import com.sproutnbloom.greenshopapplication.model.Cart;
import com.sproutnbloom.greenshopapplication.model.User;
import com.sproutnbloom.greenshopapplication.repository.CartRepository;
import com.sproutnbloom.greenshopapplication.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Regression tests for the IDOR fix (UserCartController/UserCartItemController) and the
// LazyInitializationException fix on cart.getItems() (see CartRepository/CartService).
class CartOwnershipIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CartRepository cartRepository;

    private Cart createCartFor(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
    }

    @Test
    void myCart_returnsOwnCartWithoutTouchingUserIdInUrl() throws Exception {
        String token = registerAndGetToken("owner1@test.com", "Password1!");
        Cart cart = createCartFor("owner1@test.com");

        mockMvc.perform(get("/api/v1/user/carts/my-cart")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cartId").value(cart.getId()))
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    void totalPrice_ownCart_isAllowed() throws Exception {
        String token = registerAndGetToken("owner2@test.com", "Password1!");
        Cart cart = createCartFor("owner2@test.com");

        mockMvc.perform(get("/api/v1/user/carts/" + cart.getId() + "/cart/total-price")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void totalPrice_otherUsersCart_isForbidden() throws Exception {
        registerAndGetToken("owner3@test.com", "Password1!");
        String attackerToken = registerAndGetToken("attacker3@test.com", "Password1!");
        Cart victimCart = createCartFor("owner3@test.com");

        mockMvc.perform(get("/api/v1/user/carts/" + victimCart.getId() + "/cart/total-price")
                        .header("Authorization", "Bearer " + attackerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void removeItem_otherUsersCart_isForbidden() throws Exception {
        registerAndGetToken("owner4@test.com", "Password1!");
        String attackerToken = registerAndGetToken("attacker4@test.com", "Password1!");
        Cart victimCart = createCartFor("owner4@test.com");

        mockMvc.perform(delete("/api/v1/cartItems/cart/" + victimCart.getId() + "/item/1/remove")
                        .header("Authorization", "Bearer " + attackerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateItem_otherUsersCart_isForbidden() throws Exception {
        registerAndGetToken("owner5@test.com", "Password1!");
        String attackerToken = registerAndGetToken("attacker5@test.com", "Password1!");
        Cart victimCart = createCartFor("owner5@test.com");

        mockMvc.perform(put("/api/v1/cartItems/cart/" + victimCart.getId() + "/item/1/update?quantity=5")
                        .header("Authorization", "Bearer " + attackerToken))
                .andExpect(status().isForbidden());
    }
}
