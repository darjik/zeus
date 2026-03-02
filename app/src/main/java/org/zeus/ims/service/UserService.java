package org.zeus.ims.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zeus.ims.dto.UserDTO;
import org.zeus.ims.entity.User;
import org.zeus.ims.entity.UserRole;
import org.zeus.ims.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getAllActiveUsers() {
        return userRepository.findByActiveTrue();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByActiveAndRole(true, role);
    }

    public List<User> searchUsers(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllActiveUsers();
        }
        return userRepository.findActiveUsersBySearch(searchTerm.trim());
    }

    public User createUser(UserDTO userDTO) {
        validateUserDTO(userDTO, true);

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setFullName(userDTO.getFullName());
        user.setEmail(userDTO.getEmail());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setRole(userDTO.getRole());
        user.setActive(userDTO.getActive());
        user.setCreatedBy(getCurrentUsername());

        return userRepository.save(user);
    }

    public User updateUser(Long id, UserDTO userDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        validateUserDTO(userDTO, false);

        if (!existingUser.getUsername().equals(userDTO.getUsername()) &&
            userRepository.existsByUsername(userDTO.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (!existingUser.getEmail().equals(userDTO.getEmail()) &&
            userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        existingUser.setUsername(userDTO.getUsername());
        existingUser.setFullName(userDTO.getFullName());
        existingUser.setEmail(userDTO.getEmail());
        existingUser.setPhoneNumber(userDTO.getPhoneNumber());
        existingUser.setRole(userDTO.getRole());
        existingUser.setActive(userDTO.getActive());
        existingUser.setUpdatedBy(getCurrentUsername());

        if (userDTO.getPassword() != null && !userDTO.getPassword().trim().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (user.getRole() == UserRole.OWNER) {
            long ownerCount = userRepository.countActiveUsersByRole(UserRole.OWNER);
            if (ownerCount <= 1) {
                throw new RuntimeException("Cannot delete the last owner user");
            }
        }

        user.setActive(false);
        user.setUpdatedBy(getCurrentUsername());
        userRepository.save(user);
    }

    public void updateLastLogin(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    public boolean validatePassword(String password, String confirmPassword) {
        return password != null && password.equals(confirmPassword) && password.length() >= 6;
    }

    public UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRole(user.getRole());
        dto.setActive(user.getActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLogin(user.getLastLogin());
        return dto;
    }

    public long getUserCountByRole(UserRole role) {
        return userRepository.countActiveUsersByRole(role);
    }

    private void validateUserDTO(UserDTO userDTO, boolean isNewUser) {
        if (userDTO.getUsername() == null || userDTO.getUsername().trim().isEmpty()) {
            throw new RuntimeException("Username is required");
        }

        if (userDTO.getFullName() == null || userDTO.getFullName().trim().isEmpty()) {
            throw new RuntimeException("Full name is required");
        }

        if (userDTO.getEmail() == null || userDTO.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }

        if (userDTO.getRole() == null) {
            throw new RuntimeException("Role is required");
        }

        if (isNewUser) {
            if (userDTO.getPassword() == null || userDTO.getPassword().length() < 6) {
                throw new RuntimeException("Password must be at least 6 characters long");
            }

            if (userRepository.existsByUsername(userDTO.getUsername())) {
                throw new RuntimeException("Username already exists");
            }

            if (userRepository.existsByEmail(userDTO.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
        }
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }
}
