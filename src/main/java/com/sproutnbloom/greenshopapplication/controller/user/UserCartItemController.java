package com.sproutnbloom.greenshopapplication.controller.user;

import com.sproutnbloom.greenshopapplication.exceptions.ResourceNotFoundException;
import com.sproutnbloom.greenshopapplication.model.Cart;
import com.sproutnbloom.greenshopapplication.model.User;
import com.sproutnbloom.greenshopapplication.response.ApiResponse;
import com.sproutnbloom.greenshopapplication.service.cart.ICartItemService;
import com.sproutnbloom.greenshopapplication.service.cart.ICartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/cartItems")
public class UserCartItemController {
    private final ICartItemService cartItemService;
    private final ICartService cartService;

    @PostMapping("/item/add")
    public ResponseEntity<ApiResponse> addItemToCart(@AuthenticationPrincipal User currentUser,
                                                     @RequestParam Long productId,
                                                     @RequestParam Integer quantity) {
        try {
            Cart cart = Optional.ofNullable(cartService.getCartByUserId(currentUser.getId()))
                    .orElseGet(() -> cartService.initializeNewCart(currentUser));
            cartItemService.addItemToCart(cart.getId(), productId, quantity);
            return ResponseEntity.ok(new ApiResponse("Add Item Success", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @DeleteMapping("/cart/{cartId}/item/{itemId}/remove")
    public ResponseEntity<ApiResponse> removeItemFromCart(@PathVariable Long cartId, @PathVariable Long itemId, @AuthenticationPrincipal User currentUser) {
        try {
            verifyOwnership(cartId, currentUser);
            cartItemService.removeItemFromCart(cartId, itemId);
            return ResponseEntity.ok(new ApiResponse("Remove Item Success", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PutMapping("/cart/{cartId}/item/{itemId}/update")
    public ResponseEntity<ApiResponse> updateItemQuantity(@PathVariable Long cartId, @PathVariable Long itemId, @RequestParam Integer quantity, @AuthenticationPrincipal User currentUser) {
        try {
            verifyOwnership(cartId, currentUser);
            cartItemService.updateItemQuantity(cartId, itemId, quantity);
            return ResponseEntity.ok(new ApiResponse("Update Item Success", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }

    }

    private void verifyOwnership(Long cartId, User currentUser) {
        Cart cart = cartService.getCart(cartId);
        if (cart.getUser() == null || !cart.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have access to this cart");
        }
    }
}