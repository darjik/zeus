package org.zeus.ims.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zeus.ims.entity.Order;
import org.zeus.ims.entity.Customer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByCustomerAndActiveTrue(Customer customer);

    List<Order> findByCustomerIdAndActiveTrue(Long customerId);

    Page<Order> findByActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    Page<Order> findByCustomerIdAndActiveTrueOrderByCreatedAtDesc(Long customerId, Pageable pageable);

    Page<Order> findByStatusAndActiveTrueOrderByCreatedAtDesc(Order.OrderStatus status, Pageable pageable);

    // Dashboard statistics methods
    long countByStatus(Order.OrderStatus status);

    long countByCreatedAtAfter(LocalDateTime createdAt);

    long countByCreatedByAndCreatedAtAfter(String createdBy, LocalDateTime createdAt);

    long countByDeliveryExpectedDateBeforeAndStatusNot(LocalDate deliveryDate, Order.OrderStatus status);

    List<Order> findTop5ByOrderByCreatedAtDesc();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = :status")
    BigDecimal sumTotalAmountByStatus(@Param("status") Order.OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = :status AND o.createdAt >= :createdAt")
    BigDecimal sumTotalAmountByStatusAndCreatedAtAfter(@Param("status") Order.OrderStatus status, @Param("createdAt") LocalDateTime createdAt);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status != :status")
    BigDecimal sumTotalAmountByStatusNot(@Param("status") Order.OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.active = true AND " +
           "(LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(o.customer.companyName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(o.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(o.purchaseOrderNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY o.createdAt DESC")
    Page<Order> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.active = true AND o.customer.id = :customerId AND " +
           "(LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(o.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(o.purchaseOrderNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY o.createdAt DESC")
    Page<Order> findByCustomerIdAndSearchTerm(@Param("customerId") Long customerId, @Param("searchTerm") String searchTerm, Pageable pageable);

    List<Order> findByDeliveryExpectedDateBetweenAndActiveTrue(LocalDateTime startDate, LocalDateTime endDate);

    List<Order> findByOrderDateBetweenAndActiveTrue(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.active = true")
    long countActiveOrders();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.customer.id = :customerId AND o.active = true")
    long countByCustomerId(@Param("customerId") Long customerId);

    List<Order> findByEnquiryIdAndActiveTrue(Long enquiryId);

    @Query("SELECT o FROM Order o WHERE o.active = true AND o.status IN :statuses ORDER BY o.createdAt DESC")
    List<Order> findByStatusInAndActiveTrue(@Param("statuses") List<Order.OrderStatus> statuses);

    @Query("SELECT o FROM Order o WHERE o.active = true AND o.deliveryExpectedDate <= :date AND o.status NOT IN ('DELIVERED', 'CANCELLED') ORDER BY o.deliveryExpectedDate ASC")
    List<Order> findOverdueOrders(@Param("date") LocalDateTime date);

    @Query("SELECT o FROM Order o WHERE o.active = true AND " +
           "(LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(o.customer.companyName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(o.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(o.purchaseOrderNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY o.createdAt DESC")
    List<Order> searchOrders(@Param("searchTerm") String searchTerm);
}
