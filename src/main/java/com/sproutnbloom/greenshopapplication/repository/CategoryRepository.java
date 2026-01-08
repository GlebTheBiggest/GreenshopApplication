package com.sproutnbloom.greenshopapplication.repository;

import com.sproutnbloom.greenshopapplication.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

//Good
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByName(String name);

    boolean existsByName(String name);
}