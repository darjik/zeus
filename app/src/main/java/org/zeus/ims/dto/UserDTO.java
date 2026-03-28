package org.zeus.ims.dto;

import org.zeus.ims.entity.UserRole;
import org.zeus.ims.validation.CreateUser;
import org.zeus.ims.validation.UpdateUser;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class UserDTO {

    private Long id;

    @NotBlank(message = "Username is required", groups = {CreateUser.class, UpdateUser.class})
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters", groups = {CreateUser.class, UpdateUser.class})
    private String username;

    @NotBlank(message = "Password is required", groups = CreateUser.class)
    @Size(min = 6, message = "Password must be at least 6 characters", groups = CreateUser.class)
    private String password;

    private String confirmPassword;

    @NotBlank(message = "Full name is required", groups = {CreateUser.class, UpdateUser.class})
    @Size(max = 100, message = "Full name must not exceed 100 characters", groups = {CreateUser.class, UpdateUser.class})
    private String fullName;

    @NotBlank(message = "Email is required", groups = {CreateUser.class, UpdateUser.class})
    @Email(message = "Email should be valid", groups = {CreateUser.class, UpdateUser.class})
    @Size(max = 100, message = "Email must not exceed 100 characters", groups = {CreateUser.class, UpdateUser.class})
    private String email;

    @Size(max = 15, message = "Phone number must not exceed 15 characters", groups = {CreateUser.class, UpdateUser.class})
    private String phoneNumber;

    @NotNull(message = "Role is required", groups = {CreateUser.class, UpdateUser.class})
    private UserRole role;

    private Boolean active = true;

    private LocalDateTime createdAt;

    private LocalDateTime lastLogin;

    public UserDTO() {
    }

    public UserDTO(String username, String fullName, String email, UserRole role) {
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
}
