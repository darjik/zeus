package org.zeus.ims.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.zeus.ims.dto.CustomerPersonnelDTO;
import org.zeus.ims.entity.Customer;
import org.zeus.ims.entity.CustomerPersonnel;
import org.zeus.ims.service.CustomerService;
import org.zeus.ims.validation.CreateCustomer;
import org.zeus.ims.validation.UpdateCustomer;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/customers/{customerId}/personnel")
@PreAuthorize("hasAnyAuthority('OWNER', 'SALES')")
public class CustomerPersonnelController {

    private final CustomerService customerService;

    @Autowired
    public CustomerPersonnelController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public String listPersonnel(@PathVariable Long customerId,
                               @RequestParam(value = "search", required = false) String search,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        Optional<Customer> customerOpt = customerService.getCustomerById(customerId);
        if (!customerOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Customer not found!");
            return "redirect:/customers";
        }

        Customer customer = customerOpt.get();
        List<CustomerPersonnel> personnel;

        if (search != null && !search.trim().isEmpty()) {
            personnel = customerService.searchPersonnelByCustomer(customerId, search);
            model.addAttribute("search", search);
        } else {
            personnel = customerService.getPersonnelByCustomer(customerId);
        }

        model.addAttribute("customer", customer);
        model.addAttribute("personnel", personnel);
        model.addAttribute("personnelCount", customerService.getPersonnelCountByCustomer(customerId));
        model.addAttribute("primaryContact", customerService.getPrimaryContactByCustomer(customerId).orElse(null));
        return "customers/personnel/list";
    }

    @GetMapping("/new")
    public String showCreatePersonnelForm(@PathVariable Long customerId, Model model, RedirectAttributes redirectAttributes) {
        Optional<Customer> customerOpt = customerService.getCustomerById(customerId);
        if (!customerOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Customer not found!");
            return "redirect:/customers";
        }

        CustomerPersonnelDTO personnelDTO = new CustomerPersonnelDTO();
        personnelDTO.setCustomerId(customerId);

        model.addAttribute("customer", customerOpt.get());
        model.addAttribute("personnelDTO", personnelDTO);
        model.addAttribute("pageTitle", "Add New Personnel");
        model.addAttribute("isEdit", false);
        model.addAttribute("departments", customerService.getDistinctDepartments());
        model.addAttribute("designations", customerService.getDistinctDesignations());
        return "customers/personnel/form";
    }

    @PostMapping("/new")
    public String createPersonnel(@PathVariable Long customerId,
                                 @Validated(CreateCustomer.class) @ModelAttribute("personnelDTO") CustomerPersonnelDTO personnelDTO,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {

        Optional<Customer> customerOpt = customerService.getCustomerById(customerId);
        if (!customerOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Customer not found!");
            return "redirect:/customers";
        }

        personnelDTO.setCustomerId(customerId);

        if (result.hasErrors()) {
            model.addAttribute("customer", customerOpt.get());
            model.addAttribute("pageTitle", "Add New Personnel");
            model.addAttribute("isEdit", false);
            model.addAttribute("departments", customerService.getDistinctDepartments());
            model.addAttribute("designations", customerService.getDistinctDesignations());
            return "customers/personnel/form";
        }

        try {
            CustomerPersonnel createdPersonnel = customerService.createPersonnel(personnelDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                "Personnel '" + createdPersonnel.getFullName() + "' added successfully!");
            return "redirect:/customers/" + customerId + "/personnel";
        } catch (RuntimeException ex) {
            model.addAttribute("customer", customerOpt.get());
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("pageTitle", "Add New Personnel");
            model.addAttribute("isEdit", false);
            model.addAttribute("departments", customerService.getDistinctDepartments());
            model.addAttribute("designations", customerService.getDistinctDesignations());
            return "customers/personnel/form";
        }
    }

    @GetMapping("/edit/{personnelId}")
    public String showEditPersonnelForm(@PathVariable Long customerId,
                                       @PathVariable Long personnelId,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {

        Optional<Customer> customerOpt = customerService.getCustomerById(customerId);
        if (!customerOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Customer not found!");
            return "redirect:/customers";
        }

        Optional<CustomerPersonnel> personnelOpt = customerService.getPersonnelById(personnelId);
        if (!personnelOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Personnel not found!");
            return "redirect:/customers/" + customerId + "/personnel";
        }

        CustomerPersonnel personnel = personnelOpt.get();
        CustomerPersonnelDTO personnelDTO = customerService.convertPersonnelToDTO(personnel);

        model.addAttribute("customer", customerOpt.get());
        model.addAttribute("personnelDTO", personnelDTO);
        model.addAttribute("pageTitle", "Edit Personnel");
        model.addAttribute("isEdit", true);
        model.addAttribute("departments", customerService.getDistinctDepartments());
        model.addAttribute("designations", customerService.getDistinctDesignations());
        return "customers/personnel/form";
    }

    @PostMapping("/edit/{personnelId}")
    public String updatePersonnel(@PathVariable Long customerId,
                                 @PathVariable Long personnelId,
                                 @Validated(UpdateCustomer.class) @ModelAttribute("personnelDTO") CustomerPersonnelDTO personnelDTO,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {

        Optional<Customer> customerOpt = customerService.getCustomerById(customerId);
        if (!customerOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Customer not found!");
            return "redirect:/customers";
        }

        personnelDTO.setCustomerId(customerId);

        if (result.hasErrors()) {
            model.addAttribute("customer", customerOpt.get());
            model.addAttribute("pageTitle", "Edit Personnel");
            model.addAttribute("isEdit", true);
            model.addAttribute("departments", customerService.getDistinctDepartments());
            model.addAttribute("designations", customerService.getDistinctDesignations());
            return "customers/personnel/form";
        }

        try {
            CustomerPersonnel updatedPersonnel = customerService.updatePersonnel(personnelId, personnelDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                "Personnel '" + updatedPersonnel.getFullName() + "' updated successfully!");
            return "redirect:/customers/" + customerId + "/personnel";
        } catch (RuntimeException ex) {
            model.addAttribute("customer", customerOpt.get());
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("pageTitle", "Edit Personnel");
            model.addAttribute("isEdit", true);
            model.addAttribute("departments", customerService.getDistinctDepartments());
            model.addAttribute("designations", customerService.getDistinctDesignations());
            return "customers/personnel/form";
        }
    }

    @GetMapping("/view/{personnelId}")
    public String viewPersonnel(@PathVariable Long customerId,
                               @PathVariable Long personnelId,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        Optional<Customer> customerOpt = customerService.getCustomerById(customerId);
        if (!customerOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Customer not found!");
            return "redirect:/customers";
        }

        Optional<CustomerPersonnel> personnelOpt = customerService.getPersonnelById(personnelId);
        if (!personnelOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Personnel not found!");
            return "redirect:/customers/" + customerId + "/personnel";
        }

        model.addAttribute("customer", customerOpt.get());
        model.addAttribute("personnel", personnelOpt.get());
        return "customers/personnel/view";
    }

    @PostMapping("/delete/{personnelId}")
    public String deletePersonnel(@PathVariable Long customerId,
                                 @PathVariable Long personnelId,
                                 RedirectAttributes redirectAttributes) {
        try {
            Optional<CustomerPersonnel> personnelOpt = customerService.getPersonnelById(personnelId);
            if (!personnelOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Personnel not found!");
                return "redirect:/customers/" + customerId + "/personnel";
            }

            String fullName = personnelOpt.get().getFullName();
            customerService.deletePersonnel(personnelId);
            redirectAttributes.addFlashAttribute("successMessage",
                "Personnel '" + fullName + "' deactivated successfully!");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/customers/" + customerId + "/personnel";
    }
}
