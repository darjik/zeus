package org.zeus.ims.dto;

import org.zeus.ims.entity.OrderTask;
import java.time.LocalDateTime;
import java.util.List;

public class OrderTaskDTO {

    private Long id;
    private Long orderId;
    private String orderNumber;
    private String taskType;
    private String status;
    private String description;
    private String responsiblePerson;
    private String assignedTo;
    private LocalDateTime dueDate;
    private LocalDateTime completedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private List<OrderTaskDocumentDTO> documents;
    private int documentCount;

    public OrderTaskDTO() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResponsiblePerson() {
        return responsiblePerson;
    }

    public void setResponsiblePerson(String responsiblePerson) {
        this.responsiblePerson = responsiblePerson;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(LocalDateTime completedDate) {
        this.completedDate = completedDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public List<OrderTaskDocumentDTO> getDocuments() {
        return documents;
    }

    public void setDocuments(List<OrderTaskDocumentDTO> documents) {
        this.documents = documents;
    }

    public int getDocumentCount() {
        return documentCount;
    }

    public void setDocumentCount(int documentCount) {
        this.documentCount = documentCount;
    }

    // Helper methods
    public OrderTask.TaskType getTaskTypeEnum() {
        return taskType != null ? OrderTask.TaskType.valueOf(taskType) : null;
    }

    public OrderTask.TaskStatus getStatusEnum() {
        return status != null ? OrderTask.TaskStatus.valueOf(status) : null;
    }

    public String getTaskTypeDisplayName() {
        return getTaskTypeEnum() != null ? getTaskTypeEnum().getDisplayName() : null;
    }

    public String getStatusDisplayName() {
        return getStatusEnum() != null ? getStatusEnum().getDisplayName() : null;
    }
}
