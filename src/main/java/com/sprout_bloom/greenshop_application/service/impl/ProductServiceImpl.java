package com.sprout_bloom.greenshop_application.service.impl;

import com.sprout_bloom.greenshop_application.dto.ProductDto;
import com.sprout_bloom.greenshop_application.entity.ImageEntity;
import com.sprout_bloom.greenshop_application.entity.Product;
import com.sprout_bloom.greenshop_application.enums.ProductCategory;
import com.sprout_bloom.greenshop_application.repository.ProductRepository;
import com.sprout_bloom.greenshop_application.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Product addProduct(ProductDto productDto) {
        log.info("Adding new product: {}", productDto.getName());

        Product product = Product.builder()
                .name(productDto.getName())
                .description(productDto.getDescription())
                .price(productDto.getPrice())
                .category(productDto.getCategory())
                .stock(productDto.getStock())
                .unit(productDto.getUnit())
                .isAvailable(productDto.getStock() > 0)
                .images(convertImageUrlsToEntities(productDto.getImageUrls()))
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Product added successfully: ID={}, Name={}", savedProduct.getId(), savedProduct.getName());

        return savedProduct;
    }

    @Override
    public void applyDiscountToProduct(ProductCategory category, BigDecimal discountPercentage) {
        log.info("Applying {}% discount to category: {}", discountPercentage, category);

        List<Product> products = productRepository.findByCategory(category);
        if (products.isEmpty()) {
            log.warn("No products found for category: {}", category);
            return;
        }

        products.forEach(product -> {
            BigDecimal discountAmount = product.getPrice()
                    .multiply(discountPercentage)
                    .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP)
                    .setScale(0, RoundingMode.HALF_UP);

            product.setPrice(product.getPrice().subtract(discountAmount)); // 🔥 Оновлюємо ціну
        });

        productRepository.saveAll(products);
        log.info("Discount applied successfully to category: {}", category);
    }

    private Set<ImageEntity> convertImageUrlsToEntities(Set<String> imageUrls) {
        if (imageUrls == null) return Set.of();
        return imageUrls.stream()
                .map(url -> ImageEntity.builder().url(url).build())
                .collect(Collectors.toSet());
    }
}