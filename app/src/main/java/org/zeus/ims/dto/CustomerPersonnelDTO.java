package org.zeus.ims.dto;

import org.zeus.ims.validation.CreateCustomer;
import org.zeus.ims.validation.UpdateCustomer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CustomerPersonnelDTO {

    private Long id;

    private Long customerId;

    @NotBlank(message = "Full name is required", groups = {CreateCustomer.class, UpdateCustomer.class})
    @Size(max = 100, message = "Full name must not exceed 100 characters", groups = {CreateCustomer.class, UpdateCustomer.class})
    private String fullName;

    @Email(message = "Email should be valid", groups = {CreateCustomer.class, UpdateCustomer.class})
    @Size(max = 100, message = "Email must not exceed 100 characters", groups = {CreateCustomer.class, UpdateCustomer.class})
    private String email;

    @Size(max = 20, message = "Contact number must not exceed 20 characters", groups = {CreateCustomer.class, UpdateCustomer.class})
    private String contactNumber;

    @Size(max = 20, message = "Secondary contact must not exceed 20 characters", groups = {CreateCustomer.class, UpdateCustomer.class})
    private String secondaryContact;

    @Size(max = 100, message = "Department must not exceed 100 characters", groups = {CreateCustomer.class, UpdateCustomer.class})
    private String department;

    @Size(max = 100, message = "Designation must not exceed 100 characters", groups = {CreateCustomer.class, UpdateCustomer.class})
    private String designation;

    private String notes;

    private Boolean active = true;

    private Boolean isPrimaryContact = false;

    public CustomerPersonnelDTO() {
    }

    public CustomerPersonnelDTO(Long customerId, String fullName, String email, String contactNumber) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.email = email;
        this.contactNumber = contactNumber;
        this.active = true;
        this.isPrimaryContact = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getSecondaryContact() {
        return secondaryContact;
    }

    public void setSecondaryContact(String secondaryContact) {
        this.secondaryContact = secondaryContact;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getIsPrimaryContact() {
        return isPrimaryContact;
    }

    public void setIsPrimaryContact(Boolean isPrimaryContact) {
        this.isPrimaryContact = isPrimaryContact;
    }
}
