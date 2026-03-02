package org.zeus.ims.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zeus.ims.dto.CustomerDTO;
import org.zeus.ims.dto.CustomerPersonnelDTO;
import org.zeus.ims.entity.Customer;
import org.zeus.ims.entity.CustomerPersonnel;
import org.zeus.ims.repository.CustomerPersonnelRepository;
import org.zeus.ims.repository.CustomerRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerPersonnelRepository customerPersonnelRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository, CustomerPersonnelRepository customerPersonnelRepository) {
        this.customerRepository = customerRepository;
        this.customerPersonnelRepository = customerPersonnelRepository;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public List<Customer> getAllActiveCustomers() {
        return customerRepository.findByActiveTrueOrderByCompanyNameAsc();
    }

    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public Optional<Customer> getCustomerByCompanyName(String companyName) {
        return customerRepository.findByCompanyNameAndActiveTrue(companyName);
    }

    public List<Customer> searchCustomers(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllActiveCustomers();
        }
        return customerRepository.findActiveCustomersBySearch(searchTerm.trim());
    }

    public List<Customer> searchCustomersByLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            return getAllActiveCustomers();
        }
        return customerRepository.findActiveCustomersByLocation(location.trim());
    }

    public Customer createCustomer(CustomerDTO customerDTO) {
        validateCustomerDTO(customerDTO, true);

        Customer customer = new Customer();
        mapDtoToEntity(customerDTO, customer);
        customer.setCreatedBy(getCurrentUsername());

        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Long id, CustomerDTO customerDTO) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));

        validateCustomerDTO(customerDTO, false);

        if (!existingCustomer.getCompanyName().equals(customerDTO.getCompanyName()) &&
            customerRepository.existsByCompanyNameAndActiveTrue(customerDTO.getCompanyName())) {
            throw new RuntimeException("Company name already exists");
        }

        mapDtoToEntity(customerDTO, existingCustomer);
        existingCustomer.setUpdatedBy(getCurrentUsername());

        return customerRepository.save(existingCustomer);
    }

    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));

        customer.setActive(false);
        customer.setUpdatedBy(getCurrentUsername());

        // Also deactivate all personnel
        List<CustomerPersonnel> personnel = customerPersonnelRepository.findByCustomerAndActiveTrue(customer);
        personnel.forEach(person -> {
            person.setActive(false);
            person.setUpdatedBy(getCurrentUsername());
        });
        customerPersonnelRepository.saveAll(personnel);

        customerRepository.save(customer);
    }

    public boolean isCompanyNameAvailable(String companyName) {
        return !customerRepository.existsByCompanyNameAndActiveTrue(companyName);
    }

    public CustomerDTO convertToDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setCompanyName(customer.getCompanyName());
        dto.setAddressLine1(customer.getAddressLine1());
        dto.setAddressLine2(customer.getAddressLine2());
        dto.setCity(customer.getCity());
        dto.setState(customer.getState());
        dto.setPostalCode(customer.getPostalCode());
        dto.setCountry(customer.getCountry());
        dto.setContactNumber(customer.getContactNumber());
        dto.setSecondaryContact(customer.getSecondaryContact());
        dto.setEmail(customer.getEmail());
        dto.setWebsiteUrl(customer.getWebsiteUrl());
        dto.setDescription(customer.getDescription());
        dto.setActive(customer.getActive());
        return dto;
    }

    public long getActiveCustomerCount() {
        return customerRepository.countActiveCustomers();
    }

    public List<String> getDistinctCities() {
        return customerRepository.findDistinctCities();
    }

    public List<String> getDistinctStates() {
        return customerRepository.findDistinctStates();
    }

    public List<String> getDistinctCountries() {
        return customerRepository.findDistinctCountries();
    }

    // Customer Personnel Methods
    public List<CustomerPersonnel> getPersonnelByCustomer(Long customerId) {
        return customerPersonnelRepository.findActivePersonnelByCustomerOrderByPrimaryContactFirst(customerId);
    }

    public Optional<CustomerPersonnel> getPersonnelById(Long id) {
        return customerPersonnelRepository.findById(id);
    }

    public Optional<CustomerPersonnel> getPrimaryContactByCustomer(Long customerId) {
        return customerPersonnelRepository.findByCustomerIdAndIsPrimaryContactTrueAndActiveTrue(customerId);
    }

    public CustomerPersonnel createPersonnel(CustomerPersonnelDTO personnelDTO) {
        validatePersonnelDTO(personnelDTO, true);

        Customer customer = customerRepository.findById(personnelDTO.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + personnelDTO.getCustomerId()));

        CustomerPersonnel personnel = new CustomerPersonnel();
        mapPersonnelDtoToEntity(personnelDTO, personnel);
        personnel.setCustomer(customer);
        personnel.setCreatedBy(getCurrentUsername());

        // If this is set as primary contact, unset previous primary contact
        if (Boolean.TRUE.equals(personnelDTO.getIsPrimaryContact())) {
            unsetPreviousPrimaryContact(customer);
        }

        return customerPersonnelRepository.save(personnel);
    }

    public CustomerPersonnel updatePersonnel(Long id, CustomerPersonnelDTO personnelDTO) {
        CustomerPersonnel existingPersonnel = customerPersonnelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Personnel not found with id: " + id));

        validatePersonnelDTO(personnelDTO, false);

        if (personnelDTO.getEmail() != null && !personnelDTO.getEmail().trim().isEmpty() &&
            !existingPersonnel.getEmail().equals(personnelDTO.getEmail()) &&
            customerPersonnelRepository.existsByCustomerAndEmailAndActiveTrue(existingPersonnel.getCustomer(), personnelDTO.getEmail())) {
            throw new RuntimeException("Email already exists for this customer");
        }

        mapPersonnelDtoToEntity(personnelDTO, existingPersonnel);
        existingPersonnel.setUpdatedBy(getCurrentUsername());

        // If this is set as primary contact, unset previous primary contact
        if (Boolean.TRUE.equals(personnelDTO.getIsPrimaryContact()) && !Boolean.TRUE.equals(existingPersonnel.getIsPrimaryContact())) {
            unsetPreviousPrimaryContact(existingPersonnel.getCustomer());
        }

        return customerPersonnelRepository.save(existingPersonnel);
    }

    public void deletePersonnel(Long id) {
        CustomerPersonnel personnel = customerPersonnelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Personnel not found with id: " + id));

        personnel.setActive(false);
        personnel.setUpdatedBy(getCurrentUsername());
        customerPersonnelRepository.save(personnel);
    }

    public List<CustomerPersonnel> searchPersonnelByCustomer(Long customerId, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getPersonnelByCustomer(customerId);
        }
        return customerPersonnelRepository.findActivePersonnelByCustomerAndSearch(customerId, searchTerm.trim());
    }

    public List<CustomerPersonnelDTO> getPersonnelByCustomerId(Long customerId) {
        List<CustomerPersonnel> personnel = customerPersonnelRepository.findByCustomerIdAndActiveTrue(customerId);
        return personnel.stream()
                .map(this::convertPersonnelToDTO)
                .collect(Collectors.toList());
    }

    public CustomerPersonnelDTO convertPersonnelToDTO(CustomerPersonnel personnel) {
        CustomerPersonnelDTO dto = new CustomerPersonnelDTO();
        dto.setId(personnel.getId());
        dto.setCustomerId(personnel.getCustomer().getId());
        dto.setFullName(personnel.getFullName());
        dto.setEmail(personnel.getEmail());
        dto.setContactNumber(personnel.getContactNumber());
        dto.setSecondaryContact(personnel.getSecondaryContact());
        dto.setDepartment(personnel.getDepartment());
        dto.setDesignation(personnel.getDesignation());
        dto.setNotes(personnel.getNotes());
        dto.setActive(personnel.getActive());
        dto.setIsPrimaryContact(personnel.getIsPrimaryContact());
        return dto;
    }

    public long getPersonnelCountByCustomer(Long customerId) {
        return customerPersonnelRepository.countActivePersonnelByCustomer(customerId);
    }

    public List<String> getDistinctDepartments() {
        return customerPersonnelRepository.findDistinctDepartments();
    }

    public List<String> getDistinctDesignations() {
        return customerPersonnelRepository.findDistinctDesignations();
    }

    // Private helper methods
    private void validateCustomerDTO(CustomerDTO customerDTO, boolean isNewCustomer) {
        if (customerDTO.getCompanyName() == null || customerDTO.getCompanyName().trim().isEmpty()) {
            throw new RuntimeException("Company name is required");
        }

        if (isNewCustomer && customerRepository.existsByCompanyNameAndActiveTrue(customerDTO.getCompanyName())) {
            throw new RuntimeException("Company name already exists");
        }
    }

    private void validatePersonnelDTO(CustomerPersonnelDTO personnelDTO, boolean isNewPersonnel) {
        if (personnelDTO.getFullName() == null || personnelDTO.getFullName().trim().isEmpty()) {
            throw new RuntimeException("Full name is required");
        }

        if (personnelDTO.getCustomerId() == null) {
            throw new RuntimeException("Customer ID is required");
        }

        if (isNewPersonnel && personnelDTO.getEmail() != null && !personnelDTO.getEmail().trim().isEmpty() &&
            customerPersonnelRepository.existsByCustomerIdAndEmailAndActiveTrue(personnelDTO.getCustomerId(), personnelDTO.getEmail())) {
            throw new RuntimeException("Email already exists for this customer");
        }
    }

    private void mapDtoToEntity(CustomerDTO dto, Customer entity) {
        entity.setCompanyName(dto.getCompanyName());
        entity.setAddressLine1(dto.getAddressLine1());
        entity.setAddressLine2(dto.getAddressLine2());
        entity.setCity(dto.getCity());
        entity.setState(dto.getState());
        entity.setPostalCode(dto.getPostalCode());
        entity.setCountry(dto.getCountry());
        entity.setContactNumber(dto.getContactNumber());
        entity.setSecondaryContact(dto.getSecondaryContact());
        entity.setEmail(dto.getEmail());
        entity.setWebsiteUrl(dto.getWebsiteUrl());
        entity.setDescription(dto.getDescription());
        entity.setActive(dto.getActive());
    }

    private void mapPersonnelDtoToEntity(CustomerPersonnelDTO dto, CustomerPersonnel entity) {
        entity.setFullName(dto.getFullName());
        entity.setEmail(dto.getEmail());
        entity.setContactNumber(dto.getContactNumber());
        entity.setSecondaryContact(dto.getSecondaryContact());
        entity.setDepartment(dto.getDepartment());
        entity.setDesignation(dto.getDesignation());
        entity.setNotes(dto.getNotes());
        entity.setActive(dto.getActive());
        entity.setIsPrimaryContact(dto.getIsPrimaryContact());
    }

    private void unsetPreviousPrimaryContact(Customer customer) {
        Optional<CustomerPersonnel> existingPrimary = customerPersonnelRepository.findByCustomerAndIsPrimaryContactTrueAndActiveTrue(customer);
        if (existingPrimary.isPresent()) {
            CustomerPersonnel primary = existingPrimary.get();
            primary.setIsPrimaryContact(false);
            primary.setUpdatedBy(getCurrentUsername());
            customerPersonnelRepository.save(primary);
        }
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }
}
