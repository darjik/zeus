package org.zeus.ims.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {

    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    private String name;

    private String description;

    private String modelNumber;

    private String brand;

    private String category;

    private String unitOfMeasure;

    private Double weight;

    private String dimensions;

    private String material;

    private String specifications;

    private String cadDrawingsPath;

    private String productImagesPath;

    private String catalogPath;

    @Builder.Default
    private Boolean active = true;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;

    private String updatedBy;

    @Builder.Default
    private List<ProductPartDTO> parts = new ArrayList<>();

    // Computed properties
    public String getDisplayName() {
        StringBuilder displayName = new StringBuilder(name);
        if (modelNumber != null && !modelNumber.trim().isEmpty()) {
            displayName.append(" (").append(modelNumber).append(")");
        }
        return displayName.toString();
    }

    public int getActivePartsCount() {
        return parts != null ? (int) parts.stream().filter(part -> part.getActive()).count() : 0;
    }

    public boolean hasFiles() {
        return (cadDrawingsPath != null && !cadDrawingsPath.trim().isEmpty()) ||
               (productImagesPath != null && !productImagesPath.trim().isEmpty()) ||
               (catalogPath != null && !catalogPath.trim().isEmpty());
    }
}
