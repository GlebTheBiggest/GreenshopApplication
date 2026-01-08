package com.sproutnbloom.greenshopapplication.service.cart;

import com.sproutnbloom.greenshopapplication.model.Cart;
import com.sproutnbloom.greenshopapplication.model.User;

import java.math.BigDecimal;
import java.util.Optional;

public interface ICartService {
    Cart getCart(Long id);
    void clearCart(Long id);
    BigDecimal getTotalPrice(Long id);

    Cart initializeNewCart(User user);

    Cart getCartByUserId(Long userId);
}
