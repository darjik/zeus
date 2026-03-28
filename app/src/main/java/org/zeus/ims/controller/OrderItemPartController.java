package org.zeus.ims.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.zeus.ims.dto.OrderItemDTO;
import org.zeus.ims.dto.OrderItemPartDTO;
import org.zeus.ims.dto.ProductPartDTO;
import org.zeus.ims.service.OrderItemPartService;
import org.zeus.ims.service.OrderItemService;
import org.zeus.ims.service.ProductPartService;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/orders/{orderId}/items/{itemId}/parts")
@PreAuthorize("hasAnyAuthority('OWNER', 'SALES', 'PRODUCTION_MANAGER')")
public class OrderItemPartController {

    private final OrderItemPartService orderItemPartService;
    private final OrderItemService orderItemService;
    private final ProductPartService productPartService;

    @Autowired
    public OrderItemPartController(OrderItemPartService orderItemPartService,
                                   OrderItemService orderItemService,
                                   ProductPartService productPartService) {
        this.orderItemPartService = orderItemPartService;
        this.orderItemService = orderItemService;
        this.productPartService = productPartService;
    }

    @GetMapping
    public String listParts(@PathVariable Long orderId,
                           @PathVariable Long itemId,
                           Model model) {
        Optional<OrderItemDTO> orderItemOpt = orderItemService.getItemById(itemId);
        if (!orderItemOpt.isPresent()) {
            return "redirect:/orders/" + orderId + "?error=Order item not found";
        }

        OrderItemDTO orderItem = orderItemOpt.get();
        List<OrderItemPartDTO> parts = orderItemPartService.getPartsByOrderItemId(itemId);
        List<ProductPartDTO> availableParts = productPartService.getActivePartsByProductId(orderItem.getProductId());

        model.addAttribute("orderId", orderId);
        model.addAttribute("orderItem", orderItem);
        model.addAttribute("parts", parts);
        model.addAttribute("availableParts", availableParts);

        return "orders/items/parts";
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('OWNER', 'SALES', 'PRODUCTION_MANAGER')")
    public String addPart(@PathVariable Long orderId,
                         @PathVariable Long itemId,
                         @RequestParam Long productPartId,
                         @RequestParam(required = false) Integer quantityRequired,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            orderItemPartService.addPartToOrderItem(itemId, productPartId, quantityRequired, username);
            redirectAttributes.addFlashAttribute("success", "Part added successfully");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Failed to add part: " + ex.getMessage());
        }
        return "redirect:/orders/" + orderId + "/items/" + itemId + "/parts";
    }

    @PostMapping("/{partId}/delete")
    @PreAuthorize("hasAnyAuthority('OWNER', 'SALES', 'PRODUCTION_MANAGER')")
    public String deletePart(@PathVariable Long orderId,
                            @PathVariable Long itemId,
                            @PathVariable Long partId,
                            RedirectAttributes redirectAttributes) {
        try {
            orderItemPartService.removePartFromOrderItem(partId);
            redirectAttributes.addFlashAttribute("success", "Part removed successfully");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Failed to remove part: " + ex.getMessage());
        }
        return "redirect:/orders/" + orderId + "/items/" + itemId + "/parts";
    }

    @PostMapping("/{partId}/quantity")
    @PreAuthorize("hasAnyAuthority('OWNER', 'SALES', 'PRODUCTION_MANAGER')")
    public String updateQuantity(@PathVariable Long orderId,
                                 @PathVariable Long itemId,
                                 @PathVariable Long partId,
                                 @RequestParam Integer quantityRequired,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            orderItemPartService.updatePartQuantity(partId, quantityRequired, username);
            redirectAttributes.addFlashAttribute("success", "Part quantity updated successfully");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Failed to update part quantity: " + ex.getMessage());
        }
        return "redirect:/orders/" + orderId + "/items/" + itemId + "/parts";
    }

    @GetMapping("/api")
    @PreAuthorize("hasAnyAuthority('OWNER', 'SALES', 'PRODUCTION_MANAGER', 'WORKSHOP_PERSONNEL')")
    public ResponseEntity<List<OrderItemPartDTO>> getPartsApi(@PathVariable Long orderId,
                                                              @PathVariable Long itemId) {
        List<OrderItemPartDTO> parts = orderItemPartService.getPartsByOrderItemId(itemId);
        return ResponseEntity.ok(parts);
    }
}

