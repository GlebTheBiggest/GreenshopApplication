package com.sproutnbloom.greenshopapplication.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class CartDto {
    private Long cartId;
    private BigDecimal totalAmount;
    private Set<CartItemDto> items;
}