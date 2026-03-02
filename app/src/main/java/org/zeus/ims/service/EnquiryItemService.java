package org.zeus.ims.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zeus.ims.dto.EnquiryItemDTO;
import org.zeus.ims.entity.Enquiry;
import org.zeus.ims.entity.EnquiryItem;
import org.zeus.ims.entity.Product;
import org.zeus.ims.repository.EnquiryItemRepository;
import org.zeus.ims.repository.EnquiryRepository;
import org.zeus.ims.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class EnquiryItemService {

    private final EnquiryItemRepository enquiryItemRepository;
    private final EnquiryRepository enquiryRepository;
    private final ProductRepository productRepository;

    @Autowired
    public EnquiryItemService(EnquiryItemRepository enquiryItemRepository,
                              EnquiryRepository enquiryRepository,
                              ProductRepository productRepository) {
        this.enquiryItemRepository = enquiryItemRepository;
        this.enquiryRepository = enquiryRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<EnquiryItem> getItemsByEnquiryId(Long enquiryId) {
        return enquiryItemRepository.findByEnquiryIdOrderByCreatedAtAsc(enquiryId);
    }

    @Transactional(readOnly = true)
    public Optional<EnquiryItem> getItemById(Long id) {
        return enquiryItemRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateTotalAmountForEnquiry(Long enquiryId) {
        BigDecimal total = enquiryItemRepository.calculateTotalAmountForEnquiry(enquiryId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public long getItemCountForEnquiry(Long enquiryId) {
        return enquiryItemRepository.countByEnquiryId(enquiryId);
    }

    public EnquiryItem addItemToEnquiry(Long enquiryId, EnquiryItemDTO itemDTO) {
        Enquiry enquiry = enquiryRepository.findByIdAndActiveTrue(enquiryId)
                .orElseThrow(() -> new IllegalArgumentException("Enquiry not found"));
        Product product = productRepository.findById(itemDTO.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        EnquiryItem item = new EnquiryItem();
        item.setEnquiry(enquiry);
        item.setProduct(product);
        item.setQuantity(itemDTO.getQuantity());
        item.setUnitPrice(itemDTO.getUnitPrice());
        item.setSpecifications(itemDTO.getSpecifications());
        item.setRemarks(itemDTO.getRemarks());
        item.setCreatedBy(getCurrentUsername());

        return enquiryItemRepository.save(item);
    }

    public EnquiryItem updateEnquiryItem(Long id, EnquiryItemDTO itemDTO) {
        EnquiryItem item = enquiryItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Enquiry item not found"));

        if (itemDTO.getProductId() != null) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));
            item.setProduct(product);
        }

        if (itemDTO.getQuantity() != null) {
            item.setQuantity(itemDTO.getQuantity());
        }
        if (itemDTO.getUnitPrice() != null) {
            item.setUnitPrice(itemDTO.getUnitPrice());
        }
        if (itemDTO.getSpecifications() != null) {
            item.setSpecifications(itemDTO.getSpecifications());
        }
        if (itemDTO.getRemarks() != null) {
            item.setRemarks(itemDTO.getRemarks());
        }

        item.setUpdatedBy(getCurrentUsername());

        return enquiryItemRepository.save(item);
    }

    public void deleteEnquiryItem(Long id) {
        EnquiryItem item = enquiryItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Enquiry item not found"));
        enquiryItemRepository.delete(item);
    }

    public void deleteAllItemsByEnquiryId(Long enquiryId) {
        enquiryItemRepository.deleteByEnquiryId(enquiryId);
    }

    public EnquiryItemDTO convertToDTO(EnquiryItem item) {
        EnquiryItemDTO dto = new EnquiryItemDTO();
        dto.setId(item.getId());
        dto.setEnquiryId(item.getEnquiry().getId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setTotalAmount(item.getTotalAmount());
        dto.setSpecifications(item.getSpecifications());
        dto.setRemarks(item.getRemarks());
        return dto;
    }

    public List<EnquiryItemDTO> convertToDTOList(List<EnquiryItem> items) {
        return items.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }
}
