package com.sproutnbloom.greenshopapplication.service.cart;

import com.sproutnbloom.greenshopapplication.model.CartItem;

public interface ICartItemService {
    void addItemToCart(Long cartId, Long productId, int quantity);
    void removeItemFromCart(Long cartId, Long cartItemId);
    void updateItemQuantity(Long cartId, Long cartItemId, int quantity);
    CartItem getCartItem(Long cartId, Long productId);
}
