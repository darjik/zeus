package org.zeus.ims.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.zeus.ims.dto.CustomerDTO;
import org.zeus.ims.dto.CustomerPersonnelDTO;
import org.zeus.ims.entity.Customer;
import org.zeus.ims.service.CustomerService;
import org.zeus.ims.validation.CreateCustomer;
import org.zeus.ims.validation.UpdateCustomer;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/customers")
@PreAuthorize("hasAnyAuthority('OWNER', 'SALES')")
public class CustomerController {

    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public String listCustomers(@RequestParam(value = "search", required = false) String search, Model model) {
        List<Customer> customers;
        if (search != null && !search.trim().isEmpty()) {
            customers = customerService.searchCustomers(search);
            model.addAttribute("search", search);
        } else {
            customers = customerService.getAllActiveCustomers();
        }

        model.addAttribute("customers", customers);
        model.addAttribute("totalCustomers", customerService.getActiveCustomerCount());
        return "customers/list";
    }

    @GetMapping("/new")
    public String showCreateCustomerForm(Model model) {
        model.addAttribute("customerDTO", new CustomerDTO());
        model.addAttribute("pageTitle", "Add New Customer");
        model.addAttribute("isEdit", false);
        model.addAttribute("countries", customerService.getDistinctCountries());
        model.addAttribute("states", customerService.getDistinctStates());
        model.addAttribute("cities", customerService.getDistinctCities());
        return "customers/form";
    }

    @PostMapping("/new")
    public String createCustomer(@Validated(CreateCustomer.class) @ModelAttribute("customerDTO") CustomerDTO customerDTO,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Add New Customer");
            model.addAttribute("isEdit", false);
            model.addAttribute("countries", customerService.getDistinctCountries());
            model.addAttribute("states", customerService.getDistinctStates());
            model.addAttribute("cities", customerService.getDistinctCities());
            return "customers/form";
        }

        try {
            Customer createdCustomer = customerService.createCustomer(customerDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                "Customer '" + createdCustomer.getCompanyName() + "' created successfully!");
            return "redirect:/customers";
        } catch (RuntimeException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("pageTitle", "Add New Customer");
            model.addAttribute("isEdit", false);
            model.addAttribute("countries", customerService.getDistinctCountries());
            model.addAttribute("states", customerService.getDistinctStates());
            model.addAttribute("cities", customerService.getDistinctCities());
            return "customers/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditCustomerForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Customer> customerOpt = customerService.getCustomerById(id);
        if (!customerOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Customer not found!");
            return "redirect:/customers";
        }

        Customer customer = customerOpt.get();
        CustomerDTO customerDTO = customerService.convertToDTO(customer);

        model.addAttribute("customerDTO", customerDTO);
        model.addAttribute("pageTitle", "Edit Customer");
        model.addAttribute("isEdit", true);
        model.addAttribute("countries", customerService.getDistinctCountries());
        model.addAttribute("states", customerService.getDistinctStates());
        model.addAttribute("cities", customerService.getDistinctCities());
        return "customers/form";
    }

    @PostMapping("/edit/{id}")
    public String updateCustomer(@PathVariable Long id,
                                @Validated(UpdateCustomer.class) @ModelAttribute("customerDTO") CustomerDTO customerDTO,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Customer");
            model.addAttribute("isEdit", true);
            model.addAttribute("countries", customerService.getDistinctCountries());
            model.addAttribute("states", customerService.getDistinctStates());
            model.addAttribute("cities", customerService.getDistinctCities());
            return "customers/form";
        }

        try {
            Customer updatedCustomer = customerService.updateCustomer(id, customerDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                "Customer '" + updatedCustomer.getCompanyName() + "' updated successfully!");
            return "redirect:/customers";
        } catch (RuntimeException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("pageTitle", "Edit Customer");
            model.addAttribute("isEdit", true);
            model.addAttribute("countries", customerService.getDistinctCountries());
            model.addAttribute("states", customerService.getDistinctStates());
            model.addAttribute("cities", customerService.getDistinctCities());
            return "customers/form";
        }
    }

    @GetMapping("/view/{id}")
    public String viewCustomer(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Customer> customerOpt = customerService.getCustomerById(id);
        if (!customerOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Customer not found!");
            return "redirect:/customers";
        }

        Customer customer = customerOpt.get();
        model.addAttribute("customer", customer);
        model.addAttribute("personnel", customerService.getPersonnelByCustomer(id));
        model.addAttribute("personnelCount", customerService.getPersonnelCountByCustomer(id));
        model.addAttribute("primaryContact", customerService.getPrimaryContactByCustomer(id).orElse(null));
        return "customers/view";
    }

    @PostMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Customer> customerOpt = customerService.getCustomerById(id);
            if (!customerOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Customer not found!");
                return "redirect:/customers";
            }

            String companyName = customerOpt.get().getCompanyName();
            customerService.deleteCustomer(id);
            redirectAttributes.addFlashAttribute("successMessage",
                "Customer '" + companyName + "' deactivated successfully!");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/customers";
    }

    @ResponseBody
    @GetMapping("/check-company-name")
    public boolean checkCompanyNameAvailability(@RequestParam String companyName,
                                               @RequestParam(required = false) Long customerId) {
        if (customerId != null) {
            Optional<Customer> customerOpt = customerService.getCustomerById(customerId);
            if (customerOpt.isPresent() && customerOpt.get().getCompanyName().equals(companyName)) {
                return true;
            }
        }
        return customerService.isCompanyNameAvailable(companyName);
    }

    @GetMapping("/location")
    public String searchByLocation(@RequestParam(value = "location", required = false) String location, Model model) {
        List<Customer> customers;
        if (location != null && !location.trim().isEmpty()) {
            customers = customerService.searchCustomersByLocation(location);
            model.addAttribute("location", location);
        } else {
            customers = customerService.getAllActiveCustomers();
        }

        model.addAttribute("customers", customers);
        model.addAttribute("totalCustomers", customerService.getActiveCustomerCount());
        model.addAttribute("countries", customerService.getDistinctCountries());
        model.addAttribute("states", customerService.getDistinctStates());
        model.addAttribute("cities", customerService.getDistinctCities());
        return "customers/location";
    }

    /**
     * API endpoint to get customer personnel for AJAX calls
     */
    @GetMapping("/{customerId}/personnel/api")
    @ResponseBody
    public ResponseEntity<List<CustomerPersonnelDTO>> getCustomerPersonnel(@PathVariable Long customerId) {
        try {
            List<CustomerPersonnelDTO> personnel = customerService.getPersonnelByCustomerId(customerId);
            return ResponseEntity.ok(personnel);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }
}
