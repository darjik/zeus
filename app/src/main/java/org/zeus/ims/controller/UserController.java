package org.zeus.ims.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.zeus.ims.dto.UserDTO;
import org.zeus.ims.entity.User;
import org.zeus.ims.entity.UserRole;
import org.zeus.ims.service.UserService;
import org.zeus.ims.validation.CreateUser;
import org.zeus.ims.validation.UpdateUser;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/users")
@PreAuthorize("hasAuthority('OWNER')")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String listUsers(@RequestParam(value = "search", required = false) String search, Model model) {
        List<User> users;
        if (search != null && !search.trim().isEmpty()) {
            users = userService.searchUsers(search);
            model.addAttribute("search", search);
        } else {
            users = userService.getAllUsers();
        }

        model.addAttribute("users", users);
        model.addAttribute("userRoles", UserRole.values());
        return "users/list";
    }

    @GetMapping("/new")
    public String showCreateUserForm(Model model) {
        model.addAttribute("userDTO", new UserDTO());
        model.addAttribute("userRoles", UserRole.values());
        model.addAttribute("pageTitle", "Create New User");
        return "users/form";
    }

    @PostMapping("/new")
    public String createUser(@Validated(CreateUser.class) @ModelAttribute("userDTO") UserDTO userDTO,
                           BindingResult result,
                           Model model,
                           RedirectAttributes redirectAttributes) {

        if (!userDTO.getPassword().equals(userDTO.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.confirmPassword", "Passwords do not match");
        }

        if (result.hasErrors()) {
            model.addAttribute("userRoles", UserRole.values());
            model.addAttribute("pageTitle", "Create New User");
            return "users/form";
        }

        try {
            User createdUser = userService.createUser(userDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                "User '" + createdUser.getUsername() + "' created successfully!");
            return "redirect:/users";
        } catch (RuntimeException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("userRoles", UserRole.values());
            model.addAttribute("pageTitle", "Create New User");
            return "users/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userService.getUserById(id);
        if (!userOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found!");
            return "redirect:/users";
        }

        User user = userOpt.get();
        UserDTO userDTO = userService.convertToDTO(user);

        model.addAttribute("userDTO", userDTO);
        model.addAttribute("userRoles", UserRole.values());
        model.addAttribute("pageTitle", "Edit User");
        model.addAttribute("isEdit", true);
        return "users/form";
    }

    @PostMapping("/edit/{id}")
    public String updateUser(@PathVariable Long id,
                           @Validated(UpdateUser.class) @ModelAttribute("userDTO") UserDTO userDTO,
                           BindingResult result,
                           Model model,
                           RedirectAttributes redirectAttributes) {

        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            if (!userDTO.getPassword().equals(userDTO.getConfirmPassword())) {
                result.rejectValue("confirmPassword", "error.confirmPassword", "Passwords do not match");
            }
        }

        if (result.hasErrors()) {
            model.addAttribute("userRoles", UserRole.values());
            model.addAttribute("pageTitle", "Edit User");
            model.addAttribute("isEdit", true);
            return "users/form";
        }

        try {
            User updatedUser = userService.updateUser(id, userDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                "User '" + updatedUser.getUsername() + "' updated successfully!");
            return "redirect:/users";
        } catch (RuntimeException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("userRoles", UserRole.values());
            model.addAttribute("pageTitle", "Edit User");
            model.addAttribute("isEdit", true);
            return "users/form";
        }
    }

    @GetMapping("/view/{id}")
    public String viewUser(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userService.getUserById(id);
        if (!userOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found!");
            return "redirect:/users";
        }

        model.addAttribute("user", userOpt.get());
        return "users/view";
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<User> userOpt = userService.getUserById(id);
            if (!userOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("errorMessage", "User not found!");
                return "redirect:/users";
            }

            String username = userOpt.get().getUsername();
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage",
                "User '" + username + "' deactivated successfully!");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/users";
    }

    @ResponseBody
    @GetMapping("/check-username")
    public boolean checkUsernameAvailability(@RequestParam String username,
                                           @RequestParam(required = false) Long userId) {
        if (userId != null) {
            Optional<User> userOpt = userService.getUserById(userId);
            if (userOpt.isPresent() && userOpt.get().getUsername().equals(username)) {
                return true;
            }
        }
        return userService.isUsernameAvailable(username);
    }

    @ResponseBody
    @GetMapping("/check-email")
    public boolean checkEmailAvailability(@RequestParam String email,
                                        @RequestParam(required = false) Long userId) {
        if (userId != null) {
            Optional<User> userOpt = userService.getUserById(userId);
            if (userOpt.isPresent() && userOpt.get().getEmail().equals(email)) {
                return true;
            }
        }
        return userService.isEmailAvailable(email);
    }
}
