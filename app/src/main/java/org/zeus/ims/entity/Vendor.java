package org.zeus.ims.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vendors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 100, message = "Company name must be between 2 and 100 characters")
    @Column(name = "company_name", nullable = false, unique = true)
    private String companyName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "website_url")
    private String websiteUrl;

    // Address fields
    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "country")
    private String country;

    // Contact fields
    @Column(name = "contact_number")
    private String contactNumber;

    @Column(name = "secondary_contact")
    private String secondaryContact;

    @Email(message = "Please provide a valid email address")
    @Column(name = "email")
    private String email;

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
    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<VendorPersonnel> personnel = new ArrayList<>();

    // Computed properties
    public String getFullAddress() {
        StringBuilder address = new StringBuilder();

        if (addressLine1 != null && !addressLine1.trim().isEmpty()) {
            address.append(addressLine1);
        }

        if (addressLine2 != null && !addressLine2.trim().isEmpty()) {
            if (address.length() > 0) {
                address.append(", ");
            }
            address.append(addressLine2);
        }

        if (city != null && !city.trim().isEmpty()) {
            if (address.length() > 0) {
                address.append(", ");
            }
            address.append(city);
        }

        if (state != null && !state.trim().isEmpty()) {
            if (address.length() > 0) {
                address.append(", ");
            }
            address.append(state);
        }

        if (postalCode != null && !postalCode.trim().isEmpty()) {
            if (address.length() > 0) {
                address.append(" ");
            }
            address.append(postalCode);
        }

        if (country != null && !country.trim().isEmpty()) {
            if (address.length() > 0) {
                address.append(", ");
            }
            address.append(country);
        }

        return address.toString();
    }

    @PrePersist
    @PreUpdate
    public void updateAuditFields() {
        // Set current user as updatedBy (will be enhanced with security context later)
        this.updatedBy = "system";
        if (this.createdBy == null) {
            this.createdBy = "system";
        }
    }

    // Helper methods for personnel management
    public void addPersonnel(VendorPersonnel person) {
        personnel.add(person);
        person.setVendor(this);
    }

    public void removePersonnel(VendorPersonnel person) {
        personnel.remove(person);
        person.setVendor(null);
    }

    public long getActivePersonnelCount() {
        return personnel.stream()
                .filter(VendorPersonnel::getActive)
                .count();
    }

    public VendorPersonnel getPrimaryContact() {
        return personnel.stream()
                .filter(VendorPersonnel::getActive)
                .filter(VendorPersonnel::getIsPrimaryContact)
                .findFirst()
                .orElse(null);
    }
}
