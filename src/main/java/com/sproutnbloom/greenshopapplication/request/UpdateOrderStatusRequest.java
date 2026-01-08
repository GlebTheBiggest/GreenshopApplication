package com.sproutnbloom.greenshopapplication.request;

import com.sproutnbloom.greenshopapplication.enums.OrderStatus;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    private OrderStatus status;
}
