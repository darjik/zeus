package org.zeus.ims.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "vendor_personnel")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorPersonnel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "designation")
    private String designation;

    @Column(name = "department")
    private String department;

    @Email(message = "Please provide a valid email address")
    @Column(name = "email")
    private String email;

    @Column(name = "contact_number")
    private String contactNumber;

    @Column(name = "secondary_contact")
    private String secondaryContact;

    @Builder.Default
    @Column(name = "is_primary_contact", nullable = false)
    private Boolean isPrimaryContact = false;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Audit fields
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @PrePersist
    @PreUpdate
    public void updateAuditFields() {
        // Set current user as updatedBy (will be enhanced with security context later)
        this.updatedBy = "system";
        if (this.createdBy == null) {
            this.createdBy = "system";
        }
    }

    // Helper method to get vendor company name
    public String getVendorCompanyName() {
        return vendor != null ? vendor.getCompanyName() : null;
    }
}
