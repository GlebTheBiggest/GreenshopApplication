package com.sproutnbloom.greenshopapplication.service.category;

import com.sproutnbloom.greenshopapplication.exceptions.AlreadyExistsException;
import com.sproutnbloom.greenshopapplication.exceptions.ResourceNotFoundException;
import com.sproutnbloom.greenshopapplication.model.Category;
import com.sproutnbloom.greenshopapplication.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService {
    private final CategoryRepository categoryRepository;

    @Cacheable(value = "categories", key = "#id")
    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found!"));
    }

    @Cacheable(value = "categories", key = "'name:' + #name")
    @Override
    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }

    @Cacheable(value = "categories", key = "'all'")
    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @CacheEvict(value = "categories", allEntries = true)
    @Override
    public Category addCategory(Category category) {
        return Optional.of(category).filter(c -> !categoryRepository.existsByName(c.getName()))
                .map(categoryRepository::save)
                .orElseThrow(() -> new AlreadyExistsException(category.getName() + " already exists"));
    }

    @Caching(evict = {
            @CacheEvict(value = "categories", allEntries = true),
            @CacheEvict(value = "products", allEntries = true)
    })
    @Override
    public Category updateCategory(Category category, Long id) {
        return Optional.ofNullable(getCategoryById(id)).map(oldCategory -> {
            oldCategory.setName(category.getName());
            return categoryRepository.save(oldCategory);
        }).orElseThrow(() -> new ResourceNotFoundException("Category not found!"));
    }


    @Caching(evict = {
            @CacheEvict(value = "categories", allEntries = true),
            @CacheEvict(value = "products", allEntries = true)
    })
    @Override
    public void deleteCategoryById(Long id) {
        categoryRepository.findById(id)
                .ifPresentOrElse(categoryRepository::delete, () -> {
                    throw new ResourceNotFoundException("Category not found!");
                });

    }
}