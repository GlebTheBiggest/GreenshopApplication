package com.sproutnbloom.greenshopapplication.service.cart;

import com.sproutnbloom.greenshopapplication.exceptions.ResourceNotFoundException;
import com.sproutnbloom.greenshopapplication.model.Cart;
import com.sproutnbloom.greenshopapplication.model.User;
import com.sproutnbloom.greenshopapplication.repository.CartItemRepository;
import com.sproutnbloom.greenshopapplication.repository.CartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @InjectMocks
    private CartService cartService;

    @Test
    void getCartByUserId_returnsNull_whenNoCartExists() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThat(cartService.getCartByUserId(1L)).isNull();
    }

    @Test
    void getCartByUserId_returnsCart_whenExists() {
        Cart cart = new Cart();
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        assertThat(cartService.getCartByUserId(1L)).isSameAs(cart);
    }

    @Test
    void getCart_throwsResourceNotFound_whenMissing() {
        when(cartRepository.findByIdWithItems(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.getCart(1L));
    }

    @Test
    void initializeNewCart_returnsExistingCart_insteadOfCreatingDuplicate() {
        User user = new User();
        user.setId(1L);
        Cart existing = new Cart();
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(existing));

        Cart result = cartService.initializeNewCart(user);

        assertThat(result).isSameAs(existing);
        verify(cartRepository, never()).save(any());
    }

    @Test
    void initializeNewCart_createsAndSavesNewCart_whenNoneExists() {
        User user = new User();
        user.setId(1L);
        Cart saved = new Cart();
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(saved);

        Cart result = cartService.initializeNewCart(user);

        assertThat(result).isSameAs(saved);
        verify(cartRepository).save(any(Cart.class));
    }
}
