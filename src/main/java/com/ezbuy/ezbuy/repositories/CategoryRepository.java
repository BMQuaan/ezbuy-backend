package com.ezbuy.ezbuy.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ezbuy.ezbuy.entities.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    boolean existsByName(String name);
    boolean existsBySlug(String slug);
    boolean existsByParentId(Integer parentId);
    boolean existsBySlugAndIdNot(String slug, Integer id);
    boolean existsByNameAndIdNot(String name, Integer id);

    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.category.id = :categoryId")
    boolean hasProducts(Integer categoryId);
    Page<Category> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.children WHERE c.parent IS NULL")
    List<Category> findRootCategoriesWithChildren();

    Optional<Category> findByName(String name);
}