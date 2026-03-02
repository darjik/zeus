package org.zeus.ims.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zeus.ims.entity.UserRole;
import org.zeus.ims.service.CustomUserDetailsService;
import org.zeus.ims.service.DashboardService;
import org.zeus.ims.service.UserService;

import java.util.Map;

@Controller
public class AuthController {

    private final UserService userService;
    private final DashboardService dashboardService;

    @Autowired
    public AuthController(UserService userService, DashboardService dashboardService) {
        this.userService = userService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                          @RequestParam(value = "logout", required = false) String logout,
                          Model model) {

        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password!");
        }

        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully!");
        }

        return "auth/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetailsService.CustomUserPrincipal) {
            CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();

            model.addAttribute("user", userPrincipal.getUser());
            model.addAttribute("fullName", userPrincipal.getFullName());
            model.addAttribute("role", userPrincipal.getUser().getRole());

            // Add role-based dashboard statistics
            addDashboardStatistics(model, userPrincipal.getUser().getRole(), authentication.getName());
        }

        return "dashboard/index";
    }

    private void addDashboardStatistics(Model model, UserRole userRole, String username) {
        try {
            Map<String, Object> stats;

            switch (userRole) {
                case OWNER:
                    stats = dashboardService.getOwnerDashboardStats();
                    model.addAllAttributes(stats);

                    // Add recent activities for owner
                    model.addAttribute("recentEnquiries", dashboardService.getRecentEnquiries(5));
                    model.addAttribute("recentOrders", dashboardService.getRecentOrders(5));
                    break;

                case SALES:
                    stats = dashboardService.getSalesDashboardStats(username);
                    model.addAllAttributes(stats);

                    // Add pending enquiries for sales user
                    model.addAttribute("myPendingEnquiries", dashboardService.getPendingEnquiriesForUser(username, 5));
                    model.addAttribute("recentEnquiries", dashboardService.getRecentEnquiries(5));
                    model.addAttribute("recentOrders", dashboardService.getRecentOrders(5));
                    break;

                case PRODUCTION_MANAGER:
                    stats = dashboardService.getProductionDashboardStats();
                    model.addAllAttributes(stats);

                    // Add recent orders for production
                    model.addAttribute("recentOrders", dashboardService.getRecentOrders(5));
                    model.addAttribute("recentEnquiries", dashboardService.getRecentEnquiries(5));
                    break;

                case WORKSHOP_PERSONNEL:
                    stats = dashboardService.getWorkshopDashboardStats(username);
                    model.addAllAttributes(stats);

                    // Add recent activities for workshop
                    model.addAttribute("recentOrders", dashboardService.getRecentOrders(5));
                    break;

                case ACCOUNTANT:
                    stats = dashboardService.getAccountantDashboardStats();
                    model.addAllAttributes(stats);

                    // Add recent orders for financial tracking
                    model.addAttribute("recentOrders", dashboardService.getRecentOrders(5));
                    model.addAttribute("recentEnquiries", dashboardService.getRecentEnquiries(5));
                    break;

                default:
                    // Basic stats for any other roles
                    model.addAttribute("totalUsers", userService.getAllActiveUsers().size());
                    // Add empty lists to prevent template errors
                    model.addAttribute("recentEnquiries", java.util.Collections.emptyList());
                    model.addAttribute("recentOrders", java.util.Collections.emptyList());
                    break;
            }
        } catch (Exception ex) {
            // Log error and provide fallback
            model.addAttribute("dashboardError", "Unable to load dashboard statistics");
            // Add empty lists to prevent template errors
            model.addAttribute("recentEnquiries", java.util.Collections.emptyList());
            model.addAttribute("recentOrders", java.util.Collections.emptyList());
        }
    }
}
