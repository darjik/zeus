package org.zeus.ims.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zeus.ims.entity.OrderItemPart;
import java.util.List;

@Repository
public interface OrderItemPartRepository extends JpaRepository<OrderItemPart, Long> {

    List<OrderItemPart> findByOrderItemIdOrderByCreatedAt(Long orderItemId);

    void deleteByOrderItemId(Long orderItemId);
}

