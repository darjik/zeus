package org.zeus.ims.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.format.annotation.DateTimeFormat;
import org.zeus.ims.dto.OrderDTO;
import org.zeus.ims.dto.OrderDocumentDTO;
import org.zeus.ims.dto.OrderItemDTO;
import org.zeus.ims.entity.Order;
import org.zeus.ims.entity.OrderDocument;
import org.zeus.ims.service.CustomerService;
import org.zeus.ims.service.EnquiryService;
import org.zeus.ims.service.OrderDocumentService;
import org.zeus.ims.service.OrderItemService;
import org.zeus.ims.service.OrderService;
import org.zeus.ims.service.ProductService;
import org.zeus.ims.service.OrderTaskService;
import org.zeus.ims.service.PurchaseOrderService;
import org.zeus.ims.service.VendorService;
import org.zeus.ims.service.OrderItemPartService;
import org.zeus.ims.validation.CreateOrder;
import org.zeus.ims.validation.UpdateOrder;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/orders")
@PreAuthorize("hasAnyAuthority('OWNER', 'SALES', 'PRODUCTION_MANAGER', 'WORKSHOP_PERSONNEL')")
public class OrderController {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final OrderDocumentService orderDocumentService;
    private final CustomerService customerService;
    private final ProductService productService;
    private final EnquiryService enquiryService;
    private final OrderTaskService orderTaskService;
    private final VendorService vendorService;
    private final PurchaseOrderService purchaseOrderService;
    private final OrderItemPartService orderItemPartService;

    @Autowired
    public OrderController(OrderService orderService,
                          OrderItemService orderItemService,
                          OrderDocumentService orderDocumentService,
                          CustomerService customerService,
                          ProductService productService,
                          EnquiryService enquiryService,
                          OrderTaskService orderTaskService,
                          VendorService vendorService,
                          PurchaseOrderService purchaseOrderService,
                          OrderItemPartService orderItemPartService) {
        this.orderService = orderService;
        this.orderItemService = orderItemService;
        this.orderDocumentService = orderDocumentService;
        this.customerService = customerService;
        this.productService = productService;
        this.enquiryService = enquiryService;
        this.orderTaskService = orderTaskService;
        this.vendorService = vendorService;
        this.purchaseOrderService = purchaseOrderService;
        this.orderItemPartService = orderItemPartService;
    }

    @GetMapping
    public String listOrders(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(required = false) String search,
                            @RequestParam(required = false) String status,
                            @RequestParam(required = false) Long customerId,
                            Model model,
                            Authentication authentication) {

        Pageable pageable = PageRequest.of(page, size);
        Page<OrderDTO> ordersPage;

        if (customerId != null) {
            ordersPage = orderService.getOrdersByCustomer(customerId, pageable);
            model.addAttribute("customerId", customerId);
        } else if (status != null && !status.isEmpty()) {
            try {
                Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status);
                ordersPage = orderService.getOrdersByStatus(orderStatus, pageable);
                model.addAttribute("selectedStatus", status);
            } catch (IllegalArgumentException ex) {
                ordersPage = orderService.getAllOrders(pageable);
            }
        } else if (search != null && !search.trim().isEmpty()) {
            ordersPage = orderService.searchOrders(search, pageable);
            model.addAttribute("search", search);
        } else {
            ordersPage = orderService.getAllOrders(pageable);
        }

        model.addAttribute("ordersPage", ordersPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ordersPage.getTotalPages());
        model.addAttribute("totalElements", ordersPage.getTotalElements());
        model.addAttribute("orderStatuses", Order.OrderStatus.values());
        model.addAttribute("customers", customerService.getAllCustomers());

        // Add read-only mode for production users
        String userRole = authentication.getAuthorities().iterator().next().getAuthority();
        boolean isReadOnly = "PRODUCTION_MANAGER".equals(userRole) || "WORKSHOP_PERSONNEL".equals(userRole);
        model.addAttribute("readOnlyMode", isReadOnly);

        return "orders/list";
    }

    @GetMapping("/{id}")
    public String viewOrder(@PathVariable Long id, Model model, Authentication authentication) {
        Optional<OrderDTO> orderOpt = orderService.getOrderById(id);
        if (!orderOpt.isPresent()) {
            return "redirect:/orders?error=Order not found";
        }

        OrderDTO order = orderOpt.get();
        List<OrderItemDTO> orderItems = orderItemService.getItemsByOrderId(id);
        List<OrderDocumentDTO> orderDocuments = orderDocumentService.getDocumentsByOrderId(id);
        model.addAttribute("vendors", vendorService.getActiveVendors());
        model.addAttribute("purchaseOrders", purchaseOrderService.getPurchaseOrdersByOrderId(id));

        model.addAttribute("order", order);
        model.addAttribute("orderItems", orderItems);
        model.addAttribute("orderDocuments", orderDocuments);
        model.addAttribute("documentTypes", OrderDocument.DocumentType.values());
        model.addAttribute("orderStatuses", Order.OrderStatus.values());

        // Add read-only mode for production users
        String userRole = authentication.getAuthorities().iterator().next().getAuthority();
        boolean isReadOnly = "PRODUCTION_MANAGER".equals(userRole) || "WORKSHOP_PERSONNEL".equals(userRole);
        model.addAttribute("readOnlyMode", isReadOnly);

        return "orders/view";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyAuthority('OWNER', 'SALES')")
    public String newOrder(@RequestParam(required = false) Long enquiryId, Model model) {
        OrderDTO orderDTO = new OrderDTO();

        if (enquiryId != null) {
            model.addAttribute("enquiryId", enquiryId);
            model.addAttribute("fromEnquiry", true);
        }

        model.addAttribute("order", orderDTO);
        model.addAttribute("customers", customerService.getAllCustomers());
        model.addAttribute("orderPriorities", Order.OrderPriority.values());
        model.addAttribute("orderStatuses", Order.OrderStatus.values());
        model.addAttribute("editMode", false);

        return "orders/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('OWNER', 'SALES')")
    public String createOrder(@ModelAttribute("order") @Validated(CreateOrder.class) OrderDTO orderDTO,
                             BindingResult result,
                             @RequestParam(required = false) Long enquiryId,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes,
                             Model model) {

        if (result.hasErrors()) {
            model.addAttribute("customers", customerService.getAllCustomers());
            model.addAttribute("orderPriorities", Order.OrderPriority.values());
            model.addAttribute("orderStatuses", Order.OrderStatus.values());
            model.addAttribute("editMode", false);
            if (enquiryId != null) {
                model.addAttribute("enquiryId", enquiryId);
                model.addAttribute("fromEnquiry", true);
            }
            return "orders/form";
        }

        try {
            String username = authentication.getName();
            OrderDTO savedOrder;

            if (enquiryId != null) {
                savedOrder = orderService.createOrderFromEnquiry(enquiryId, username);
            } else {
                savedOrder = orderService.createOrder(orderDTO, username);
            }

            // Create default tasks for the new order
            orderTaskService.createDefaultTasksForOrder(savedOrder.getId(), username);

            redirectAttributes.addFlashAttribute("success", "Order created successfully with number: " + savedOrder.getOrderNumber());
            return "redirect:/orders/" + savedOrder.getId();
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Failed to create order: " + ex.getMessage());
            return "redirect:/orders/new" + (enquiryId != null ? "?enquiryId=" + enquiryId : "");
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyAuthority('OWNER', 'SALES')")
    public String editOrder(@PathVariable Long id, Model model) {
        Optional<OrderDTO> orderOpt = orderService.getOrderById(id);
        if (!orderOpt.isPresent()) {
            return "redirect:/orders?error=Order not found";
        }

        model.addAttribute("order", orderOpt.get());
        model.addAttribute("customers", customerService.getAllCustomers());
        model.addAttribute("orderPriorities", Order.OrderPriority.values());
        model.addAttribute("orderStatuses", Order.OrderStatus.values());
        model.addAttribute("editMode", true);

        return "orders/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'SALES')")
    public String updateOrder(@PathVariable Long id,
                             @ModelAttribute("order") @Validated(UpdateOrder.class) OrderDTO orderDTO,
                             BindingResult result,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes,
                             Model model) {

        if (result.hasErrors()) {
            // Log validation errors for debugging
            result.getAllErrors().forEach(error ->
                System.err.println("Validation error: " + error.getDefaultMessage())
            );

            model.addAttribute("customers", customerService.getAllCustomers());
            model.addAttribute("orderPriorities", Order.OrderPriority.values());
            model.addAttribute("orderStatuses", Order.OrderStatus.values());
            model.addAttribute("editMode", true);
            return "orders/form";
        }

        try {
            String username = authentication.getName();
            orderService.updateOrder(id, orderDTO, username);
            redirectAttributes.addFlashAttribute("success", "Order updated successfully");
            return "redirect:/orders/" + id;
        } catch (Exception ex) {
            // Log the exception for debugging
            System.err.println("Error updating order: " + ex.getMessage());
            ex.printStackTrace();

            redirectAttributes.addFlashAttribute("error", "Failed to update order: " + ex.getMessage());
            return "redirect:/orders/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('OWNER', 'SALES', 'PRODUCTION_MANAGER')")
    public String updateOrderStatus(@PathVariable Long id,
                                   @RequestParam Order.OrderStatus status,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            orderService.updateOrderStatus(id, status, username);
            redirectAttributes.addFlashAttribute("success", "Order status updated successfully");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Failed to update order status: " + ex.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyAuthority('OWNER', 'SALES')")
    public String deleteOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orderService.deleteOrder(id);
            redirectAttributes.addFlashAttribute("success", "Order deleted successfully");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete order: " + ex.getMessage());
        }
        return "redirect:/orders";
    }

    @PostMapping("/{orderId}/items")
    @PreAuthorize("hasAnyAuthority('OWNER', 'SALES')")
    public String addOrderItem(@PathVariable Long orderId,
                              @ModelAttribute OrderItemDTO orderItemDTO,
                              RedirectAttributes redirectAttributes) {
        try {
            orderItemDTO.setOrderId(orderId);
            orderItemService.createOrderItem(orderItemDTO);
            redirectAttributes.addFlashAttribute("success", "Order item added successfully");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Failed to add order item: " + ex.getMessage());
        }
        return "redirect:/orders/" + orderId;
    }

    @PostMapping("/{orderId}/items/{itemId}/delete")
    @PreAuthorize("hasAnyAuthority('OWNER', 'SALES')")
    public String deleteOrderItem(@PathVariable Long orderId,
                                 @PathVariable Long itemId,
                                 RedirectAttributes redirectAttributes) {
        try {
            orderItemService.deleteOrderItem(itemId);
            redirectAttributes.addFlashAttribute("success", "Order item removed successfully");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Failed to remove order item: " + ex.getMessage());
        }
        return "redirect:/orders/" + orderId;
    }

    @PostMapping("/{orderId}/documents")
    @PreAuthorize("hasAnyAuthority('OWNER', 'SALES')")
    public String uploadDocument(@PathVariable Long orderId,
                                @RequestParam("file") MultipartFile file,
                                @RequestParam OrderDocument.DocumentType documentType,
                                @RequestParam(required = false) String description,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please select a file to upload");
                return "redirect:/orders/" + orderId;
            }

            String username = authentication.getName();
            orderDocumentService.uploadDocument(orderId, file, documentType, description, username);
            redirectAttributes.addFlashAttribute("success", "Document uploaded successfully");
        } catch (IOException ex) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload document: " + ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload document: " + ex.getMessage());
        }
        return "redirect:/orders/" + orderId;
    }

    @GetMapping("/{orderId}/documents/{documentId}/download")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long orderId,
                                                  @PathVariable Long documentId) {
        try {
            Optional<OrderDocumentDTO> documentOpt = orderDocumentService.getDocumentById(documentId);
            if (!documentOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            OrderDocumentDTO document = documentOpt.get();

            // Verify the document belongs to the specified order
            if (!document.getOrderId().equals(orderId)) {
                return ResponseEntity.notFound().build();
            }

            byte[] fileContent = orderDocumentService.downloadDocument(documentId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", document.getOriginalFilename());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileContent);
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{orderId}/documents/{documentId}/delete")
    @PreAuthorize("hasAnyAuthority('OWNER', 'SALES')")
    public String deleteDocument(@PathVariable Long orderId,
                                @PathVariable Long documentId,
                                RedirectAttributes redirectAttributes) {
        try {
            orderDocumentService.deleteDocument(documentId);
            redirectAttributes.addFlashAttribute("success", "Document deleted successfully");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete document: " + ex.getMessage());
        }
        return "redirect:/orders/" + orderId;
    }

    @PostMapping("/{orderId}/purchase-orders")
    @PreAuthorize("hasAnyAuthority('OWNER', 'SALES', 'PRODUCTION_MANAGER')")
    public String createPurchaseOrder(@PathVariable Long orderId,
                                      @RequestParam Long vendorId,
                                      @RequestParam(required = false) List<Long> orderItemIds,
                                      @RequestParam(required = false) String notes,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expectedDeliveryDate,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            purchaseOrderService.createPurchaseOrder(orderId, vendorId, orderItemIds, expectedDeliveryDate, notes, username);
            redirectAttributes.addFlashAttribute("success", "Purchase order created successfully");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Failed to create purchase order: " + ex.getMessage());
        }
        return "redirect:/orders/" + orderId;
    }

    @PostMapping("/{orderId}/items/{itemId}/status")
    @PreAuthorize("hasAnyAuthority('OWNER', 'SALES', 'PRODUCTION_MANAGER')")
    public String updateProductStatus(@PathVariable Long orderId,
                                      @PathVariable Long itemId,
                                      @RequestParam String productStatus,
                                      RedirectAttributes redirectAttributes) {
        try {
            OrderItemDTO itemDTO = new OrderItemDTO();
            itemDTO.setProductStatus(productStatus);
            orderItemService.updateOrderItem(itemId, itemDTO);
            redirectAttributes.addFlashAttribute("success", "Product status updated successfully");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Failed to update product status: " + ex.getMessage());
        }
        return "redirect:/orders/" + orderId;
    }
}
