package org.zeus.ims.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import org.zeus.ims.dto.EnquiryConversationDTO;
import org.zeus.ims.dto.EnquiryDTO;
import org.zeus.ims.dto.EnquiryItemDTO;
import org.zeus.ims.dto.OrderDTO;
import org.zeus.ims.entity.Enquiry;
import org.zeus.ims.entity.EnquiryConversation;
import org.zeus.ims.entity.EnquiryItem;
import org.zeus.ims.service.CustomerService;
import org.zeus.ims.service.EnquiryConversationService;
import org.zeus.ims.service.EnquiryItemService;
import org.zeus.ims.service.EnquiryService;
import org.zeus.ims.service.OrderService;
import org.zeus.ims.service.ProductService;
import org.zeus.ims.validation.CreateEnquiry;
import org.zeus.ims.validation.UpdateEnquiry;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/enquiries")
@PreAuthorize("hasAnyAuthority('OWNER', 'SALES')")
public class EnquiryController {

    private final EnquiryService enquiryService;
    private final EnquiryItemService enquiryItemService;
    private final EnquiryConversationService conversationService;
    private final CustomerService customerService;
    private final ProductService productService;
    private final OrderService orderService;

    @Autowired
    public EnquiryController(EnquiryService enquiryService,
                             EnquiryItemService enquiryItemService,
                             EnquiryConversationService conversationService,
                             CustomerService customerService,
                             ProductService productService,
                             OrderService orderService) {
        this.enquiryService = enquiryService;
        this.enquiryItemService = enquiryItemService;
        this.conversationService = conversationService;
        this.customerService = customerService;
        this.productService = productService;
        this.orderService = orderService;
    }

    @GetMapping
    public String listEnquiries(@RequestParam(value = "search", required = false) String search,
                                @RequestParam(value = "customerId", required = false) Long customerId,
                                @RequestParam(value = "productId", required = false) Long productId,
                                @RequestParam(value = "status", required = false) String status,
                                @RequestParam(value = "priority", required = false) String priority,
                                Model model) {
        List<Enquiry> enquiries;

        if (search != null && !search.trim().isEmpty()) {
            enquiries = enquiryService.searchEnquiries(search);
            model.addAttribute("search", search);
        } else if (customerId != null || productId != null || status != null || priority != null) {
            Enquiry.EnquiryStatus enquiryStatus = status != null ? Enquiry.EnquiryStatus.valueOf(status) : null;
            Enquiry.EnquiryPriority enquiryPriority = priority != null ? Enquiry.EnquiryPriority.valueOf(priority) : null;
            enquiries = enquiryService.getEnquiriesWithFilters(customerId, productId, enquiryStatus, enquiryPriority);
            model.addAttribute("customerId", customerId);
            model.addAttribute("productId", productId);
            model.addAttribute("status", status);
            model.addAttribute("priority", priority);
        } else {
            enquiries = enquiryService.getAllActiveEnquiries();
        }

        model.addAttribute("enquiries", enquiries);
        model.addAttribute("totalEnquiries", enquiryService.getActiveEnquiryCount());
        model.addAttribute("pendingCount", enquiryService.getEnquiryCountByStatus(Enquiry.EnquiryStatus.PENDING));
        model.addAttribute("quotedCount", enquiryService.getEnquiryCountByStatus(Enquiry.EnquiryStatus.QUOTED));
        model.addAttribute("approvedCount", enquiryService.getEnquiryCountByStatus(Enquiry.EnquiryStatus.APPROVED));
        model.addAttribute("customers", customerService.getAllActiveCustomers());
        model.addAttribute("products", productService.getActiveProducts());
        model.addAttribute("statuses", Enquiry.EnquiryStatus.values());
        model.addAttribute("priorities", Enquiry.EnquiryPriority.values());

        return "enquiries/list";
    }

    @GetMapping("/new")
    public String showCreateEnquiryForm(Model model) {
        model.addAttribute("enquiryDTO", new EnquiryDTO());
        model.addAttribute("pageTitle", "Add New Enquiry");
        model.addAttribute("isEdit", false);
        model.addAttribute("customers", customerService.getAllActiveCustomers());
        model.addAttribute("products", productService.getActiveProducts());
        model.addAttribute("statuses", Enquiry.EnquiryStatus.values());
        model.addAttribute("priorities", Enquiry.EnquiryPriority.values());
        return "enquiries/form";
    }

    @PostMapping("/new")
    public String createEnquiry(@Validated(CreateEnquiry.class) @ModelAttribute("enquiryDTO") EnquiryDTO enquiryDTO,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Add New Enquiry");
            model.addAttribute("isEdit", false);
            model.addAttribute("customers", customerService.getAllActiveCustomers());
            model.addAttribute("products", productService.getActiveProducts());
            model.addAttribute("statuses", Enquiry.EnquiryStatus.values());
            model.addAttribute("priorities", Enquiry.EnquiryPriority.values());
            return "enquiries/form";
        }

        try {
            Enquiry savedEnquiry = enquiryService.createEnquiry(enquiryDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Enquiry created successfully!");
            return "redirect:/enquiries/" + savedEnquiry.getId();
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Error creating enquiry: " + ex.getMessage());
            model.addAttribute("pageTitle", "Add New Enquiry");
            model.addAttribute("isEdit", false);
            model.addAttribute("customers", customerService.getAllActiveCustomers());
            model.addAttribute("products", productService.getActiveProducts());
            model.addAttribute("statuses", Enquiry.EnquiryStatus.values());
            model.addAttribute("priorities", Enquiry.EnquiryPriority.values());
            return "enquiries/form";
        }
    }

    @GetMapping("/{id}")
    public String viewEnquiry(@PathVariable Long id, Model model) {
        Optional<Enquiry> enquiryOpt = enquiryService.getEnquiryById(id);
        if (enquiryOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Enquiry not found");
            return "redirect:/enquiries";
        }

        Enquiry enquiry = enquiryOpt.get();
        List<EnquiryItem> items = enquiryItemService.getItemsByEnquiryId(id);
        List<EnquiryConversation> conversations = conversationService.getConversationsByEnquiryId(id);

        model.addAttribute("enquiry", enquiry);
        model.addAttribute("items", items);
        model.addAttribute("conversations", conversations);
        model.addAttribute("conversationDTO", new EnquiryConversationDTO());
        model.addAttribute("itemDTO", new EnquiryItemDTO());
        model.addAttribute("messageTypes", EnquiryConversation.MessageType.values());
        model.addAttribute("messageDirections", EnquiryConversation.MessageDirection.values());
        model.addAttribute("conversationCount", conversations.size());
        model.addAttribute("products", productService.getActiveProducts());
        model.addAttribute("totalAmount", enquiryItemService.calculateTotalAmountForEnquiry(id));

        // Fetch related orders if enquiry is converted
        if (enquiry.getStatus() == Enquiry.EnquiryStatus.CONVERTED) {
            model.addAttribute("relatedOrders", orderService.getOrdersByEnquiryId(id));
        }

        return "enquiries/view";
    }

    @GetMapping("/{id}/edit")
    public String showEditEnquiryForm(@PathVariable Long id, Model model) {
        Optional<Enquiry> enquiryOpt = enquiryService.getEnquiryById(id);
        if (enquiryOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Enquiry not found");
            return "redirect:/enquiries";
        }

        Enquiry enquiry = enquiryOpt.get();
        EnquiryDTO enquiryDTO = enquiryService.convertToDTO(enquiry);

        model.addAttribute("enquiryDTO", enquiryDTO);
        model.addAttribute("pageTitle", "Edit Enquiry");
        model.addAttribute("isEdit", true);
        model.addAttribute("customers", customerService.getAllActiveCustomers());
        model.addAttribute("products", productService.getActiveProducts());
        model.addAttribute("statuses", Enquiry.EnquiryStatus.values());
        model.addAttribute("priorities", Enquiry.EnquiryPriority.values());

        return "enquiries/form";
    }

    @PostMapping("/{id}/edit")
    public String updateEnquiry(@PathVariable Long id,
                                @Validated(UpdateEnquiry.class) @ModelAttribute("enquiryDTO") EnquiryDTO enquiryDTO,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Enquiry");
            model.addAttribute("isEdit", true);
            model.addAttribute("customers", customerService.getAllActiveCustomers());
            model.addAttribute("products", productService.getActiveProducts());
            model.addAttribute("statuses", Enquiry.EnquiryStatus.values());
            model.addAttribute("priorities", Enquiry.EnquiryPriority.values());
            return "enquiries/form";
        }

        try {
            enquiryService.updateEnquiry(id, enquiryDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Enquiry updated successfully!");
            return "redirect:/enquiries/" + id;
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Error updating enquiry: " + ex.getMessage());
            model.addAttribute("pageTitle", "Edit Enquiry");
            model.addAttribute("isEdit", true);
            model.addAttribute("customers", customerService.getAllActiveCustomers());
            model.addAttribute("products", productService.getActiveProducts());
            model.addAttribute("statuses", Enquiry.EnquiryStatus.values());
            model.addAttribute("priorities", Enquiry.EnquiryPriority.values());
            return "enquiries/form";
        }
    }

    @PostMapping("/{id}/items")
    public String addItemToEnquiry(@PathVariable Long id,
                                   @Validated @ModelAttribute("itemDTO") EnquiryItemDTO itemDTO,
                                   BindingResult result,
                                   RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding item: Invalid data provided");
            return "redirect:/enquiries/" + id;
        }

        try {
            enquiryItemService.addItemToEnquiry(id, itemDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Item added successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding item: " + ex.getMessage());
        }

        return "redirect:/enquiries/" + id;
    }

    @PostMapping("/{enquiryId}/items/{itemId}/delete")
    public String deleteItemFromEnquiry(@PathVariable Long enquiryId,
                                        @PathVariable Long itemId,
                                        RedirectAttributes redirectAttributes) {
        try {
            enquiryItemService.deleteEnquiryItem(itemId);
            redirectAttributes.addFlashAttribute("successMessage", "Item removed successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error removing item: " + ex.getMessage());
        }

        return "redirect:/enquiries/" + enquiryId;
    }

    @PostMapping("/{id}/delete")
    public String deleteEnquiry(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            enquiryService.deleteEnquiry(id);
            redirectAttributes.addFlashAttribute("successMessage", "Enquiry deactivated successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deactivating enquiry: " + ex.getMessage());
        }
        return "redirect:/enquiries";
    }

    @PostMapping("/{id}/conversations")
    public String addConversation(@PathVariable Long id,
                                  @Validated @ModelAttribute("conversationDTO") EnquiryConversationDTO conversationDTO,
                                  @RequestParam(value = "attachments", required = false) List<MultipartFile> attachments,
                                  BindingResult result,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding conversation: Invalid data provided");
            return "redirect:/enquiries/" + id;
        }

        try {
            conversationDTO.setEnquiryId(id);
            if (attachments != null && !attachments.isEmpty()) {
                conversationService.addConversationWithAttachments(conversationDTO, attachments);
            } else {
                conversationService.addConversation(conversationDTO);
            }
            redirectAttributes.addFlashAttribute("successMessage", "Conversation added successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding conversation: " + ex.getMessage());
        }

        return "redirect:/enquiries/" + id;
    }

    @PostMapping("/{enquiryId}/conversations/{conversationId}/attachments/{attachmentId}/delete")
    public String deleteAttachment(@PathVariable Long enquiryId,
                                   @PathVariable Long conversationId,
                                   @PathVariable Long attachmentId,
                                   RedirectAttributes redirectAttributes) {
        try {
            conversationService.deleteAttachment(attachmentId);
            redirectAttributes.addFlashAttribute("successMessage", "Attachment deleted successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting attachment: " + ex.getMessage());
        }

        return "redirect:/enquiries/" + enquiryId;
    }

    @PostMapping("/{id}/convert-to-order")
    @PreAuthorize("hasAnyAuthority('OWNER', 'SALES')")
    public String convertEnquiryToOrder(@PathVariable Long id,
                                        RedirectAttributes redirectAttributes) {
        try {
            String currentUser = getCurrentUser();
            OrderDTO orderDTO = orderService.createOrderFromEnquiry(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage",
                "Enquiry successfully converted to order!");
            return "redirect:/orders/" + orderDTO.getId();
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                "Error converting enquiry to order: " + ex.getMessage());
            return "redirect:/enquiries/" + id;
        }
    }

    private String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null) {
            return authentication.getName();
        }
        return "system";
    }
}
