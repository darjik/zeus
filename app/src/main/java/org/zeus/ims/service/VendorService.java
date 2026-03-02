package org.zeus.ims.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zeus.ims.dto.VendorDTO;
import org.zeus.ims.dto.VendorPersonnelDTO;
import org.zeus.ims.entity.Vendor;
import org.zeus.ims.entity.VendorPersonnel;
import org.zeus.ims.repository.VendorPersonnelRepository;
import org.zeus.ims.repository.VendorRepository;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VendorService {

    private final VendorRepository vendorRepository;
    private final VendorPersonnelRepository vendorPersonnelRepository;

    /**
     * Get all vendors with optional search functionality
     */
    @Transactional(readOnly = true)
    public List<VendorDTO> getAllVendors(String search) {
        List<Vendor> vendors;

        if (search != null && !search.trim().isEmpty()) {
            vendors = vendorRepository.findBySearchTerm(search.trim());
        } else {
            vendors = vendorRepository.findAllByOrderByCompanyName();
        }

        return vendors.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get only active vendors
     */
    @Transactional(readOnly = true)
    public List<VendorDTO> getActiveVendors() {
        return vendorRepository.findByActiveTrueOrderByCompanyName().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get vendor by ID
     */
    @Transactional(readOnly = true)
    public VendorDTO getVendorById(Long id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vendor not found with id: " + id));
        return convertToDTO(vendor);
    }

    /**
     * Create a new vendor
     */
    public VendorDTO createVendor(VendorDTO vendorDTO) {
        log.info("Creating new vendor: {}", vendorDTO.getCompanyName());

        // Check if company name already exists
        if (vendorRepository.existsByCompanyNameIgnoreCase(vendorDTO.getCompanyName())) {
            throw new IllegalArgumentException("A vendor with this company name already exists");
        }

        Vendor vendor = convertToEntity(vendorDTO);
        vendor = vendorRepository.save(vendor);

        log.info("Created vendor with id: {}", vendor.getId());
        return convertToDTO(vendor);
    }

    /**
     * Update an existing vendor
     */
    public VendorDTO updateVendor(Long id, VendorDTO vendorDTO) {
        log.info("Updating vendor with id: {}", id);

        Vendor existingVendor = vendorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vendor not found with id: " + id));

        // Check if company name already exists for another vendor
        if (!existingVendor.getCompanyName().equalsIgnoreCase(vendorDTO.getCompanyName()) &&
            vendorRepository.existsByCompanyNameIgnoreCaseAndIdNot(vendorDTO.getCompanyName(), id)) {
            throw new IllegalArgumentException("A vendor with this company name already exists");
        }

        updateVendorFields(existingVendor, vendorDTO);
        existingVendor = vendorRepository.save(existingVendor);

        log.info("Updated vendor: {}", existingVendor.getCompanyName());
        return convertToDTO(existingVendor);
    }

    /**
     * Delete (deactivate) a vendor
     */
    public void deleteVendor(Long id) {
        log.info("Deactivating vendor with id: {}", id);

        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vendor not found with id: " + id));

        vendor.setActive(false);

        // Also deactivate all personnel
        List<VendorPersonnel> personnel = vendorPersonnelRepository.findByVendorIdOrderByFullName(id);
        personnel.forEach(person -> person.setActive(false));
        vendorPersonnelRepository.saveAll(personnel);

        vendorRepository.save(vendor);
        log.info("Deactivated vendor: {}", vendor.getCompanyName());
    }

    /**
     * Check if company name is available
     */
    @Transactional(readOnly = true)
    public boolean isCompanyNameAvailable(String companyName, Long excludeVendorId) {
        if (excludeVendorId != null) {
            return !vendorRepository.existsByCompanyNameIgnoreCaseAndIdNot(companyName, excludeVendorId);
        }
        return !vendorRepository.existsByCompanyNameIgnoreCase(companyName);
    }

    /**
     * Get vendor statistics
     */
    @Transactional(readOnly = true)
    public long getTotalVendorCount() {
        return vendorRepository.count();
    }

    @Transactional(readOnly = true)
    public long getActiveVendorCount() {
        return vendorRepository.countByActiveTrue();
    }

    /**
     * Convert entity to DTO
     */
    private VendorDTO convertToDTO(Vendor vendor) {
        VendorPersonnel primaryContact = vendor.getPrimaryContact();

        return VendorDTO.builder()
                .id(vendor.getId())
                .companyName(vendor.getCompanyName())
                .description(vendor.getDescription())
                .websiteUrl(vendor.getWebsiteUrl())
                .addressLine1(vendor.getAddressLine1())
                .addressLine2(vendor.getAddressLine2())
                .city(vendor.getCity())
                .state(vendor.getState())
                .postalCode(vendor.getPostalCode())
                .country(vendor.getCountry())
                .contactNumber(vendor.getContactNumber())
                .secondaryContact(vendor.getSecondaryContact())
                .email(vendor.getEmail())
                .active(vendor.getActive())
                .fullAddress(vendor.getFullAddress())
                .personnelCount(vendor.getActivePersonnelCount())
                .primaryContact(primaryContact != null ? convertPersonnelToDTO(primaryContact) : null)
                .createdAt(vendor.getCreatedAt())
                .updatedAt(vendor.getUpdatedAt())
                .createdBy(vendor.getCreatedBy())
                .updatedBy(vendor.getUpdatedBy())
                .build();
    }

    /**
     * Convert DTO to entity
     */
    private Vendor convertToEntity(VendorDTO vendorDTO) {
        return Vendor.builder()
                .companyName(vendorDTO.getCompanyName())
                .description(vendorDTO.getDescription())
                .websiteUrl(vendorDTO.getWebsiteUrl())
                .addressLine1(vendorDTO.getAddressLine1())
                .addressLine2(vendorDTO.getAddressLine2())
                .city(vendorDTO.getCity())
                .state(vendorDTO.getState())
                .postalCode(vendorDTO.getPostalCode())
                .country(vendorDTO.getCountry())
                .contactNumber(vendorDTO.getContactNumber())
                .secondaryContact(vendorDTO.getSecondaryContact())
                .email(vendorDTO.getEmail())
                .active(vendorDTO.getActive())
                .build();
    }

    /**
     * Update vendor fields from DTO
     */
    private void updateVendorFields(Vendor vendor, VendorDTO vendorDTO) {
        vendor.setCompanyName(vendorDTO.getCompanyName());
        vendor.setDescription(vendorDTO.getDescription());
        vendor.setWebsiteUrl(vendorDTO.getWebsiteUrl());
        vendor.setAddressLine1(vendorDTO.getAddressLine1());
        vendor.setAddressLine2(vendorDTO.getAddressLine2());
        vendor.setCity(vendorDTO.getCity());
        vendor.setState(vendorDTO.getState());
        vendor.setPostalCode(vendorDTO.getPostalCode());
        vendor.setCountry(vendorDTO.getCountry());
        vendor.setContactNumber(vendorDTO.getContactNumber());
        vendor.setSecondaryContact(vendorDTO.getSecondaryContact());
        vendor.setEmail(vendorDTO.getEmail());
        vendor.setActive(vendorDTO.getActive());
    }

    /**
     * Convert VendorPersonnel entity to DTO
     */
    private VendorPersonnelDTO convertPersonnelToDTO(VendorPersonnel personnel) {
        return VendorPersonnelDTO.builder()
                .id(personnel.getId())
                .fullName(personnel.getFullName())
                .designation(personnel.getDesignation())
                .department(personnel.getDepartment())
                .email(personnel.getEmail())
                .contactNumber(personnel.getContactNumber())
                .secondaryContact(personnel.getSecondaryContact())
                .isPrimaryContact(personnel.getIsPrimaryContact())
                .active(personnel.getActive())
                .notes(personnel.getNotes())
                .vendorId(personnel.getVendor().getId())
                .vendorCompanyName(personnel.getVendor().getCompanyName())
                .build();
    }
}
