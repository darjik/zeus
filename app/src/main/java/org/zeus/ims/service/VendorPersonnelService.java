package org.zeus.ims.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
public class VendorPersonnelService {

    private final VendorPersonnelRepository vendorPersonnelRepository;
    private final VendorRepository vendorRepository;

    /**
     * Get all personnel for a vendor with optional search
     */
    @Transactional(readOnly = true)
    public List<VendorPersonnelDTO> getVendorPersonnel(Long vendorId, String search) {
        List<VendorPersonnel> personnel;

        if (search != null && !search.trim().isEmpty()) {
            personnel = vendorPersonnelRepository.findByVendorIdAndSearchTerm(vendorId, search.trim());
        } else {
            personnel = vendorPersonnelRepository.findByVendorIdOrderByFullName(vendorId);
        }

        return personnel.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get only active personnel for a vendor
     */
    @Transactional(readOnly = true)
    public List<VendorPersonnelDTO> getActiveVendorPersonnel(Long vendorId) {
        return vendorPersonnelRepository.findByVendorIdAndActiveTrueOrderByFullName(vendorId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get personnel by ID
     */
    @Transactional(readOnly = true)
    public VendorPersonnelDTO getPersonnelById(Long id) {
        VendorPersonnel personnel = vendorPersonnelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vendor personnel not found with id: " + id));
        return convertToDTO(personnel);
    }

    /**
     * Create new personnel for a vendor
     */
    public VendorPersonnelDTO createPersonnel(VendorPersonnelDTO personnelDTO) {
        log.info("Creating new personnel: {} for vendor id: {}", personnelDTO.getFullName(), personnelDTO.getVendorId());

        Vendor vendor = vendorRepository.findById(personnelDTO.getVendorId())
                .orElseThrow(() -> new EntityNotFoundException("Vendor not found with id: " + personnelDTO.getVendorId()));

        // Check if email already exists for this vendor
        if (personnelDTO.getEmail() != null && !personnelDTO.getEmail().trim().isEmpty() &&
            vendorPersonnelRepository.existsByVendorIdAndEmailIgnoreCase(personnelDTO.getVendorId(), personnelDTO.getEmail())) {
            throw new IllegalArgumentException("A personnel with this email already exists for this vendor");
        }

        // If this is set as primary contact, remove primary contact flag from existing personnel
        if (personnelDTO.getIsPrimaryContact()) {
            removePrimaryContactFlag(personnelDTO.getVendorId());
        }

        VendorPersonnel personnel = convertToEntity(personnelDTO, vendor);
        personnel = vendorPersonnelRepository.save(personnel);

        log.info("Created personnel with id: {}", personnel.getId());
        return convertToDTO(personnel);
    }

    /**
     * Update existing personnel
     */
    public VendorPersonnelDTO updatePersonnel(Long id, VendorPersonnelDTO personnelDTO) {
        log.info("Updating personnel with id: {}", id);

        VendorPersonnel existingPersonnel = vendorPersonnelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vendor personnel not found with id: " + id));

        // Check if email already exists for another personnel in this vendor
        if (personnelDTO.getEmail() != null && !personnelDTO.getEmail().trim().isEmpty() &&
            !existingPersonnel.getEmail().equalsIgnoreCase(personnelDTO.getEmail()) &&
            vendorPersonnelRepository.existsByVendorIdAndEmailIgnoreCaseAndIdNot(
                    existingPersonnel.getVendor().getId(), personnelDTO.getEmail(), id)) {
            throw new IllegalArgumentException("A personnel with this email already exists for this vendor");
        }

        // If this is set as primary contact, remove primary contact flag from other personnel
        if (personnelDTO.getIsPrimaryContact() && !existingPersonnel.getIsPrimaryContact()) {
            removePrimaryContactFlag(existingPersonnel.getVendor().getId());
        }

        updatePersonnelFields(existingPersonnel, personnelDTO);
        existingPersonnel = vendorPersonnelRepository.save(existingPersonnel);

        log.info("Updated personnel: {}", existingPersonnel.getFullName());
        return convertToDTO(existingPersonnel);
    }

    /**
     * Delete (deactivate) personnel
     */
    public void deletePersonnel(Long id) {
        log.info("Deactivating personnel with id: {}", id);

        VendorPersonnel personnel = vendorPersonnelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vendor personnel not found with id: " + id));

        personnel.setActive(false);
        personnel.setIsPrimaryContact(false); // Remove primary contact if deactivated

        vendorPersonnelRepository.save(personnel);
        log.info("Deactivated personnel: {}", personnel.getFullName());
    }

    /**
     * Get primary contact for a vendor
     */
    @Transactional(readOnly = true)
    public VendorPersonnelDTO getPrimaryContact(Long vendorId) {
        return vendorPersonnelRepository.findByVendorIdAndIsPrimaryContactTrueAndActiveTrue(vendorId)
                .map(this::convertToDTO)
                .orElse(null);
    }

    /**
     * Set primary contact for a vendor
     */
    public void setPrimaryContact(Long vendorId, Long personnelId) {
        log.info("Setting primary contact for vendor id: {} to personnel id: {}", vendorId, personnelId);

        // Remove primary contact flag from all personnel of this vendor
        removePrimaryContactFlag(vendorId);

        // Set the new primary contact
        VendorPersonnel personnel = vendorPersonnelRepository.findById(personnelId)
                .orElseThrow(() -> new EntityNotFoundException("Vendor personnel not found with id: " + personnelId));

        if (!personnel.getVendor().getId().equals(vendorId)) {
            throw new IllegalArgumentException("Personnel does not belong to the specified vendor");
        }

        personnel.setIsPrimaryContact(true);
        vendorPersonnelRepository.save(personnel);

        log.info("Set {} as primary contact for vendor", personnel.getFullName());
    }

    /**
     * Get personnel count for a vendor
     */
    @Transactional(readOnly = true)
    public long getPersonnelCount(Long vendorId, boolean activeOnly) {
        if (activeOnly) {
            return vendorPersonnelRepository.countByVendorIdAndActiveTrue(vendorId);
        }
        return vendorPersonnelRepository.countByVendorId(vendorId);
    }

    /**
     * Remove primary contact flag from all personnel of a vendor
     */
    private void removePrimaryContactFlag(Long vendorId) {
        List<VendorPersonnel> personnel = vendorPersonnelRepository.findByVendorIdOrderByFullName(vendorId);
        personnel.forEach(p -> p.setIsPrimaryContact(false));
        vendorPersonnelRepository.saveAll(personnel);
    }

    /**
     * Convert entity to DTO
     */
    private VendorPersonnelDTO convertToDTO(VendorPersonnel personnel) {
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
                .createdAt(personnel.getCreatedAt())
                .updatedAt(personnel.getUpdatedAt())
                .createdBy(personnel.getCreatedBy())
                .updatedBy(personnel.getUpdatedBy())
                .build();
    }

    /**
     * Convert DTO to entity
     */
    private VendorPersonnel convertToEntity(VendorPersonnelDTO personnelDTO, Vendor vendor) {
        return VendorPersonnel.builder()
                .fullName(personnelDTO.getFullName())
                .designation(personnelDTO.getDesignation())
                .department(personnelDTO.getDepartment())
                .email(personnelDTO.getEmail())
                .contactNumber(personnelDTO.getContactNumber())
                .secondaryContact(personnelDTO.getSecondaryContact())
                .isPrimaryContact(personnelDTO.getIsPrimaryContact())
                .active(personnelDTO.getActive())
                .notes(personnelDTO.getNotes())
                .vendor(vendor)
                .build();
    }

    /**
     * Update personnel fields from DTO
     */
    private void updatePersonnelFields(VendorPersonnel personnel, VendorPersonnelDTO personnelDTO) {
        personnel.setFullName(personnelDTO.getFullName());
        personnel.setDesignation(personnelDTO.getDesignation());
        personnel.setDepartment(personnelDTO.getDepartment());
        personnel.setEmail(personnelDTO.getEmail());
        personnel.setContactNumber(personnelDTO.getContactNumber());
        personnel.setSecondaryContact(personnelDTO.getSecondaryContact());
        personnel.setIsPrimaryContact(personnelDTO.getIsPrimaryContact());
        personnel.setActive(personnelDTO.getActive());
        personnel.setNotes(personnelDTO.getNotes());
    }
}
