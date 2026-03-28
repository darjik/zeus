package org.zeus.ims.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPartDTO {

    private Long id;

    @NotBlank(message = "Part name is required")
    @Size(min = 2, max = 100, message = "Part name must be between 2 and 100 characters")
    private String partName;

    private String partNumber;

    private String description;

    private String material;

    private String dimensions;

    private String unitOfMeasure;

    private Integer quantityRequired;

    private String specifications;

    @Builder.Default
    private Boolean active = true;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;

    private String updatedBy;

    private Long productId;

    private Long vendorId;

    private String vendorName;

    // Helper methods
    public String getDisplayName() {
        StringBuilder displayName = new StringBuilder(partName);
        if (partNumber != null && !partNumber.trim().isEmpty()) {
            displayName.append(" (").append(partNumber).append(")");
        }
        return displayName.toString();
    }

    public String getVendorDisplayName() {
        return vendorName != null ? vendorName : "No Vendor Assigned";
    }
}
