package com.sproutnbloom.greenshopapplication.service.order;

import com.sproutnbloom.greenshopapplication.dto.OrderDto;
import com.sproutnbloom.greenshopapplication.dto.OrderItemDto;
import com.sproutnbloom.greenshopapplication.enums.OrderStatus;
import com.sproutnbloom.greenshopapplication.model.Order;
import com.sproutnbloom.greenshopapplication.model.OrderItem;
import com.sproutnbloom.greenshopapplication.request.UpdateOrderStatusRequest;

import java.util.List;

public interface IOrderService {
    Order placeOrder(Long userId);
    OrderDto getOrder(Long orderId);
    List<OrderDto> getUserOrders(Long userId);
    OrderDto convertToDto(Order order);
    OrderItemDto convertToOrderItemDto(OrderItem item);
    OrderDto updateOrderStatus(Long orderId, OrderStatus newStatus);
}
