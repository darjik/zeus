package org.zeus.ims.dto;

import org.springframework.format.annotation.DateTimeFormat;
import org.zeus.ims.validation.CreateEnquiry;
import org.zeus.ims.validation.UpdateEnquiry;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EnquiryDTO {

    private Long id;

    private String enquiryNumber;

    @NotNull(message = "Customer is required", groups = {CreateEnquiry.class, UpdateEnquiry.class})
    private Long customerId;

    @Size(max = 1000, message = "Description must not exceed 1000 characters", groups = {CreateEnquiry.class, UpdateEnquiry.class})
    private String description;

    @Size(max = 1000, message = "Requirements must not exceed 1000 characters", groups = {CreateEnquiry.class, UpdateEnquiry.class})
    private String requirements;

    @NotBlank(message = "Status is required", groups = {CreateEnquiry.class, UpdateEnquiry.class})
    private String status;

    @NotBlank(message = "Priority is required", groups = {CreateEnquiry.class, UpdateEnquiry.class})
    private String priority;

    @Size(max = 500, message = "Tags must not exceed 500 characters", groups = {CreateEnquiry.class, UpdateEnquiry.class})
    private String tags;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime deliveryExpectedDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime quoteValidUntil;

    private Boolean active = true;

    private String customerName;

    private List<EnquiryItemDTO> items = new ArrayList<>();

    private BigDecimal totalAmount;

    private Integer totalItems;

    public EnquiryDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEnquiryNumber() {
        return enquiryNumber;
    }

    public void setEnquiryNumber(String enquiryNumber) {
        this.enquiryNumber = enquiryNumber;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public LocalDateTime getDeliveryExpectedDate() {
        return deliveryExpectedDate;
    }

    public void setDeliveryExpectedDate(LocalDateTime deliveryExpectedDate) {
        this.deliveryExpectedDate = deliveryExpectedDate;
    }

    public LocalDateTime getQuoteValidUntil() {
        return quoteValidUntil;
    }

    public void setQuoteValidUntil(LocalDateTime quoteValidUntil) {
        this.quoteValidUntil = quoteValidUntil;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public List<EnquiryItemDTO> getItems() {
        return items;
    }

    public void setItems(List<EnquiryItemDTO> items) {
        this.items = items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }
}
