package org.zeus.ims.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zeus.ims.entity.OrderTaskDocument;
import java.util.List;

@Repository
public interface OrderTaskDocumentRepository extends JpaRepository<OrderTaskDocument, Long> {

    List<OrderTaskDocument> findByOrderTaskIdOrderByCreatedAtDesc(Long orderTaskId);

    List<OrderTaskDocument> findByOrderTaskIdAndDocumentTypeOrderByCreatedAtDesc(Long orderTaskId, OrderTaskDocument.DocumentType documentType);

    @Query("SELECT COUNT(otd) FROM OrderTaskDocument otd WHERE otd.orderTask.id = :orderTaskId")
    long countByOrderTaskId(@Param("orderTaskId") Long orderTaskId);

    @Query("SELECT COUNT(otd) FROM OrderTaskDocument otd WHERE otd.orderTask.id = :orderTaskId AND otd.documentType = :documentType")
    long countByOrderTaskIdAndDocumentType(@Param("orderTaskId") Long orderTaskId, @Param("documentType") OrderTaskDocument.DocumentType documentType);

    @Query("SELECT otd FROM OrderTaskDocument otd WHERE otd.orderTask.order.id = :orderId ORDER BY otd.createdAt DESC")
    List<OrderTaskDocument> findByOrderIdOrderByCreatedAtDesc(@Param("orderId") Long orderId);
}
