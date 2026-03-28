package org.zeus.ims.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "model_number")
    private String modelNumber;

    @Column(name = "brand")
    private String brand;

    @Column(name = "category")
    private String category;

    @Column(name = "unit_of_measure")
    private String unitOfMeasure;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "dimensions")
    private String dimensions;

    @Column(name = "material")
    private String material;

    @Column(name = "specifications", columnDefinition = "TEXT")
    private String specifications;

    @Column(name = "cad_drawings_path")
    private String cadDrawingsPath;

    @Column(name = "product_images_path")
    private String productImagesPath;

    @Column(name = "catalog_path")
    private String catalogPath;

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
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<ProductPart> parts = new ArrayList<>();

    // Helper methods
    public int getActivePartsCount() {
        return parts != null ? (int) parts.stream().filter(part -> part.getActive()).count() : 0;
    }

    public String getDisplayName() {
        StringBuilder displayName = new StringBuilder(name);
        if (modelNumber != null && !modelNumber.trim().isEmpty()) {
            displayName.append(" (").append(modelNumber).append(")");
        }
        return displayName.toString();
    }

    public boolean hasFiles() {
        return (cadDrawingsPath != null && !cadDrawingsPath.trim().isEmpty()) ||
               (productImagesPath != null && !productImagesPath.trim().isEmpty()) ||
               (catalogPath != null && !catalogPath.trim().isEmpty());
    }
}
