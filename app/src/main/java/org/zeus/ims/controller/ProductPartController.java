package org.zeus.ims.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.zeus.ims.dto.ProductPartDTO;
import org.zeus.ims.dto.VendorDTO;
import org.zeus.ims.service.ProductPartService;
import org.zeus.ims.service.VendorService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/products/{productId}/parts")
@RequiredArgsConstructor
@Slf4j
public class ProductPartController {

    private final ProductPartService productPartService;
    private final VendorService vendorService;

    /**
     * Handle part creation
     */
    @PostMapping("/new")
    public String createPart(@PathVariable Long productId,
                            @Valid @ModelAttribute("newPart") ProductPartDTO partDTO,
                            BindingResult bindingResult,
                            HttpSession session,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        try {
            if (bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please correct the errors in the form.");
                return "redirect:/products/" + productId + "/parts";
            }

            String currentUser = (String) session.getAttribute("username");
            if (currentUser == null) {
                currentUser = "system";
            }

            partDTO.setProductId(productId);
            ProductPartDTO createdPart = productPartService.createPart(partDTO, currentUser);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Part '" + createdPart.getPartName() + "' added successfully!");
            return "redirect:/products/" + productId + "/parts";

        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/products/" + productId + "/parts";
        } catch (Exception ex) {
            log.error("Error creating part for product id: {}", productId, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create part. Please try again.");
            return "redirect:/products/" + productId + "/parts";
        }
    }

    /**
     * Display part edit form
     */
    @GetMapping("/{partId}/edit")
    public String showEditPartForm(@PathVariable Long productId,
                                  @PathVariable Long partId,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            ProductPartDTO part = productPartService.getPartById(partId);
            List<VendorDTO> vendors = vendorService.getActiveVendors();
            List<String> unitOptions = getUnitOfMeasureOptions();

            model.addAttribute("part", part);
            model.addAttribute("vendors", vendors);
            model.addAttribute("unitOptions", unitOptions);
            model.addAttribute("productId", productId);

            return "products/part-form";
        } catch (EntityNotFoundException ex) {
            log.warn("Part not found for edit with id: {}", partId);
            redirectAttributes.addFlashAttribute("errorMessage", "Part not found.");
            return "redirect:/products/" + productId + "/parts";
        } catch (Exception ex) {
            log.error("Error loading part edit form for id: {}", partId, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to load part for editing.");
            return "redirect:/products/" + productId + "/parts";
        }
    }

    /**
     * Handle part update
     */
    @PostMapping("/{partId}/edit")
    public String updatePart(@PathVariable Long productId,
                            @PathVariable Long partId,
                            @Valid @ModelAttribute("part") ProductPartDTO partDTO,
                            BindingResult bindingResult,
                            HttpSession session,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        try {
            if (bindingResult.hasErrors()) {
                List<VendorDTO> vendors = vendorService.getActiveVendors();
                List<String> unitOptions = getUnitOfMeasureOptions();

                model.addAttribute("vendors", vendors);
                model.addAttribute("unitOptions", unitOptions);
                model.addAttribute("productId", productId);
                return "products/part-form";
            }

            String currentUser = (String) session.getAttribute("username");
            if (currentUser == null) {
                currentUser = "system";
            }

            ProductPartDTO updatedPart = productPartService.updatePart(partId, partDTO, currentUser);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Part '" + updatedPart.getPartName() + "' updated successfully!");
            return "redirect:/products/" + productId + "/parts";

        } catch (EntityNotFoundException ex) {
            log.warn("Part not found for update with id: {}", partId);
            redirectAttributes.addFlashAttribute("errorMessage", "Part not found.");
            return "redirect:/products/" + productId + "/parts";
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("partName", "error.part", ex.getMessage());
            List<VendorDTO> vendors = vendorService.getActiveVendors();
            List<String> unitOptions = getUnitOfMeasureOptions();

            model.addAttribute("vendors", vendors);
            model.addAttribute("unitOptions", unitOptions);
            model.addAttribute("productId", productId);
            return "products/part-form";
        } catch (Exception ex) {
            log.error("Error updating part with id: {}", partId, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update part. Please try again.");
            return "redirect:/products/" + productId + "/parts/" + partId + "/edit";
        }
    }

    /**
     * Toggle part active status
     */
    @PostMapping("/{partId}/toggle-status")
    @ResponseBody
    public ResponseEntity<String> togglePartStatus(@PathVariable Long productId,
                                                  @PathVariable Long partId,
                                                  HttpSession session) {
        try {
            String currentUser = (String) session.getAttribute("username");
            if (currentUser == null) {
                currentUser = "system";
            }

            productPartService.togglePartStatus(partId, currentUser);
            return ResponseEntity.ok("Status updated successfully");
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.badRequest().body("Part not found");
        } catch (Exception ex) {
            log.error("Error toggling part status for id: {}", partId, ex);
            return ResponseEntity.badRequest().body("Failed to update status");
        }
    }

    /**
     * Delete part
     */
    @PostMapping("/{partId}/delete")
    public String deletePart(@PathVariable Long productId,
                            @PathVariable Long partId,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        try {
            String currentUser = (String) session.getAttribute("username");
            if (currentUser == null) {
                currentUser = "system";
            }

            ProductPartDTO part = productPartService.getPartById(partId);
            productPartService.deletePart(partId, currentUser);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Part '" + part.getPartName() + "' deleted successfully!");
            return "redirect:/products/" + productId + "/parts";
        } catch (EntityNotFoundException ex) {
            log.warn("Part not found for deletion with id: {}", partId);
            redirectAttributes.addFlashAttribute("errorMessage", "Part not found.");
            return "redirect:/products/" + productId + "/parts";
        } catch (Exception ex) {
            log.error("Error deleting part with id: {}", partId, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete part. Please try again.");
            return "redirect:/products/" + productId + "/parts";
        }
    }

    /**
     * Get unit of measure options
     */
    private List<String> getUnitOfMeasureOptions() {
        return Arrays.asList(
                "Pieces", "Kilograms", "Grams", "Meters", "Centimeters", "Millimeters",
                "Liters", "Milliliters", "Square Meters", "Cubic Meters", "Boxes", "Sets"
        );
    }
}
