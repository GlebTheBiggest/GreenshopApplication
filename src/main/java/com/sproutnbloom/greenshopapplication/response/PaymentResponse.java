package com.sproutnbloom.greenshopapplication.response;

import com.sproutnbloom.greenshopapplication.dto.OrderDto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentResponse {
    private OrderDto order;
    private String clientSecret;
}