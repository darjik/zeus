package org.zeus.ims.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zeus.ims.entity.Enquiry;
import org.zeus.ims.entity.Enquiry.EnquiryStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnquiryRepository extends JpaRepository<Enquiry, Long> {

    List<Enquiry> findByActiveTrue();

    Page<Enquiry> findByActiveTrue(Pageable pageable);

    Optional<Enquiry> findByIdAndActiveTrue(Long id);

    Optional<Enquiry> findByEnquiryNumber(String enquiryNumber);

    List<Enquiry> findByCustomerIdAndActiveTrue(Long customerId);

    // Dashboard statistics methods
    long countByStatus(EnquiryStatus status);

    long countByCreatedAtAfter(LocalDateTime createdAt);

    long countByCreatedBy(String createdBy);

    long countByCreatedByAndStatus(String createdBy, EnquiryStatus status);

    long countByCreatedByAndCreatedAtAfter(String createdBy, LocalDateTime createdAt);

    long countByCreatedByAndStatusAndUpdatedAtBefore(String createdBy, EnquiryStatus status, LocalDateTime updatedAt);

    List<Enquiry> findTop5ByOrderByCreatedAtDesc();

    List<Enquiry> findTop5ByCreatedByAndStatusOrderByCreatedAtDesc(String createdBy, EnquiryStatus status);

    @Query("SELECT DISTINCT e FROM Enquiry e JOIN e.items i WHERE i.product.id = :productId AND e.active = true")
    List<Enquiry> findByProductIdAndActiveTrue(@Param("productId") Long productId);

    List<Enquiry> findByStatusAndActiveTrue(EnquiryStatus status);

    @Query("SELECT e FROM Enquiry e WHERE e.active = true AND " +
           "(LOWER(e.enquiryNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.customer.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.tags) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Enquiry> searchEnquiries(@Param("search") String search);

    @Query("SELECT COUNT(e) FROM Enquiry e WHERE e.active = true")
    long countActiveEnquiries();

    @Query("SELECT e FROM Enquiry e WHERE e.active = true AND e.quoteValidUntil < :date")
    List<Enquiry> findExpiredQuotes(@Param("date") LocalDateTime date);

    @Query("SELECT e FROM Enquiry e WHERE e.active = true AND e.deliveryExpectedDate BETWEEN :startDate AND :endDate")
    List<Enquiry> findByDeliveryDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT DISTINCT e FROM Enquiry e LEFT JOIN e.items i WHERE e.active = true AND " +
           "(:customerId IS NULL OR e.customer.id = :customerId) AND " +
           "(:productId IS NULL OR i.product.id = :productId) AND " +
           "(:status IS NULL OR e.status = :status) AND " +
           "(:priority IS NULL OR e.priority = :priority)")
    List<Enquiry> findWithFilters(@Param("customerId") Long customerId,
                                  @Param("productId") Long productId,
                                  @Param("status") EnquiryStatus status,
                                  @Param("priority") Enquiry.EnquiryPriority priority);
}
