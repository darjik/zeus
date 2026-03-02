package org.zeus.ims.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zeus.ims.entity.Customer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByActiveTrue();

    List<Customer> findByActiveTrueOrderByCompanyNameAsc();

    Optional<Customer> findByCompanyNameAndActiveTrue(String companyName);

    boolean existsByCompanyNameAndActiveTrue(String companyName);

    long countByActiveTrue();

    long countByActiveTrueAndCreatedAtAfter(LocalDateTime createdAt);

    @Query("SELECT c FROM Customer c WHERE c.active = true AND " +
           "(LOWER(c.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.contactNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.city) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.state) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.country) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Customer> findActiveCustomersBySearch(@Param("search") String search);

    @Query("SELECT c FROM Customer c WHERE " +
           "(LOWER(c.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.contactNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.city) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.state) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.country) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Customer> findCustomersBySearch(@Param("search") String search);

    @Query("SELECT c FROM Customer c WHERE c.active = true AND " +
           "(LOWER(c.city) LIKE LOWER(CONCAT('%', :location, '%')) OR " +
           "LOWER(c.state) LIKE LOWER(CONCAT('%', :location, '%')) OR " +
           "LOWER(c.country) LIKE LOWER(CONCAT('%', :location, '%')))")
    List<Customer> findActiveCustomersByLocation(@Param("location") String location);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.active = true")
    long countActiveCustomers();

    @Query("SELECT DISTINCT c.city FROM Customer c WHERE c.active = true AND c.city IS NOT NULL ORDER BY c.city")
    List<String> findDistinctCities();

    @Query("SELECT DISTINCT c.state FROM Customer c WHERE c.active = true AND c.state IS NOT NULL ORDER BY c.state")
    List<String> findDistinctStates();

    @Query("SELECT DISTINCT c.country FROM Customer c WHERE c.active = true AND c.country IS NOT NULL ORDER BY c.country")
    List<String> findDistinctCountries();
}
