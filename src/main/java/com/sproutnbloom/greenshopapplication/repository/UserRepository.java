package com.sproutnbloom.greenshopapplication.repository;

import com.sproutnbloom.greenshopapplication.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = {
            "cart",
            "cart.items",
            "cart.items.product",
            "cart.items.product.category",
            "cart.items.product.images",
            "orders"
    })
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithDetails(@Param("id") Long id);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
}