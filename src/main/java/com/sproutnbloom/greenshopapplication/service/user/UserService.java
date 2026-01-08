package com.sproutnbloom.greenshopapplication.service.user;

import com.sproutnbloom.greenshopapplication.dto.*;
import com.sproutnbloom.greenshopapplication.exceptions.AlreadyExistsException;
import com.sproutnbloom.greenshopapplication.exceptions.ResourceNotFoundException;
import com.sproutnbloom.greenshopapplication.model.*;
import com.sproutnbloom.greenshopapplication.repository.UserRepository;
import com.sproutnbloom.greenshopapplication.request.CreateUserRequest;
import com.sproutnbloom.greenshopapplication.request.UpdateUserRequest;
import com.sproutnbloom.greenshopapplication.service.order.IOrderService;
import com.sproutnbloom.greenshopapplication.service.product.IProductService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final IProductService productService;
    private final IOrderService orderService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    @Override
    public UserDto getUserById(Long userId) {
        User user = userRepository.findByIdWithDetails(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return convertUserToDto(user);
    }

    @Transactional
    @Override
    public User createUser(CreateUserRequest request) {
        return Optional.of(request)
                .filter(user -> !userRepository.existsByEmail(request.getEmail()))
                .map(req -> {
                    User user = new User();
                    user.setEmail(request.getEmail());
                    user.setPassword(passwordEncoder.encode(request.getPassword()));
                    user.setFirstname(request.getFirstName());
                    user.setLastname(request.getLastName());
                    user.setRole(request.getRole());
                    return userRepository.save(user);
                }).orElseThrow(() -> new AlreadyExistsException("Oops!" + request.getEmail() + " already exists!"));
    }

    @Transactional
    @Override
    public User updateUser(UpdateUserRequest request, Long userId) {
        return userRepository.findByIdWithDetails(userId).map(existingUser -> {
            existingUser.setFirstname(request.getFirstName());
            existingUser.setLastname(request.getLastName());
            return userRepository.save(existingUser);
        }).orElseThrow(() -> new ResourceNotFoundException("User not found!"));

    }

    @Transactional
    @Override
    public void deleteUser(Long userId) {
        userRepository.findByIdWithDetails(userId).ifPresentOrElse(userRepository::delete, () -> {
            throw new ResourceNotFoundException("User not found!");
        });
    }

    @Override
    public UserDto convertUserToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstname());
        dto.setLastName(user.getLastname());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().toString());

        // Маппування корзини
        if (user.getCart() != null) {
            dto.setCart(convertCartToDto(user.getCart()));
        }

        // Маппування замовлень
        if (user.getOrders() != null) {
            Set<OrderDto> orderDtos = user.getOrders().stream()
                    .map(this::convertOrderToDto)
                    .collect(Collectors.toSet());
            dto.setOrders(orderDtos);
        }

        return dto;
    }

    private CartDto convertCartToDto(Cart cart) {
        CartDto dto = new CartDto();
        dto.setCartId(cart.getId());
        dto.setTotalAmount(cart.getTotalAmount());

        if (cart.getItems() != null) {
            Set<CartItemDto> itemDtos = cart.getItems().stream()
                    .map(this::convertCartItemToDto)
                    .collect(Collectors.toSet());
            dto.setItems(itemDtos);
        }

        return dto;
    }

    private CartItemDto convertCartItemToDto(CartItem item) {
        CartItemDto dto = new CartItemDto();
        dto.setItemId(item.getId());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());

        if (item.getProduct() != null) {
            dto.setProduct(productService.convertToDto(item.getProduct()));
        }

        return dto;
    }

    private OrderDto convertOrderToDto(Order order) {
        return orderService.convertToDto(order);
    }
}