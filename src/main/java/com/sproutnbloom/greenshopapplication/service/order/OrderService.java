package com.sproutnbloom.greenshopapplication.service.order;

import com.sproutnbloom.greenshopapplication.dto.ImageDto;
import com.sproutnbloom.greenshopapplication.dto.OrderDto;
import com.sproutnbloom.greenshopapplication.dto.OrderItemDto;
import com.sproutnbloom.greenshopapplication.enums.OrderStatus;
import com.sproutnbloom.greenshopapplication.exceptions.InsufficientStockException;
import com.sproutnbloom.greenshopapplication.exceptions.ResourceNotFoundException;
import com.sproutnbloom.greenshopapplication.model.*;
import com.sproutnbloom.greenshopapplication.repository.OrderRepository;
import com.sproutnbloom.greenshopapplication.repository.ProductRepository;
import com.sproutnbloom.greenshopapplication.request.UpdateOrderStatusRequest;
import com.sproutnbloom.greenshopapplication.request.UpdateProductRequest;
import com.sproutnbloom.greenshopapplication.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final ModelMapper modelMapper;


    @Transactional
    @Override
    public Order placeOrder(Long userId) {
        Cart cart = cartService.getCartByUserId(userId);
        Order order = createOrder(cart);
        List<OrderItem> orderItemList = createOrderItems(order, cart);
        order.setOrderItems(new HashSet<>(orderItemList));
        order.setTotalAmount(calculateTotalAmount(orderItemList));
        Order savedOrder = orderRepository.save(order);
        orderRepository.flush();
        cartService.clearCart(cart.getId());
        return savedOrder;
    }

    private Order createOrder(Cart cart) {
        Order order = new Order();
        order.setUser(cart.getUser());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    private List<OrderItem> createOrderItems(Order order, Cart cart) {
        return cart.getItems().stream().map(cartItem -> {
            // Беремо товар напряму з репозиторію, а не через закешований ProductService.getProductById:
            // перевірка/списання залишку має працювати з гарантовано свіжими даними, а не тими,
            // що могли закешуватись до 10 хвилин тому.
            Product product = productRepository.findByIdWithImages(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found!"));

            // Перевірити наявність товару
            if (product.getStock() < cartItem.getQuantity()) {
                throw new InsufficientStockException("Not enough stock for product: " + product.getName());
            }

            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            return new OrderItem(
                    order,
                    product,
                    cartItem.getQuantity(),
                    cartItem.getUnitPrice()
            );
        }).toList();
    }

    private BigDecimal calculateTotalAmount(List<OrderItem> orderItemList) {
        return orderItemList
                .stream()
                .map(item -> item.getPrice()
                        .multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    @Override
    public OrderDto getOrder(Long orderId) {
        return orderRepository.findByIdWithItems(orderId)
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    @Override
    public List<OrderDto> getUserOrders(Long userId) {
        List<Order> orders = orderRepository.findByUserIdWithItems(userId);
        return orders.stream().map(this::convertToDto).toList();
    }

    @Override
    @Transactional
    public OrderDto updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        order.setOrderStatus(newStatus);
        orderRepository.save(order);
        return convertToDto(order);
    }


    @Override
    public OrderDto convertToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getOrderId());
        dto.setUserId(order.getUser().getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getOrderStatus().toString());

        // Переконайтеся, що orderItems ініціалізовані
        if (order.getOrderItems() != null) {
            List<OrderItemDto> itemDtos = order.getOrderItems().stream()
                    .map(this::convertToOrderItemDto)
                    .collect(Collectors.toList());
            dto.setItems(itemDtos);
        }

        return dto;
    }

    @Override
    public OrderItemDto convertToOrderItemDto(OrderItem item) {
        OrderItemDto dto = new OrderItemDto();
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());

        // Переконайтеся, що зображення ініціалізовані
        if (item.getProduct().getImages() != null) {
            Set<ImageDto> imageDtos = item.getProduct().getImages().stream()
                    .map(image -> modelMapper.map(image, ImageDto.class))
                    .collect(Collectors.toSet());
            dto.setProductImages(imageDtos);
        }

        return dto;
    }
}