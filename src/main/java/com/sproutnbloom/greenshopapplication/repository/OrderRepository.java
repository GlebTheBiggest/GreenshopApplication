package com.sproutnbloom.greenshopapplication.repository;

import com.sproutnbloom.greenshopapplication.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // Знайти замовлення з елементами та продуктами і зображеннями
    @Query("""
        SELECT DISTINCT o FROM Order o
        LEFT JOIN FETCH o.orderItems oi
        LEFT JOIN FETCH oi.product p
        LEFT JOIN FETCH p.images
        WHERE o.orderId = :orderId
    """)
    Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);

    // Знайти всі замовлення певного користувача з деталями
    @Query("""
        SELECT DISTINCT o FROM Order o
        LEFT JOIN FETCH o.orderItems oi
        LEFT JOIN FETCH oi.product p
        LEFT JOIN FETCH p.images
        WHERE o.user.id = :userId
    """)
    List<Order> findByUserIdWithItems(@Param("userId") Long userId);
}