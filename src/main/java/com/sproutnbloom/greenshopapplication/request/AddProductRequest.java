package com.sproutnbloom.greenshopapplication.request;

import com.sproutnbloom.greenshopapplication.model.Category;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddProductRequest {
    private Long id;
    private String name;
    private String brand;
    private BigDecimal price;
    private int stock;
    private String description;
    private Category category;
}