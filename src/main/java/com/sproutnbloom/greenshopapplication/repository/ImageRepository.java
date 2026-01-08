package com.sproutnbloom.greenshopapplication.repository;

import com.sproutnbloom.greenshopapplication.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

//Good
public interface ImageRepository extends JpaRepository<Image, Long> {
    Set<Image> findByProductId(Long id);
}