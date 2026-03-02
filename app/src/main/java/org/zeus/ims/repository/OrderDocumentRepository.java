package org.zeus.ims.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zeus.ims.entity.OrderDocument;
import org.zeus.ims.entity.Order;
import java.util.List;

@Repository
public interface OrderDocumentRepository extends JpaRepository<OrderDocument, Long> {

    List<OrderDocument> findByOrder(Order order);

    List<OrderDocument> findByOrderId(Long orderId);

    List<OrderDocument> findByOrderIdOrderByCreatedAtDesc(Long orderId);

    List<OrderDocument> findByDocumentType(OrderDocument.DocumentType documentType);

    @Query("SELECT od FROM OrderDocument od WHERE od.order.id = :orderId AND od.documentType = :documentType ORDER BY od.createdAt DESC")
    List<OrderDocument> findByOrderIdAndDocumentType(@Param("orderId") Long orderId, @Param("documentType") OrderDocument.DocumentType documentType);

    @Query("SELECT COUNT(od) FROM OrderDocument od WHERE od.order.id = :orderId")
    long countByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT COUNT(od) FROM OrderDocument od WHERE od.order.id = :orderId AND od.documentType = :documentType")
    long countByOrderIdAndDocumentType(@Param("orderId") Long orderId, @Param("documentType") OrderDocument.DocumentType documentType);

    void deleteByOrderId(Long orderId);
}
