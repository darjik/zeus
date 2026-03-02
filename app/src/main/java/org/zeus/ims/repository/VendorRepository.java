package org.zeus.ims.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zeus.ims.entity.Vendor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {

    /**
     * Find vendors by company name containing the search term (case-insensitive)
     */
    @Query("SELECT v FROM Vendor v WHERE LOWER(v.companyName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(v.description) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(v.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(v.contactNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "ORDER BY v.companyName")
    List<Vendor> findBySearchTerm(@Param("search") String search);

    /**
     * Find all active vendors ordered by company name
     */
    List<Vendor> findByActiveTrueOrderByCompanyName();

    /**
     * Find all vendors ordered by company name
     */
    List<Vendor> findAllByOrderByCompanyName();

    /**
     * Check if a company name exists (case-insensitive)
     */
    @Query("SELECT COUNT(v) > 0 FROM Vendor v WHERE LOWER(v.companyName) = LOWER(:companyName)")
    boolean existsByCompanyNameIgnoreCase(@Param("companyName") String companyName);

    /**
     * Check if a company name exists excluding a specific vendor (for updates)
     */
    @Query("SELECT COUNT(v) > 0 FROM Vendor v WHERE LOWER(v.companyName) = LOWER(:companyName) AND v.id != :vendorId")
    boolean existsByCompanyNameIgnoreCaseAndIdNot(@Param("companyName") String companyName, @Param("vendorId") Long vendorId);

    /**
     * Find vendor by company name (case-insensitive)
     */
    @Query("SELECT v FROM Vendor v WHERE LOWER(v.companyName) = LOWER(:companyName)")
    Optional<Vendor> findByCompanyNameIgnoreCase(@Param("companyName") String companyName);

    /**
     * Count all active vendors
     */
    long countByActiveTrue();

    /**
     * Count all active vendors created after a specific date
     */
    long countByActiveTrueAndCreatedAtAfter(LocalDateTime createdAt);

    /**
     * Find vendors by city (case-insensitive)
     */
    @Query("SELECT v FROM Vendor v WHERE LOWER(v.city) = LOWER(:city) ORDER BY v.companyName")
    List<Vendor> findByCityIgnoreCase(@Param("city") String city);

    /**
     * Find vendors by state (case-insensitive)
     */
    @Query("SELECT v FROM Vendor v WHERE LOWER(v.state) = LOWER(:state) ORDER BY v.companyName")
    List<Vendor> findByStateIgnoreCase(@Param("state") String state);

    /**
     * Find vendors by country (case-insensitive)
     */
    @Query("SELECT v FROM Vendor v WHERE LOWER(v.country) = LOWER(:country) ORDER BY v.companyName")
    List<Vendor> findByCountryIgnoreCase(@Param("country") String country);
}
