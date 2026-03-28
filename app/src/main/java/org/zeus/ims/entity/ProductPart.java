package org.zeus.ims.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_parts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Part name is required")
    @Size(min = 2, max = 100, message = "Part name must be between 2 and 100 characters")
    @Column(name = "part_name", nullable = false)
    private String partName;

    @Column(name = "part_number")
    private String partNumber;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "material")
    private String material;

    @Column(name = "dimensions")
    private String dimensions;

    @Column(name = "unit_of_measure")
    private String unitOfMeasure;

    @Column(name = "quantity_required")
    private Integer quantityRequired;

    @Column(name = "specifications", columnDefinition = "TEXT")
    private String specifications;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

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
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    // Helper methods
    public String getDisplayName() {
        StringBuilder displayName = new StringBuilder(partName);
        if (partNumber != null && !partNumber.trim().isEmpty()) {
            displayName.append(" (").append(partNumber).append(")");
        }
        return displayName.toString();
    }

    public String getVendorName() {
        return vendor != null ? vendor.getCompanyName() : "No Vendor Assigned";
    }
}
