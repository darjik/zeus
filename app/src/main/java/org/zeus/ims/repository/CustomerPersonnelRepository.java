package org.zeus.ims.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zeus.ims.entity.Customer;
import org.zeus.ims.entity.CustomerPersonnel;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerPersonnelRepository extends JpaRepository<CustomerPersonnel, Long> {

    List<CustomerPersonnel> findByCustomerAndActiveTrue(Customer customer);

    List<CustomerPersonnel> findByCustomerIdAndActiveTrue(Long customerId);

    List<CustomerPersonnel> findByCustomerOrderByFullNameAsc(Customer customer);

    Optional<CustomerPersonnel> findByCustomerAndIsPrimaryContactTrueAndActiveTrue(Customer customer);

    Optional<CustomerPersonnel> findByCustomerIdAndIsPrimaryContactTrueAndActiveTrue(Long customerId);

    boolean existsByCustomerAndEmailAndActiveTrue(Customer customer, String email);

    boolean existsByCustomerIdAndEmailAndActiveTrue(Long customerId, String email);

    @Query("SELECT cp FROM CustomerPersonnel cp WHERE cp.customer.id = :customerId AND cp.active = true AND " +
           "(LOWER(cp.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(cp.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(cp.contactNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(cp.department) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(cp.designation) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<CustomerPersonnel> findActivePersonnelByCustomerAndSearch(@Param("customerId") Long customerId,
                                                                  @Param("search") String search);

    @Query("SELECT cp FROM CustomerPersonnel cp WHERE cp.active = true AND " +
           "(LOWER(cp.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(cp.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(cp.contactNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(cp.department) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(cp.designation) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<CustomerPersonnel> findActivePersonnelBySearch(@Param("search") String search);

    @Query("SELECT COUNT(cp) FROM CustomerPersonnel cp WHERE cp.customer.id = :customerId AND cp.active = true")
    long countActivePersonnelByCustomer(@Param("customerId") Long customerId);

    @Query("SELECT DISTINCT cp.department FROM CustomerPersonnel cp WHERE cp.active = true AND cp.department IS NOT NULL ORDER BY cp.department")
    List<String> findDistinctDepartments();

    @Query("SELECT DISTINCT cp.designation FROM CustomerPersonnel cp WHERE cp.active = true AND cp.designation IS NOT NULL ORDER BY cp.designation")
    List<String> findDistinctDesignations();

    @Query("SELECT cp FROM CustomerPersonnel cp WHERE cp.customer.id = :customerId AND cp.active = true ORDER BY " +
           "CASE WHEN cp.isPrimaryContact = true THEN 0 ELSE 1 END, cp.fullName ASC")
    List<CustomerPersonnel> findActivePersonnelByCustomerOrderByPrimaryContactFirst(@Param("customerId") Long customerId);
}
