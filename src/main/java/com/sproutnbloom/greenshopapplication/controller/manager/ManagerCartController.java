package com.sproutnbloom.greenshopapplication.controller.manager;

import com.sproutnbloom.greenshopapplication.dto.CartDto;
import com.sproutnbloom.greenshopapplication.dto.CartItemDto;
import com.sproutnbloom.greenshopapplication.dto.ProductDto;
import com.sproutnbloom.greenshopapplication.exceptions.ResourceNotFoundException;
import com.sproutnbloom.greenshopapplication.model.Cart;
import com.sproutnbloom.greenshopapplication.model.CartItem;
import com.sproutnbloom.greenshopapplication.response.ApiResponse;
import com.sproutnbloom.greenshopapplication.service.cart.ICartService;
import com.sproutnbloom.greenshopapplication.service.product.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/management/carts")
public class ManagerCartController {
    private final ICartService cartService;
    private final IProductService productService;

    @GetMapping("/{cartId}/cart")
    public ResponseEntity<ApiResponse> getCart(@PathVariable Long cartId) {
        try {
            Cart cart = cartService.getCart(cartId);
            CartDto dto = new CartDto();
            dto.setCartId(cart.getId());
            dto.setTotalAmount(cart.getTotalAmount());
            Set<CartItemDto> items = cart.getItems().stream().map(this::toCartItemDto).collect(Collectors.toSet());
            dto.setItems(items);
            return ResponseEntity.ok(new ApiResponse("Success", dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @Transactional
    @DeleteMapping("/{cartId}/clear")
    public ResponseEntity<ApiResponse> clearCart(@PathVariable Long cartId) {
        try {
            cartService.clearCart(cartId);
            return ResponseEntity.ok(new ApiResponse("Clear Cart Success!", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    private CartItemDto toCartItemDto(CartItem item) {
        CartItemDto dto = new CartItemDto();
        dto.setItemId(item.getId());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        ProductDto productDto = productService.convertToDto(item.getProduct());
        dto.setProduct(productDto);
        return dto;
    }
}
