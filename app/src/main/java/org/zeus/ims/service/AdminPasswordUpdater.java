package org.zeus.ims.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.zeus.ims.entity.User;
import org.zeus.ims.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import java.util.Optional;

@Component
public class AdminPasswordUpdater {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminPasswordUpdater(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void updateAdminPassword() {
        Optional<User> adminUser = userRepository.findByUsername("admin");
        if (adminUser.isPresent()) {
            User user = adminUser.get();
            String encodedPassword = passwordEncoder.encode("admin");
            user.setPassword(encodedPassword);
            userRepository.save(user);
            System.out.println("Admin password updated successfully");
        } else {
            System.out.println("Admin user not found");
        }
    }


}
