package com.sprout_bloom.greenshop_application.service;

import com.sprout_bloom.greenshop_application.dto.ProductDto;
import com.sprout_bloom.greenshop_application.entity.Product;
import com.sprout_bloom.greenshop_application.enums.ProductCategory;

import java.math.BigDecimal;


public interface ProductService {

    Product addProduct(ProductDto productDto);

    void applyDiscountToProduct(ProductCategory category, BigDecimal discountPercentage);

}
