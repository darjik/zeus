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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.zeus.ims.dto.VendorDTO;
import org.zeus.ims.dto.VendorPersonnelDTO;
import org.zeus.ims.service.VendorPersonnelService;
import org.zeus.ims.service.VendorService;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/vendors/{vendorId}/personnel")
@RequiredArgsConstructor
@Slf4j
public class VendorPersonnelController {

    private final VendorService vendorService;
    private final VendorPersonnelService vendorPersonnelService;

    /**
     * Display personnel details page
     */
    @GetMapping("/view/{id}")
    public String viewPersonnel(@PathVariable Long vendorId, @PathVariable Long id, Model model) {
        try {
            VendorDTO vendor = vendorService.getVendorById(vendorId);
            VendorPersonnelDTO personnel = vendorPersonnelService.getPersonnelById(id);

            // Verify personnel belongs to vendor
            if (!personnel.getVendorId().equals(vendorId)) {
                throw new IllegalArgumentException("Personnel does not belong to this vendor");
            }

            model.addAttribute("vendor", vendor);
            model.addAttribute("personnel", personnel);

            return "vendors/personnel/view";
        } catch (EntityNotFoundException ex) {
            log.error("Personnel or vendor not found", ex);
            model.addAttribute("errorMessage", "Personnel or vendor not found.");
            return "redirect:/vendors/" + vendorId + "/personnel";
        } catch (Exception ex) {
            log.error("Error loading personnel details", ex);
            model.addAttribute("errorMessage", "Error loading personnel details. Please try again.");
            return "redirect:/vendors/" + vendorId + "/personnel";
        }
    }

    /**
     * Display new personnel form
     */
    @GetMapping("/new")
    public String newPersonnelForm(@PathVariable Long vendorId, Model model) {
        try {
            VendorDTO vendor = vendorService.getVendorById(vendorId);
            VendorPersonnelDTO personnelDTO = new VendorPersonnelDTO();
            personnelDTO.setVendorId(vendorId);

            model.addAttribute("vendor", vendor);
            model.addAttribute("personnelDTO", personnelDTO);
            model.addAttribute("pageTitle", "Add New Personnel");
            model.addAttribute("isEdit", false);
            addFormData(model);

            return "vendors/personnel/form";
        } catch (EntityNotFoundException ex) {
            log.error("Vendor not found with id: {}", vendorId, ex);
            model.addAttribute("errorMessage", "Vendor not found.");
            return "redirect:/vendors";
        }
    }

    /**
     * Display edit personnel form
     */
    @GetMapping("/edit/{id}")
    public String editPersonnelForm(@PathVariable Long vendorId, @PathVariable Long id, Model model) {
        try {
            VendorDTO vendor = vendorService.getVendorById(vendorId);
            VendorPersonnelDTO personnel = vendorPersonnelService.getPersonnelById(id);

            // Verify personnel belongs to vendor
            if (!personnel.getVendorId().equals(vendorId)) {
                throw new IllegalArgumentException("Personnel does not belong to this vendor");
            }

            model.addAttribute("vendor", vendor);
            model.addAttribute("personnelDTO", personnel);
            model.addAttribute("pageTitle", "Edit Personnel");
            model.addAttribute("isEdit", true);
            addFormData(model);

            return "vendors/personnel/form";
        } catch (EntityNotFoundException ex) {
            log.error("Personnel or vendor not found", ex);
            model.addAttribute("errorMessage", "Personnel or vendor not found.");
            return "redirect:/vendors/" + vendorId + "/personnel";
        } catch (Exception ex) {
            log.error("Error loading personnel for edit", ex);
            model.addAttribute("errorMessage", "Error loading personnel for editing. Please try again.");
            return "redirect:/vendors/" + vendorId + "/personnel";
        }
    }

    /**
     * Handle new personnel form submission
     */
    @PostMapping("/new")
    public String createPersonnel(@PathVariable Long vendorId,
                                 @Valid @ModelAttribute VendorPersonnelDTO personnelDTO,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        personnelDTO.setVendorId(vendorId);

        if (result.hasErrors()) {
            try {
                VendorDTO vendor = vendorService.getVendorById(vendorId);
                model.addAttribute("vendor", vendor);
                model.addAttribute("pageTitle", "Add New Personnel");
                model.addAttribute("isEdit", false);
                addFormData(model);
                return "vendors/personnel/form";
            } catch (EntityNotFoundException ex) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vendor not found.");
                return "redirect:/vendors";
            }
        }

        try {
            VendorPersonnelDTO createdPersonnel = vendorPersonnelService.createPersonnel(personnelDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                "Personnel '" + createdPersonnel.getFullName() + "' has been added successfully.");
            return "redirect:/vendors/" + vendorId + "/personnel/view/" + createdPersonnel.getId();
        } catch (IllegalArgumentException ex) {
            log.error("Validation error creating personnel", ex);
            try {
                VendorDTO vendor = vendorService.getVendorById(vendorId);
                model.addAttribute("vendor", vendor);
                model.addAttribute("errorMessage", ex.getMessage());
                model.addAttribute("pageTitle", "Add New Personnel");
                model.addAttribute("isEdit", false);
                addFormData(model);
                return "vendors/personnel/form";
            } catch (EntityNotFoundException vendorEx) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vendor not found.");
                return "redirect:/vendors";
            }
        } catch (Exception ex) {
            log.error("Error creating personnel", ex);
            try {
                VendorDTO vendor = vendorService.getVendorById(vendorId);
                model.addAttribute("vendor", vendor);
                model.addAttribute("errorMessage", "Error creating personnel. Please try again.");
                model.addAttribute("pageTitle", "Add New Personnel");
                model.addAttribute("isEdit", false);
                addFormData(model);
                return "vendors/personnel/form";
            } catch (EntityNotFoundException vendorEx) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vendor not found.");
                return "redirect:/vendors";
            }
        }
    }

    /**
     * Handle edit personnel form submission
     */
    @PostMapping("/edit/{id}")
    public String updatePersonnel(@PathVariable Long vendorId,
                                 @PathVariable Long id,
                                 @Valid @ModelAttribute VendorPersonnelDTO personnelDTO,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        personnelDTO.setVendorId(vendorId);

        if (result.hasErrors()) {
            try {
                VendorDTO vendor = vendorService.getVendorById(vendorId);
                model.addAttribute("vendor", vendor);
                model.addAttribute("pageTitle", "Edit Personnel");
                model.addAttribute("isEdit", true);
                addFormData(model);
                return "vendors/personnel/form";
            } catch (EntityNotFoundException ex) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vendor not found.");
                return "redirect:/vendors";
            }
        }

        try {
            VendorPersonnelDTO updatedPersonnel = vendorPersonnelService.updatePersonnel(id, personnelDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                "Personnel '" + updatedPersonnel.getFullName() + "' has been updated successfully.");
            return "redirect:/vendors/" + vendorId + "/personnel/view/" + id;
        } catch (EntityNotFoundException ex) {
            log.error("Personnel or vendor not found", ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Personnel or vendor not found.");
            return "redirect:/vendors/" + vendorId + "/personnel";
        } catch (IllegalArgumentException ex) {
            log.error("Validation error updating personnel", ex);
            try {
                VendorDTO vendor = vendorService.getVendorById(vendorId);
                model.addAttribute("vendor", vendor);
                model.addAttribute("errorMessage", ex.getMessage());
                model.addAttribute("pageTitle", "Edit Personnel");
                model.addAttribute("isEdit", true);
                addFormData(model);
                return "vendors/personnel/form";
            } catch (EntityNotFoundException vendorEx) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vendor not found.");
                return "redirect:/vendors";
            }
        } catch (Exception ex) {
            log.error("Error updating personnel", ex);
            try {
                VendorDTO vendor = vendorService.getVendorById(vendorId);
                model.addAttribute("vendor", vendor);
                model.addAttribute("errorMessage", "Error updating personnel. Please try again.");
                model.addAttribute("pageTitle", "Edit Personnel");
                model.addAttribute("isEdit", true);
                addFormData(model);
                return "vendors/personnel/form";
            } catch (EntityNotFoundException vendorEx) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vendor not found.");
                return "redirect:/vendors";
            }
        }
    }

    /**
     * Handle personnel deletion (deactivation)
     */
    @PostMapping("/delete/{id}")
    public String deletePersonnel(@PathVariable Long vendorId,
                                 @PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        try {
            VendorPersonnelDTO personnel = vendorPersonnelService.getPersonnelById(id);
            vendorPersonnelService.deletePersonnel(id);
            redirectAttributes.addFlashAttribute("successMessage",
                "Personnel '" + personnel.getFullName() + "' has been deactivated successfully.");
        } catch (EntityNotFoundException ex) {
            log.error("Personnel not found with id: {}", id, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Personnel not found.");
        } catch (Exception ex) {
            log.error("Error deleting personnel with id: {}", id, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Error deactivating personnel. Please try again.");
        }
        return "redirect:/vendors/" + vendorId + "/personnel";
    }

    /**
     * Set personnel as primary contact
     */
    @PostMapping("/set-primary/{id}")
    public String setPrimaryContact(@PathVariable Long vendorId,
                                   @PathVariable Long id,
                                   RedirectAttributes redirectAttributes) {
        try {
            vendorPersonnelService.setPrimaryContact(vendorId, id);
            VendorPersonnelDTO personnel = vendorPersonnelService.getPersonnelById(id);
            redirectAttributes.addFlashAttribute("successMessage",
                personnel.getFullName() + " has been set as primary contact.");
        } catch (EntityNotFoundException ex) {
            log.error("Personnel not found with id: {}", id, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Personnel not found.");
        } catch (Exception ex) {
            log.error("Error setting primary contact", ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Error setting primary contact. Please try again.");
        }
        return "redirect:/vendors/" + vendorId + "/personnel";
    }

    /**
     * AJAX endpoint to check email availability within vendor
     */
    @GetMapping("/check-email")
    @ResponseBody
    public ResponseEntity<Boolean> checkEmailAvailability(@PathVariable Long vendorId,
                                                         @RequestParam String email,
                                                         @RequestParam(required = false) Long personnelId) {
        try {
            // For now, return true as email uniqueness check would need repository method
            // This can be enhanced later with actual email checking logic
            return ResponseEntity.ok(true);
        } catch (Exception ex) {
            log.error("Error checking email availability", ex);
            return ResponseEntity.ok(false);
        }
    }

    /**
     * Add common form data to model
     */
    private void addFormData(Model model) {
        model.addAttribute("departments", getDepartments());
        model.addAttribute("designations", getDesignations());
    }

    /**
     * Get list of departments for dropdown
     */
    private List<String> getDepartments() {
        return Arrays.asList(
            "Sales", "Marketing", "Operations", "Finance", "Human Resources",
            "IT", "Manufacturing", "Quality Control", "Research & Development",
            "Customer Service", "Procurement", "Logistics"
        );
    }

    /**
     * Get list of designations for dropdown
     */
    private List<String> getDesignations() {
        return Arrays.asList(
            "Manager", "Assistant Manager", "Executive", "Senior Executive",
            "Team Lead", "Supervisor", "Coordinator", "Specialist",
            "Analyst", "Officer", "Associate", "Director"
        );
    }
}
