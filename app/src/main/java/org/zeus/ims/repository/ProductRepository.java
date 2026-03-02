package org.zeus.ims.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zeus.ims.entity.Product;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find products by name, model number, brand, or category containing the search term (case-insensitive)
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.modelNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.category) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "ORDER BY p.name")
    List<Product> findBySearchTerm(@Param("search") String search);

    /**
     * Find all active products ordered by name
     */
    List<Product> findByActiveTrueOrderByName();

    /**
     * Find all active products ordered by name ascending
     */
    List<Product> findByActiveTrueOrderByNameAsc();

    /**
     * Find all products ordered by name
     */
    List<Product> findAllByOrderByName();

    /**
     * Find products by category ordered by name
     */
    List<Product> findByCategoryOrderByName(String category);

    /**
     * Find products by brand ordered by name
     */
    List<Product> findByBrandOrderByName(String brand);

    /**
     * Check if product name exists
     */
    boolean existsByName(String name);

    /**
     * Check if product name exists excluding specific id
     */
    boolean existsByNameAndIdNot(String name, Long id);

    /**
     * Find product by name (case-insensitive)
     */
    Optional<Product> findByNameIgnoreCase(String name);

    /**
     * Get distinct categories
     */
    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.category IS NOT NULL ORDER BY p.category")
    List<String> findDistinctCategories();

    /**
     * Get distinct brands
     */
    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.brand IS NOT NULL ORDER BY p.brand")
    List<String> findDistinctBrands();

    /**
     * Count total active products
     */
    long countByActiveTrue();

    /**
     * Count products by category
     */
    long countByCategory(String category);
}
