package com.sproutnbloom.greenshopapplication.repository;

import com.sproutnbloom.greenshopapplication.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @Query("""
        SELECT DISTINCT c FROM Cart c
        LEFT JOIN FETCH c.items i
        LEFT JOIN FETCH i.product p
        LEFT JOIN FETCH p.images
        WHERE c.user.id = :userId
    """)
    Optional<Cart> findByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT DISTINCT c FROM Cart c
        LEFT JOIN FETCH c.items i
        LEFT JOIN FETCH i.product p
        LEFT JOIN FETCH p.images
        WHERE c.id = :cartId
    """)
    Optional<Cart> findByIdWithItems(@Param("cartId") Long cartId);
}
