package org.zeus.ims.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zeus.ims.entity.*;
import org.zeus.ims.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final CustomerRepository customerRepository;
    private final VendorRepository vendorRepository;
    private final ProductRepository productRepository;
    private final EnquiryRepository enquiryRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Autowired
    public DashboardService(CustomerRepository customerRepository,
                           VendorRepository vendorRepository,
                           ProductRepository productRepository,
                           EnquiryRepository enquiryRepository,
                           OrderRepository orderRepository,
                           UserRepository userRepository) {
        this.customerRepository = customerRepository;
        this.vendorRepository = vendorRepository;
        this.productRepository = productRepository;
        this.enquiryRepository = enquiryRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> getOwnerDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // Business Overview Stats
        stats.put("totalCustomers", customerRepository.countByActiveTrue());
        stats.put("totalVendors", vendorRepository.countByActiveTrue());
        stats.put("totalProducts", productRepository.countByActiveTrue());
        stats.put("totalEnquiries", enquiryRepository.count());
        stats.put("totalOrders", orderRepository.count());

        // Monthly stats
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        stats.put("newCustomersThisMonth", customerRepository.countByActiveTrueAndCreatedAtAfter(startOfMonth));
        stats.put("newEnquiriesThisMonth", enquiryRepository.countByCreatedAtAfter(startOfMonth));
        stats.put("newOrdersThisMonth", orderRepository.countByCreatedAtAfter(startOfMonth));

        // Enquiry status breakdown
        stats.put("pendingEnquiries", enquiryRepository.countByStatus(Enquiry.EnquiryStatus.PENDING));
        stats.put("quotedEnquiries", enquiryRepository.countByStatus(Enquiry.EnquiryStatus.QUOTED));
        stats.put("convertedEnquiries", enquiryRepository.countByStatus(Enquiry.EnquiryStatus.CONVERTED));

        // Order status breakdown
        stats.put("pendingOrders", orderRepository.countByStatus(Order.OrderStatus.PENDING));
        stats.put("inProductionOrders", orderRepository.countByStatus(Order.OrderStatus.IN_DEVELOPMENT));
        stats.put("dispatchedOrders", orderRepository.countByStatus(Order.OrderStatus.DISPATCHED));
        stats.put("deliveredOrders", orderRepository.countByStatus(Order.OrderStatus.DELIVERED));

        // Revenue calculation (if totalAmount exists)
        BigDecimal totalRevenue = orderRepository.sumTotalAmountByStatus(Order.OrderStatus.DELIVERED);
        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

        BigDecimal monthlyRevenue = orderRepository.sumTotalAmountByStatusAndCreatedAtAfter(
                Order.OrderStatus.DELIVERED, startOfMonth);
        stats.put("monthlyRevenue", monthlyRevenue != null ? monthlyRevenue : BigDecimal.ZERO);

        // Team stats
        stats.put("totalUsers", userRepository.countByActiveTrue());
        stats.put("salesUsers", userRepository.countByActiveTrueAndRole(UserRole.SALES));
        stats.put("productionUsers", userRepository.countByActiveTrueAndRole(UserRole.PRODUCTION_MANAGER));
        stats.put("workshopUsers", userRepository.countByActiveTrueAndRole(UserRole.WORKSHOP_PERSONNEL));
        stats.put("accountantUsers", userRepository.countByActiveTrueAndRole(UserRole.ACCOUNTANT));

        return stats;
    }

    public Map<String, Object> getSalesDashboardStats(String username) {
        Map<String, Object> stats = new HashMap<>();

        // My enquiries stats
        stats.put("myPendingEnquiries", enquiryRepository.countByCreatedByAndStatus(username, Enquiry.EnquiryStatus.PENDING));
        stats.put("myQuotedEnquiries", enquiryRepository.countByCreatedByAndStatus(username, Enquiry.EnquiryStatus.QUOTED));
        stats.put("myConvertedEnquiries", enquiryRepository.countByCreatedByAndStatus(username, Enquiry.EnquiryStatus.CONVERTED));

        // Recent activity
        LocalDateTime lastWeek = LocalDateTime.now().minusDays(7);
        stats.put("recentEnquiries", enquiryRepository.countByCreatedByAndCreatedAtAfter(username, lastWeek));
        stats.put("recentOrders", orderRepository.countByCreatedByAndCreatedAtAfter(username, lastWeek));

        // Follow-up required (enquiries older than 3 days without response)
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        stats.put("followUpRequired", enquiryRepository.countByCreatedByAndStatusAndUpdatedAtBefore(
                username, Enquiry.EnquiryStatus.PENDING, threeDaysAgo));

        // Conversion rate
        long totalEnquiries = enquiryRepository.countByCreatedBy(username);
        long convertedEnquiries = enquiryRepository.countByCreatedByAndStatus(username, Enquiry.EnquiryStatus.CONVERTED);
        double conversionRate = totalEnquiries > 0 ? (double) convertedEnquiries / totalEnquiries * 100 : 0;
        stats.put("conversionRate", Math.round(conversionRate * 100.0) / 100.0);

        return stats;
    }

    public Map<String, Object> getProductionDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // Production pipeline
        stats.put("ordersInProduction", orderRepository.countByStatus(Order.OrderStatus.IN_DEVELOPMENT));
        stats.put("ordersInTesting", orderRepository.countByStatus(Order.OrderStatus.TESTING));
        stats.put("readyToDispatch", orderRepository.countByStatus(Order.OrderStatus.READY_FOR_DISPATCH));

        // Urgent orders (delivery expected within 7 days)
        LocalDate nextWeek = LocalDate.now().plusDays(7);
        stats.put("urgentDeliveries", orderRepository.countByDeliveryExpectedDateBeforeAndStatusNot(
                nextWeek, Order.OrderStatus.DELIVERED));

        // Overdue orders
        LocalDate today = LocalDate.now();
        stats.put("overdueOrders", orderRepository.countByDeliveryExpectedDateBeforeAndStatusNot(
                today, Order.OrderStatus.DELIVERED));

        return stats;
    }

    public Map<String, Object> getWorkshopDashboardStats(String username) {
        Map<String, Object> stats = new HashMap<>();

        // Work assigned to me (if we track individual assignments)
        stats.put("myTasks", orderRepository.countByStatus(Order.OrderStatus.IN_DEVELOPMENT));
        stats.put("completedThisWeek", 0); // Placeholder for now

        // Workshop overview
        stats.put("totalWorkInProgress", orderRepository.countByStatus(Order.OrderStatus.IN_DEVELOPMENT));
        stats.put("readyForTesting", orderRepository.countByStatus(Order.OrderStatus.TESTING));

        return stats;
    }

    public Map<String, Object> getAccountantDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // Financial overview
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        BigDecimal monthlyRevenue = orderRepository.sumTotalAmountByStatusAndCreatedAtAfter(
                Order.OrderStatus.DELIVERED, startOfMonth);
        stats.put("monthlyRevenue", monthlyRevenue != null ? monthlyRevenue : BigDecimal.ZERO);

        BigDecimal totalRevenue = orderRepository.sumTotalAmountByStatus(Order.OrderStatus.DELIVERED);
        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

        // Outstanding orders (not yet delivered)
        BigDecimal outstandingAmount = orderRepository.sumTotalAmountByStatusNot(Order.OrderStatus.DELIVERED);
        stats.put("outstandingAmount", outstandingAmount != null ? outstandingAmount : BigDecimal.ZERO);

        return stats;
    }

    public List<Enquiry> getRecentEnquiries(int limit) {
        try {
            return enquiryRepository.findTop5ByOrderByCreatedAtDesc();
        } catch (Exception ex) {
            // Return empty list if there's an error
            return java.util.Collections.emptyList();
        }
    }

    public List<Order> getRecentOrders(int limit) {
        try {
            return orderRepository.findTop5ByOrderByCreatedAtDesc();
        } catch (Exception ex) {
            // Return empty list if there's an error
            return java.util.Collections.emptyList();
        }
    }

    public List<Enquiry> getPendingEnquiriesForUser(String username, int limit) {
        try {
            return enquiryRepository.findTop5ByCreatedByAndStatusOrderByCreatedAtDesc(
                    username, Enquiry.EnquiryStatus.PENDING);
        } catch (Exception ex) {
            // Return empty list if there's an error
            return java.util.Collections.emptyList();
        }
    }
}
