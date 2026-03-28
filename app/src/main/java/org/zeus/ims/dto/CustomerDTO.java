package org.zeus.ims.dto;

import org.zeus.ims.validation.CreateCustomer;
import org.zeus.ims.validation.UpdateCustomer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CustomerDTO {

    private Long id;

    @NotBlank(message = "Company name is required", groups = {CreateCustomer.class, UpdateCustomer.class})
    @Size(max = 100, message = "Company name must not exceed 100 characters", groups = {CreateCustomer.class, UpdateCustomer.class})
    private String companyName;

    @Size(max = 200, message = "Address line 1 must not exceed 200 characters", groups = {CreateCustomer.class, UpdateCustomer.class})
    private String addressLine1;

    @Size(max = 200, message = "Address line 2 must not exceed 200 characters", groups = {CreateCustomer.class, UpdateCustomer.class})
    private String addressLine2;

    @Size(max = 100, message = "City must not exceed 100 characters", groups = {CreateCustomer.class, UpdateCustomer.class})
    private String city;

    @Size(max = 100, message = "State must not exceed 100 characters", groups = {CreateCustomer.class, UpdateCustomer.class})
    private String state;

    @Size(max = 20, message = "Postal code must not exceed 20 characters", groups = {CreateCustomer.class, UpdateCustomer.class})
    private String postalCode;

    @Size(max = 100, message = "Country must not exceed 100 characters", groups = {CreateCustomer.class, UpdateCustomer.class})
    private String country;

    @Size(max = 20, message = "Contact number must not exceed 20 characters", groups = {CreateCustomer.class, UpdateCustomer.class})
    private String contactNumber;

    @Size(max = 20, message = "Secondary contact must not exceed 20 characters", groups = {CreateCustomer.class, UpdateCustomer.class})
    private String secondaryContact;

    @Email(message = "Email should be valid", groups = {CreateCustomer.class, UpdateCustomer.class})
    @Size(max = 100, message = "Email must not exceed 100 characters", groups = {CreateCustomer.class, UpdateCustomer.class})
    private String email;

    @Size(max = 200, message = "Website URL must not exceed 200 characters", groups = {CreateCustomer.class, UpdateCustomer.class})
    private String websiteUrl;

    private String description;

    private Boolean active = true;

    public CustomerDTO() {
    }

    public CustomerDTO(String companyName, String contactNumber, String email) {
        this.companyName = companyName;
        this.contactNumber = contactNumber;
        this.email = email;
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
