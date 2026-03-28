package org.zeus.ims.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorDTO {

    private Long id;

    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 100, message = "Company name must be between 2 and 100 characters")
    private String companyName;

    private String description;

    private String websiteUrl;

    // Address fields
    private String addressLine1;

    private String addressLine2;

    private String city;

    private String state;

    private String postalCode;

    private String country;

    // Contact fields
    private String contactNumber;

    private String secondaryContact;

    @Email(message = "Please provide a valid email address")
    private String email;

    @Builder.Default
    private Boolean active = true;

    // Computed properties for display
    private String fullAddress;

    private Long personnelCount;

    private VendorPersonnelDTO primaryContact;

    // Audit fields
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;

    private String updatedBy;
}
