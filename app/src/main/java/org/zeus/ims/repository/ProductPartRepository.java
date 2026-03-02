package org.zeus.ims.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zeus.ims.entity.ProductPart;

import java.util.List;

@Repository
public interface ProductPartRepository extends JpaRepository<ProductPart, Long> {

    /**
     * Find parts by product id
     */
    List<ProductPart> findByProductIdOrderByPartName(Long productId);

    /**
     * Find active parts by product id
     */
    List<ProductPart> findByProductIdAndActiveTrueOrderByPartName(Long productId);

    /**
     * Find parts by vendor id
     */
    List<ProductPart> findByVendorIdOrderByPartName(Long vendorId);

    /**
     * Find parts by search term
     */
    @Query("SELECT pp FROM ProductPart pp WHERE LOWER(pp.partName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(pp.partNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(pp.description) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(pp.material) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "ORDER BY pp.partName")
    List<ProductPart> findBySearchTerm(@Param("search") String search);

    /**
     * Check if part name exists for a product
     */
    boolean existsByPartNameAndProductId(String partName, Long productId);

    /**
     * Check if part name exists for a product excluding specific id
     */
    boolean existsByPartNameAndProductIdAndIdNot(String partName, Long productId, Long id);

    /**
     * Count parts by product id
     */
    long countByProductId(Long productId);

    /**
     * Count active parts by product id
     */
    long countByProductIdAndActiveTrue(Long productId);

    /**
     * Count parts by vendor id
     */
    long countByVendorId(Long vendorId);

    /**
     * Find parts without vendor assigned
     */
    List<ProductPart> findByVendorIsNullOrderByPartName();

    /**
     * Delete parts by product id
     */
    void deleteByProductId(Long productId);
}
