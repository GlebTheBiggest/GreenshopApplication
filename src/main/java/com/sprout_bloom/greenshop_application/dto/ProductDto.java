package com.sprout_bloom.greenshop_application.dto;

import com.sprout_bloom.greenshop_application.enums.ProductCategory;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Set;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {

    @NotNull(message = "Product ID is required")
    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 100, message = "Product name must be between 3 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot be longer than 500 characters")
    private String description;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price cannot be negative")
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Category is required")
    private ProductCategory category;

    @Min(value = 0, message = "Stock cannot be negative")
    private int stock;

    @Size(max = 50, message = "Unit cannot be longer than 50 characters")
    private String unit;

    @NotNull(message = "Images are required")
    private Set<String> imageUrls;

}