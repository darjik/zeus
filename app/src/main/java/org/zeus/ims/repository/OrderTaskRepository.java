package org.zeus.ims.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zeus.ims.entity.OrderTask;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderTaskRepository extends JpaRepository<OrderTask, Long> {

    List<OrderTask> findByOrderIdOrderByTaskTypeAsc(Long orderId);

    List<OrderTask> findByOrderIdAndStatusOrderByTaskTypeAsc(Long orderId, OrderTask.TaskStatus status);

    List<OrderTask> findByTaskTypeAndStatusOrderByCreatedAtDesc(OrderTask.TaskType taskType, OrderTask.TaskStatus status);

    List<OrderTask> findByResponsiblePersonOrderByCreatedAtDesc(String responsiblePerson);

    List<OrderTask> findByAssignedToOrderByCreatedAtDesc(String assignedTo);

    @Query("SELECT ot FROM OrderTask ot WHERE ot.order.id = :orderId AND ot.taskType = :taskType")
    Optional<OrderTask> findByOrderIdAndTaskType(@Param("orderId") Long orderId, @Param("taskType") OrderTask.TaskType taskType);

    @Query("SELECT COUNT(ot) FROM OrderTask ot WHERE ot.order.id = :orderId")
    long countByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT COUNT(ot) FROM OrderTask ot WHERE ot.order.id = :orderId AND ot.status = :status")
    long countByOrderIdAndStatus(@Param("orderId") Long orderId, @Param("status") OrderTask.TaskStatus status);

    @Query("SELECT ot FROM OrderTask ot WHERE ot.status IN :statuses ORDER BY ot.createdAt DESC")
    List<OrderTask> findByStatusInOrderByCreatedAtDesc(@Param("statuses") List<OrderTask.TaskStatus> statuses);

    @Query("SELECT ot FROM OrderTask ot WHERE ot.responsiblePerson = :person OR ot.assignedTo = :person ORDER BY ot.createdAt DESC")
    List<OrderTask> findTasksAssignedToPerson(@Param("person") String person);

    @Query("SELECT DISTINCT ot.responsiblePerson FROM OrderTask ot WHERE ot.responsiblePerson IS NOT NULL")
    List<String> findAllResponsiblePersons();

    @Query("SELECT DISTINCT ot.assignedTo FROM OrderTask ot WHERE ot.assignedTo IS NOT NULL")
    List<String> findAllAssignedPersons();
}
