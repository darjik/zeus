package org.zeus.ims.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "enquiries")
public class Enquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "enquiry_number", nullable = false, unique = true, length = 20)
    private String enquiryNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "enquiry", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EnquiryItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "enquiry", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EnquiryConversation> conversations = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnquiryStatus status = EnquiryStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnquiryPriority priority = EnquiryPriority.MEDIUM;

    @Column(name = "tags", length = 500)
    private String tags;

    @Column(name = "delivery_expected_date")
    private LocalDateTime deliveryExpectedDate;

    @Column(name = "quote_valid_until")
    private LocalDateTime quoteValidUntil;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    public Enquiry() {
    }

    public Enquiry(String enquiryNumber, Customer customer) {
        this.enquiryNumber = enquiryNumber;
        this.customer = customer;
        this.status = EnquiryStatus.PENDING;
        this.priority = EnquiryPriority.MEDIUM;
        this.active = true;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum EnquiryStatus {
        PENDING("Pending"),
        QUOTED("Quoted"),
        NEGOTIATING("Negotiating"),
        APPROVED("Approved"),
        REJECTED("Rejected"),
        CONVERTED("Converted to Order"),
        CANCELLED("Cancelled");

        private final String displayName;

        EnquiryStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum EnquiryPriority {
        LOW("Low"),
        MEDIUM("Medium"),
        HIGH("High"),
        URGENT("Urgent");

        private final String displayName;

        EnquiryPriority(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
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

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<EnquiryItem> getItems() {
        return items;
    }

    public void setItems(List<EnquiryItem> items) {
        this.items = items;
    }

    public List<EnquiryConversation> getConversations() {
        return conversations;
    }

    public void setConversations(List<EnquiryConversation> conversations) {
        this.conversations = conversations;
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

    public EnquiryStatus getStatus() {
        return status;
    }

    public void setStatus(EnquiryStatus status) {
        this.status = status;
    }

    public EnquiryPriority getPriority() {
        return priority;
    }

    public void setPriority(EnquiryPriority priority) {
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

    public BigDecimal getTotalAmount() {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return items.stream()
                .filter(item -> item.getTotalAmount() != null)
                .map(EnquiryItem::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalItems() {
        return items != null ? items.size() : 0;
    }
}
