package org.zeus.ims.dto;

import org.springframework.format.annotation.DateTimeFormat;
import org.zeus.ims.entity.Order;
import org.zeus.ims.validation.CreateOrder;
import org.zeus.ims.validation.UpdateOrder;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDTO {

    private Long id;
    private String orderNumber;

    @NotNull(groups = CreateOrder.class, message = "Customer is required")
    private Long customerId;

    private String customerName;
    private Long customerPersonnelId;
    private String customerPersonnelName;
    private Long enquiryId;
    private String enquiryNumber;
    private String description;
    private String requirements;
    private Order.OrderStatus status;
    private Order.OrderPriority priority;
    private LocalDateTime orderDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate deliveryExpectedDate;

    private LocalDateTime deliveryActualDate;
    private BigDecimal totalAmount;
    private String purchaseOrderNumber;
    private String transportDetails;
    private LocalDateTime dispatchDate;
    private Boolean active;
    private List<OrderItemDTO> items = new ArrayList<>();
    private List<OrderDocumentDTO> documents = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    public OrderDTO() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Long getCustomerPersonnelId() {
        return customerPersonnelId;
    }

    public void setCustomerPersonnelId(Long customerPersonnelId) {
        this.customerPersonnelId = customerPersonnelId;
    }

    public String getCustomerPersonnelName() {
        return customerPersonnelName;
    }

    public void setCustomerPersonnelName(String customerPersonnelName) {
        this.customerPersonnelName = customerPersonnelName;
    }

    public Long getEnquiryId() {
        return enquiryId;
    }

    public void setEnquiryId(Long enquiryId) {
        this.enquiryId = enquiryId;
    }

    public String getEnquiryNumber() {
        return enquiryNumber;
    }

    public void setEnquiryNumber(String enquiryNumber) {
        this.enquiryNumber = enquiryNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public Order.OrderStatus getStatus() {
        return status;
    }

    public void setStatus(Order.OrderStatus status) {
        this.status = status;
    }

    public Order.OrderPriority getPriority() {
        return priority;
    }

    public void setPriority(Order.OrderPriority priority) {
        this.priority = priority;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDate getDeliveryExpectedDate() {
        return deliveryExpectedDate;
    }

    public void setDeliveryExpectedDate(LocalDate deliveryExpectedDate) {
        this.deliveryExpectedDate = deliveryExpectedDate;
    }

    public LocalDateTime getDeliveryActualDate() {
        return deliveryActualDate;
    }

    public void setDeliveryActualDate(LocalDateTime deliveryActualDate) {
        this.deliveryActualDate = deliveryActualDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPurchaseOrderNumber() {
        return purchaseOrderNumber;
    }

    public void setPurchaseOrderNumber(String purchaseOrderNumber) {
        this.purchaseOrderNumber = purchaseOrderNumber;
    }

    public String getTransportDetails() {
        return transportDetails;
    }

    public void setTransportDetails(String transportDetails) {
        this.transportDetails = transportDetails;
    }

    public LocalDateTime getDispatchDate() {
        return dispatchDate;
    }

    public void setDispatchDate(LocalDateTime dispatchDate) {
        this.dispatchDate = dispatchDate;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<OrderItemDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDTO> items) {
        this.items = items;
    }

    public List<OrderDocumentDTO> getDocuments() {
        return documents;
    }

    public void setDocuments(List<OrderDocumentDTO> documents) {
        this.documents = documents;
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
}
