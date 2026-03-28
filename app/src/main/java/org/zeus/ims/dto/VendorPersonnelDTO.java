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
public class VendorPersonnelDTO {

    private Long id;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    private String designation;

    private String department;

    @Email(message = "Please provide a valid email address")
    private String email;

    private String contactNumber;

    private String secondaryContact;

    @Builder.Default
    private Boolean isPrimaryContact = false;

    @Builder.Default
    private Boolean active = true;

    private String notes;

    private Long vendorId;

    private String vendorCompanyName;

    // Audit fields
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;

    private String updatedBy;
}
