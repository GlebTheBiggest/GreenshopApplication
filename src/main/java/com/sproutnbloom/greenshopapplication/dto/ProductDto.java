package com.sproutnbloom.greenshopapplication.dto;

import com.sproutnbloom.greenshopapplication.model.Category;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class ProductDto {
    private Long id;
    private String name;
    private String brand;
    private BigDecimal price;
    private int stock;
    private String description;
    private Category category;
    private Set<ImageDto> images;
}