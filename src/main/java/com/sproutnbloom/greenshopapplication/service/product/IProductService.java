package com.sproutnbloom.greenshopapplication.service.product;

import com.sproutnbloom.greenshopapplication.dto.ProductDto;
import com.sproutnbloom.greenshopapplication.model.Product;
import com.sproutnbloom.greenshopapplication.request.AddProductRequest;
import com.sproutnbloom.greenshopapplication.request.UpdateProductRequest;
import java.util.List;

public interface IProductService {
    Product addProduct(AddProductRequest product);
    Product getProductById(Long id);
    void deleteProductById(Long id);
    Product updateProduct(UpdateProductRequest product, Long productId);
    List<Product> getAllProducts();
    List<Product> getProductsByCategory(String category);
    List<Product> getProductsByBrand(String brand);
    List<Product> getProductsByCategoryAndBrand(String category, String brand);
    List<Product> getProductsByName(String name);
    List<Product> getProductsByBrandAndName(String category, String name);
    Long countProductsByBrandAndName(String brand, String name);

    List<ProductDto> getConvertedProducts(List<Product> products);

    ProductDto convertToDto(Product product);
}