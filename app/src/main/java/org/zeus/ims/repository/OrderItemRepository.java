package org.zeus.ims.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zeus.ims.entity.OrderItem;
import org.zeus.ims.entity.Order;
import org.zeus.ims.entity.Product;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder(Order order);

    List<OrderItem> findByOrderId(Long orderId);

    List<OrderItem> findByProduct(Product product);

    List<OrderItem> findByProductId(Long productId);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId ORDER BY oi.createdAt ASC")
    List<OrderItem> findByOrderIdOrderByCreatedAt(@Param("orderId") Long orderId);

    @Query("SELECT SUM(oi.totalPrice) FROM OrderItem oi WHERE oi.order.id = :orderId")
    java.math.BigDecimal calculateTotalAmountByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.order.id = :orderId")
    long countByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.product.id = :productId AND oi.order.active = true")
    List<OrderItem> findByProductIdAndOrderActive(@Param("productId") Long productId);

    void deleteByOrderId(Long orderId);
}
