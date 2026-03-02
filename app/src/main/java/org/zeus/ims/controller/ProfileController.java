package org.zeus.ims.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.zeus.ims.dto.UserDTO;
import org.zeus.ims.entity.User;
import org.zeus.ims.service.UserService;
import org.zeus.ims.validation.UpdateUser;

import java.util.Optional;

@Controller
public class ProfileController {

    private final UserService userService;

    @Autowired
    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String viewProfile(Authentication authentication, Model model) {
        String username = authentication.getName();
        Optional<User> userOpt = userService.getUserByUsername(username);

        if (!userOpt.isPresent()) {
            model.addAttribute("errorMessage", "User profile not found");
            return "redirect:/dashboard";
        }

        User user = userOpt.get();
        UserDTO userDTO = userService.convertToDTO(user);

        model.addAttribute("user", userDTO);
        model.addAttribute("pageTitle", "My Profile");
        return "auth/profile";
    }

    @GetMapping("/profile/edit")
    public String editProfile(Authentication authentication, Model model) {
        String username = authentication.getName();
        Optional<User> userOpt = userService.getUserByUsername(username);

        if (!userOpt.isPresent()) {
            model.addAttribute("errorMessage", "User profile not found");
            return "redirect:/dashboard";
        }

        User user = userOpt.get();
        UserDTO userDTO = userService.convertToDTO(user);

        model.addAttribute("userDTO", userDTO);
        model.addAttribute("pageTitle", "Edit Profile");
        model.addAttribute("isEdit", true);
        return "auth/profile-edit";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(@Validated(UpdateUser.class) @ModelAttribute("userDTO") UserDTO userDTO,
                               BindingResult result,
                               Authentication authentication,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        String currentUsername = authentication.getName();
        Optional<User> currentUserOpt = userService.getUserByUsername(currentUsername);

        if (!currentUserOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found");
            return "redirect:/dashboard";
        }

        User currentUser = currentUserOpt.get();

        // Users can only edit their own profile
        if (!currentUser.getId().equals(userDTO.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You can only edit your own profile");
            return "redirect:/profile";
        }

        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Profile");
            model.addAttribute("isEdit", true);
            return "auth/profile-edit";
        }

        try {
            userService.updateUser(userDTO.getId(), userDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
            return "redirect:/profile";
        } catch (RuntimeException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("pageTitle", "Edit Profile");
            model.addAttribute("isEdit", true);
            return "auth/profile-edit";
        }
    }
}
