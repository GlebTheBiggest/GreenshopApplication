package com.sproutnbloom.greenshopapplication.repository;

import com.sproutnbloom.greenshopapplication.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.images WHERE p.id = :id")
    Optional<Product> findByIdWithImages(@Param("id") Long id);

    // Знайти всі продукти із зображеннями
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.images")
    List<Product> findAllWithImages();

    // Знайти продукти за назвою категорії із зображеннями
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.images WHERE p.category.name = :category")
    List<Product> findByCategoryNameWithImages(@Param("category") String category);

    // Знайти продукти за брендом із зображеннями
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.images WHERE p.brand = :brand")
    List<Product> findByBrandWithImages(@Param("brand") String brand);

    // Знайти продукти за назвою категорії та брендом із зображеннями
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.images WHERE p.category.name = :category AND p.brand = :brand")
    List<Product> findByCategoryNameAndBrandWithImages(@Param("category") String category, @Param("brand") String brand);

    // Знайти продукти за частковою назвою із зображеннями (нечутливо до регістру)
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.images WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Product> findByNameWithImages(@Param("name") String name);

    // Знайти продукти за брендом і частковою назвою із зображеннями
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.images WHERE p.brand = :brand AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Product> findByBrandAndNameWithImages(@Param("brand") String brand, @Param("name") String name);

    // Підрахувати продукти за брендом і назвою (зображення не потрібні)
    @Query("SELECT COUNT(p) FROM Product p WHERE p.brand = :brand AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Long countByBrandAndName(String brand, String name);
}