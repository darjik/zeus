package org.zeus.ims.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zeus.ims.dto.EnquiryDTO;
import org.zeus.ims.dto.EnquiryItemDTO;
import org.zeus.ims.entity.Customer;
import org.zeus.ims.entity.Enquiry;
import org.zeus.ims.entity.EnquiryItem;
import org.zeus.ims.repository.CustomerRepository;
import org.zeus.ims.repository.EnquiryRepository;
import org.zeus.ims.repository.EnquiryItemRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EnquiryService {

    private final EnquiryRepository enquiryRepository;
    private final CustomerRepository customerRepository;
    private final EnquiryItemRepository enquiryItemRepository;
    private final EnquiryItemService enquiryItemService;

    @Autowired
    public EnquiryService(EnquiryRepository enquiryRepository,
                          CustomerRepository customerRepository,
                          EnquiryItemRepository enquiryItemRepository,
                          EnquiryItemService enquiryItemService) {
        this.enquiryRepository = enquiryRepository;
        this.customerRepository = customerRepository;
        this.enquiryItemRepository = enquiryItemRepository;
        this.enquiryItemService = enquiryItemService;
    }

    @Transactional(readOnly = true)
    public List<Enquiry> getAllActiveEnquiries() {
        return enquiryRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public Page<Enquiry> getAllActiveEnquiries(Pageable pageable) {
        return enquiryRepository.findByActiveTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Enquiry> getEnquiryById(Long id) {
        return enquiryRepository.findByIdAndActiveTrue(id);
    }

    @Transactional(readOnly = true)
    public Optional<Enquiry> getEnquiryByNumber(String enquiryNumber) {
        return enquiryRepository.findByEnquiryNumber(enquiryNumber);
    }

    @Transactional(readOnly = true)
    public List<Enquiry> getEnquiriesByCustomer(Long customerId) {
        return enquiryRepository.findByCustomerIdAndActiveTrue(customerId);
    }

    @Transactional(readOnly = true)
    public List<Enquiry> getEnquiriesByStatus(Enquiry.EnquiryStatus status) {
        return enquiryRepository.findByStatusAndActiveTrue(status);
    }

    @Transactional(readOnly = true)
    public List<Enquiry> searchEnquiries(String search) {
        return enquiryRepository.searchEnquiries(search);
    }

    @Transactional(readOnly = true)
    public long getActiveEnquiryCount() {
        return enquiryRepository.countActiveEnquiries();
    }

    @Transactional(readOnly = true)
    public long getEnquiryCountByStatus(Enquiry.EnquiryStatus status) {
        return enquiryRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Enquiry> getExpiredQuotes() {
        return enquiryRepository.findExpiredQuotes(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<Enquiry> getEnquiriesWithFilters(Long customerId, Long productId,
                                                  Enquiry.EnquiryStatus status,
                                                  Enquiry.EnquiryPriority priority) {
        return enquiryRepository.findWithFilters(customerId, productId, status, priority);
    }

    public Enquiry createEnquiry(EnquiryDTO enquiryDTO) {
        Customer customer = customerRepository.findById(enquiryDTO.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        Enquiry enquiry = new Enquiry();
        enquiry.setEnquiryNumber(generateEnquiryNumber());
        enquiry.setCustomer(customer);
        enquiry.setDescription(enquiryDTO.getDescription());
        enquiry.setRequirements(enquiryDTO.getRequirements());
        enquiry.setStatus(Enquiry.EnquiryStatus.valueOf(enquiryDTO.getStatus()));
        enquiry.setPriority(Enquiry.EnquiryPriority.valueOf(enquiryDTO.getPriority()));
        enquiry.setTags(enquiryDTO.getTags());
        enquiry.setDeliveryExpectedDate(enquiryDTO.getDeliveryExpectedDate());
        enquiry.setQuoteValidUntil(enquiryDTO.getQuoteValidUntil());
        enquiry.setCreatedBy(getCurrentUsername());

        Enquiry savedEnquiry = enquiryRepository.save(enquiry);

        if (enquiryDTO.getItems() != null && !enquiryDTO.getItems().isEmpty()) {
            for (EnquiryItemDTO itemDTO : enquiryDTO.getItems()) {
                enquiryItemService.addItemToEnquiry(savedEnquiry.getId(), itemDTO);
            }
        }

        return savedEnquiry;
    }

    public Enquiry updateEnquiry(Long id, EnquiryDTO enquiryDTO) {
        Enquiry enquiry = enquiryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Enquiry not found"));

        if (enquiryDTO.getCustomerId() != null) {
            Customer customer = customerRepository.findById(enquiryDTO.getCustomerId())
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
            enquiry.setCustomer(customer);
        }

        if (enquiryDTO.getDescription() != null) {
            enquiry.setDescription(enquiryDTO.getDescription());
        }
        if (enquiryDTO.getRequirements() != null) {
            enquiry.setRequirements(enquiryDTO.getRequirements());
        }
        if (enquiryDTO.getStatus() != null) {
            enquiry.setStatus(Enquiry.EnquiryStatus.valueOf(enquiryDTO.getStatus()));
        }
        if (enquiryDTO.getPriority() != null) {
            enquiry.setPriority(Enquiry.EnquiryPriority.valueOf(enquiryDTO.getPriority()));
        }
        if (enquiryDTO.getTags() != null) {
            enquiry.setTags(enquiryDTO.getTags());
        }
        if (enquiryDTO.getDeliveryExpectedDate() != null) {
            enquiry.setDeliveryExpectedDate(enquiryDTO.getDeliveryExpectedDate());
        }
        if (enquiryDTO.getQuoteValidUntil() != null) {
            enquiry.setQuoteValidUntil(enquiryDTO.getQuoteValidUntil());
        }

        enquiry.setUpdatedBy(getCurrentUsername());

        return enquiryRepository.save(enquiry);
    }

    public void deleteEnquiry(Long id) {
        Enquiry enquiry = enquiryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Enquiry not found"));

        enquiry.setActive(false);
        enquiry.setUpdatedBy(getCurrentUsername());
        enquiryRepository.save(enquiry);
    }

    public EnquiryDTO convertToDTO(Enquiry enquiry) {
        EnquiryDTO dto = new EnquiryDTO();
        dto.setId(enquiry.getId());
        dto.setEnquiryNumber(enquiry.getEnquiryNumber());
        dto.setCustomerId(enquiry.getCustomer().getId());
        dto.setCustomerName(enquiry.getCustomer().getCompanyName());
        dto.setDescription(enquiry.getDescription());
        dto.setRequirements(enquiry.getRequirements());
        dto.setStatus(enquiry.getStatus().name());
        dto.setPriority(enquiry.getPriority().name());
        dto.setTags(enquiry.getTags());
        dto.setDeliveryExpectedDate(enquiry.getDeliveryExpectedDate());
        dto.setQuoteValidUntil(enquiry.getQuoteValidUntil());
        dto.setActive(enquiry.getActive());

        List<EnquiryItem> items = enquiryItemService.getItemsByEnquiryId(enquiry.getId());
        dto.setItems(enquiryItemService.convertToDTOList(items));
        dto.setTotalItems(items.size());
        dto.setTotalAmount(enquiryItemService.calculateTotalAmountForEnquiry(enquiry.getId()));

        return dto;
    }

    private String generateEnquiryNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = enquiryRepository.count() + 1;
        return String.format("ENQ-%s-%04d", datePart, count);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }
}
