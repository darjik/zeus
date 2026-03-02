package org.zeus.ims.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zeus.ims.entity.VendorPersonnel;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorPersonnelRepository extends JpaRepository<VendorPersonnel, Long> {

    /**
     * Find all personnel for a specific vendor
     */
    List<VendorPersonnel> findByVendorIdOrderByFullName(Long vendorId);

    /**
     * Find active personnel for a specific vendor
     */
    List<VendorPersonnel> findByVendorIdAndActiveTrueOrderByFullName(Long vendorId);

    /**
     * Find personnel by search term within a vendor
     */
    @Query("SELECT vp FROM VendorPersonnel vp WHERE vp.vendor.id = :vendorId AND " +
           "(LOWER(vp.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(vp.designation) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(vp.department) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(vp.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(vp.contactNumber) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY vp.fullName")
    List<VendorPersonnel> findByVendorIdAndSearchTerm(@Param("vendorId") Long vendorId, @Param("search") String search);

    /**
     * Find primary contact for a vendor
     */
    Optional<VendorPersonnel> findByVendorIdAndIsPrimaryContactTrueAndActiveTrue(Long vendorId);

    /**
     * Count active personnel for a vendor
     */
    long countByVendorIdAndActiveTrue(Long vendorId);

    /**
     * Count all personnel for a vendor
     */
    long countByVendorId(Long vendorId);

    /**
     * Check if email exists for personnel within vendor (case-insensitive)
     */
    @Query("SELECT COUNT(vp) > 0 FROM VendorPersonnel vp WHERE vp.vendor.id = :vendorId AND LOWER(vp.email) = LOWER(:email)")
    boolean existsByVendorIdAndEmailIgnoreCase(@Param("vendorId") Long vendorId, @Param("email") String email);

    /**
     * Check if email exists for personnel within vendor excluding specific personnel (for updates)
     */
    @Query("SELECT COUNT(vp) > 0 FROM VendorPersonnel vp WHERE vp.vendor.id = :vendorId AND LOWER(vp.email) = LOWER(:email) AND vp.id != :personnelId")
    boolean existsByVendorIdAndEmailIgnoreCaseAndIdNot(@Param("vendorId") Long vendorId, @Param("email") String email, @Param("personnelId") Long personnelId);

    /**
     * Find all personnel by designation
     */
    @Query("SELECT vp FROM VendorPersonnel vp WHERE LOWER(vp.designation) = LOWER(:designation) AND vp.active = true ORDER BY vp.fullName")
    List<VendorPersonnel> findByDesignationIgnoreCaseAndActiveTrue(@Param("designation") String designation);

    /**
     * Find all personnel by department
     */
    @Query("SELECT vp FROM VendorPersonnel vp WHERE LOWER(vp.department) = LOWER(:department) AND vp.active = true ORDER BY vp.fullName")
    List<VendorPersonnel> findByDepartmentIgnoreCaseAndActiveTrue(@Param("department") String department);

    /**
     * Delete all personnel for a vendor
     */
    void deleteByVendorId(Long vendorId);
}
