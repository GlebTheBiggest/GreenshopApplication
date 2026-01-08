package com.sproutnbloom.greenshopapplication.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class OrderItemDto {
    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal price;
    private Set<ImageDto> productImages;
}