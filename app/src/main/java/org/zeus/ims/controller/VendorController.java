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
@RequestMapping("/vendors")
@RequiredArgsConstructor
@Slf4j
public class VendorController {

    private final VendorService vendorService;
    private final VendorPersonnelService vendorPersonnelService;

    /**
     * Display vendors list page
     */
    @GetMapping
    public String listVendors(@RequestParam(value = "search", required = false) String search, Model model) {
        try {
            List<VendorDTO> vendors = vendorService.getAllVendors(search);
            long totalVendors = vendorService.getTotalVendorCount();

            model.addAttribute("vendors", vendors);
            model.addAttribute("totalVendors", totalVendors);
            model.addAttribute("search", search);

            return "vendors/list";
        } catch (Exception ex) {
            log.error("Error loading vendors list", ex);
            model.addAttribute("errorMessage", "Error loading vendors. Please try again.");
            return "vendors/list";
        }
    }

    /**
     * Display vendor details page
     */
    @GetMapping("/view/{id}")
    public String viewVendor(@PathVariable Long id, Model model) {
        try {
            VendorDTO vendor = vendorService.getVendorById(id);
            List<VendorPersonnelDTO> personnel = vendorPersonnelService.getActiveVendorPersonnel(id);
            VendorPersonnelDTO primaryContact = vendorPersonnelService.getPrimaryContact(id);
            long personnelCount = vendorPersonnelService.getPersonnelCount(id, true);

            model.addAttribute("vendor", vendor);
            model.addAttribute("personnel", personnel);
            model.addAttribute("primaryContact", primaryContact);
            model.addAttribute("personnelCount", personnelCount);

            return "vendors/view";
        } catch (EntityNotFoundException ex) {
            log.error("Vendor not found with id: {}", id, ex);
            model.addAttribute("errorMessage", "Vendor not found.");
            return "redirect:/vendors";
        } catch (Exception ex) {
            log.error("Error loading vendor details for id: {}", id, ex);
            model.addAttribute("errorMessage", "Error loading vendor details. Please try again.");
            return "redirect:/vendors";
        }
    }

    /**
     * Display new vendor form
     */
    @GetMapping("/new")
    public String newVendorForm(Model model) {
        model.addAttribute("vendorDTO", new VendorDTO());
        model.addAttribute("pageTitle", "Add New Vendor");
        model.addAttribute("isEdit", false);
        addFormData(model);
        return "vendors/form";
    }

    /**
     * Display edit vendor form
     */
    @GetMapping("/edit/{id}")
    public String editVendorForm(@PathVariable Long id, Model model) {
        try {
            VendorDTO vendor = vendorService.getVendorById(id);
            model.addAttribute("vendorDTO", vendor);
            model.addAttribute("pageTitle", "Edit Vendor");
            model.addAttribute("isEdit", true);
            addFormData(model);
            return "vendors/form";
        } catch (EntityNotFoundException ex) {
            log.error("Vendor not found with id: {}", id, ex);
            model.addAttribute("errorMessage", "Vendor not found.");
            return "redirect:/vendors";
        } catch (Exception ex) {
            log.error("Error loading vendor for edit with id: {}", id, ex);
            model.addAttribute("errorMessage", "Error loading vendor for editing. Please try again.");
            return "redirect:/vendors";
        }
    }

    /**
     * Handle new vendor form submission
     */
    @PostMapping("/new")
    public String createVendor(@Valid @ModelAttribute VendorDTO vendorDTO,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Add New Vendor");
            model.addAttribute("isEdit", false);
            addFormData(model);
            return "vendors/form";
        }

        try {
            VendorDTO createdVendor = vendorService.createVendor(vendorDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                "Vendor '" + createdVendor.getCompanyName() + "' has been created successfully.");
            return "redirect:/vendors/view/" + createdVendor.getId();
        } catch (IllegalArgumentException ex) {
            log.error("Validation error creating vendor", ex);
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("pageTitle", "Add New Vendor");
            model.addAttribute("isEdit", false);
            addFormData(model);
            return "vendors/form";
        } catch (Exception ex) {
            log.error("Error creating vendor", ex);
            model.addAttribute("errorMessage", "Error creating vendor. Please try again.");
            model.addAttribute("pageTitle", "Add New Vendor");
            model.addAttribute("isEdit", false);
            addFormData(model);
            return "vendors/form";
        }
    }

    /**
     * Handle edit vendor form submission
     */
    @PostMapping("/edit/{id}")
    public String updateVendor(@PathVariable Long id,
                              @Valid @ModelAttribute VendorDTO vendorDTO,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Vendor");
            model.addAttribute("isEdit", true);
            addFormData(model);
            return "vendors/form";
        }

        try {
            VendorDTO updatedVendor = vendorService.updateVendor(id, vendorDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                "Vendor '" + updatedVendor.getCompanyName() + "' has been updated successfully.");
            return "redirect:/vendors/view/" + id;
        } catch (EntityNotFoundException ex) {
            log.error("Vendor not found with id: {}", id, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Vendor not found.");
            return "redirect:/vendors";
        } catch (IllegalArgumentException ex) {
            log.error("Validation error updating vendor", ex);
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("pageTitle", "Edit Vendor");
            model.addAttribute("isEdit", true);
            addFormData(model);
            return "vendors/form";
        } catch (Exception ex) {
            log.error("Error updating vendor with id: {}", id, ex);
            model.addAttribute("errorMessage", "Error updating vendor. Please try again.");
            model.addAttribute("pageTitle", "Edit Vendor");
            model.addAttribute("isEdit", true);
            addFormData(model);
            return "vendors/form";
        }
    }

    /**
     * Handle vendor deletion (deactivation)
     */
    @PostMapping("/delete/{id}")
    public String deleteVendor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            VendorDTO vendor = vendorService.getVendorById(id);
            vendorService.deleteVendor(id);
            redirectAttributes.addFlashAttribute("successMessage",
                "Vendor '" + vendor.getCompanyName() + "' has been deactivated successfully.");
        } catch (EntityNotFoundException ex) {
            log.error("Vendor not found with id: {}", id, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Vendor not found.");
        } catch (Exception ex) {
            log.error("Error deleting vendor with id: {}", id, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Error deactivating vendor. Please try again.");
        }
        return "redirect:/vendors";
    }

    /**
     * AJAX endpoint to check company name availability
     */
    @GetMapping("/check-company-name")
    @ResponseBody
    public ResponseEntity<Boolean> checkCompanyNameAvailability(
            @RequestParam String companyName,
            @RequestParam(required = false) Long vendorId) {
        try {
            boolean available = vendorService.isCompanyNameAvailable(companyName, vendorId);
            return ResponseEntity.ok(available);
        } catch (Exception ex) {
            log.error("Error checking company name availability", ex);
            return ResponseEntity.ok(false);
        }
    }

    /**
     * Vendor personnel management page
     */
    @GetMapping("/{id}/personnel")
    public String managePersonnel(@PathVariable Long id,
                                 @RequestParam(value = "search", required = false) String search,
                                 Model model) {
        try {
            VendorDTO vendor = vendorService.getVendorById(id);
            List<VendorPersonnelDTO> personnel = vendorPersonnelService.getVendorPersonnel(id, search);
            long personnelCount = vendorPersonnelService.getPersonnelCount(id, false);

            model.addAttribute("vendor", vendor);
            model.addAttribute("personnel", personnel);
            model.addAttribute("personnelCount", personnelCount);
            model.addAttribute("search", search);

            return "vendors/personnel/list";
        } catch (EntityNotFoundException ex) {
            log.error("Vendor not found with id: {}", id, ex);
            model.addAttribute("errorMessage", "Vendor not found.");
            return "redirect:/vendors";
        } catch (Exception ex) {
            log.error("Error loading vendor personnel for vendor id: {}", id, ex);
            model.addAttribute("errorMessage", "Error loading vendor personnel. Please try again.");
            return "redirect:/vendors";
        }
    }

    /**
     * Add common form data to model
     */
    private void addFormData(Model model) {
        // Add common dropdown data
        model.addAttribute("countries", getCountries());
        model.addAttribute("states", getStates());
        model.addAttribute("cities", getCities());
    }

    /**
     * Get list of countries for dropdown
     */
    private List<String> getCountries() {
        return Arrays.asList(
            "India", "United States", "United Kingdom", "Canada", "Australia",
            "Germany", "France", "Japan", "China", "Singapore", "UAE"
        );
    }

    /**
     * Get list of states for dropdown
     */
    private List<String> getStates() {
        return Arrays.asList(
            "Gujarat", "Maharashtra", "Karnataka", "Tamil Nadu", "Delhi",
            "Rajasthan", "Punjab", "Haryana", "Uttar Pradesh", "West Bengal"
        );
    }

    /**
     * Get list of cities for dropdown
     */
    private List<String> getCities() {
        return Arrays.asList(
            "Ahmedabad", "Mumbai", "Bangalore", "Chennai", "Delhi",
            "Pune", "Hyderabad", "Kolkata", "Jaipur", "Surat"
        );
    }
}
