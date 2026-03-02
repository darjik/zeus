package org.zeus.ims.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zeus.ims.dto.ProductPartDTO;
import org.zeus.ims.entity.Product;
import org.zeus.ims.entity.ProductPart;
import org.zeus.ims.entity.Vendor;
import org.zeus.ims.repository.ProductPartRepository;
import org.zeus.ims.repository.ProductRepository;
import org.zeus.ims.repository.VendorRepository;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductPartService {

    private static final Logger logger = LoggerFactory.getLogger(ProductPartService.class);

    private final ProductPartRepository productPartRepository;
    private final ProductRepository productRepository;
    private final VendorRepository vendorRepository;

    /**
     * Get all parts for a product
     */
    public List<ProductPartDTO> getPartsByProductId(Long productId) {
        try {
            List<ProductPart> parts = productPartRepository.findByProductIdOrderByPartName(productId);
            return parts.stream().map(this::convertToDTO).collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("Error retrieving parts for product id: {}", productId, ex);
            throw new RuntimeException("Failed to retrieve product parts", ex);
        }
    }

    /**
     * Get active parts for a product
     */
    public List<ProductPartDTO> getActivePartsByProductId(Long productId) {
        try {
            List<ProductPart> parts = productPartRepository.findByProductIdAndActiveTrueOrderByPartName(productId);
            return parts.stream().map(this::convertToDTO).collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("Error retrieving active parts for product id: {}", productId, ex);
            throw new RuntimeException("Failed to retrieve active product parts", ex);
        }
    }

    /**
     * Get part by ID
     */
    public ProductPartDTO getPartById(Long id) {
        try {
            ProductPart part = productPartRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Product part not found with id: " + id));
            return convertToDTO(part);
        } catch (EntityNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error retrieving product part with id: {}", id, ex);
            throw new RuntimeException("Failed to retrieve product part", ex);
        }
    }

    /**
     * Create new product part
     */
    @Transactional
    public ProductPartDTO createPart(ProductPartDTO partDTO, String currentUser) {
        try {
            validatePartName(partDTO.getPartName(), partDTO.getProductId(), null);

            Product product = productRepository.findById(partDTO.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + partDTO.getProductId()));

            Vendor vendor = null;
            if (partDTO.getVendorId() != null) {
                vendor = vendorRepository.findById(partDTO.getVendorId())
                        .orElseThrow(() -> new EntityNotFoundException("Vendor not found with id: " + partDTO.getVendorId()));
            }

            ProductPart part = convertToEntity(partDTO, product, vendor);
            part.setCreatedBy(currentUser);
            part.setUpdatedBy(currentUser);

            ProductPart savedPart = productPartRepository.save(part);
            log.info("Product part created successfully with id: {} for product: {} by user: {}",
                    savedPart.getId(), product.getName(), currentUser);

            return convertToDTO(savedPart);
        } catch (EntityNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error creating product part: {} for product id: {}", partDTO.getPartName(), partDTO.getProductId(), ex);
            throw new RuntimeException("Failed to create product part", ex);
        }
    }

    /**
     * Update existing product part
     */
    @Transactional
    public ProductPartDTO updatePart(Long id, ProductPartDTO partDTO, String currentUser) {
        try {
            ProductPart existingPart = productPartRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Product part not found with id: " + id));

            validatePartName(partDTO.getPartName(), existingPart.getProduct().getId(), id);

            Vendor vendor = null;
            if (partDTO.getVendorId() != null) {
                vendor = vendorRepository.findById(partDTO.getVendorId())
                        .orElseThrow(() -> new EntityNotFoundException("Vendor not found with id: " + partDTO.getVendorId()));
            }

            updatePartFields(existingPart, partDTO, vendor);
            existingPart.setUpdatedBy(currentUser);

            ProductPart savedPart = productPartRepository.save(existingPart);
            log.info("Product part updated successfully with id: {} by user: {}", id, currentUser);

            return convertToDTO(savedPart);
        } catch (EntityNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error updating product part with id: {}", id, ex);
            throw new RuntimeException("Failed to update product part", ex);
        }
    }

    /**
     * Delete product part
     */
    @Transactional
    public void deletePart(Long id, String currentUser) {
        try {
            ProductPart part = productPartRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Product part not found with id: " + id));

            productPartRepository.delete(part);
            log.info("Product part deleted successfully with id: {} by user: {}", id, currentUser);
        } catch (EntityNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error deleting product part with id: {}", id, ex);
            throw new RuntimeException("Failed to delete product part", ex);
        }
    }

    /**
     * Toggle part active status
     */
    @Transactional
    public void togglePartStatus(Long id, String currentUser) {
        try {
            ProductPart part = productPartRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Product part not found with id: " + id));

            part.setActive(!part.getActive());
            part.setUpdatedBy(currentUser);
            productPartRepository.save(part);

            log.info("Product part status toggled for id: {} to {} by user: {}", id, part.getActive(), currentUser);
        } catch (EntityNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error toggling product part status for id: {}", id, ex);
            throw new RuntimeException("Failed to toggle product part status", ex);
        }
    }

    /**
     * Get parts by vendor
     */
    public List<ProductPartDTO> getPartsByVendorId(Long vendorId) {
        try {
            List<ProductPart> parts = productPartRepository.findByVendorIdOrderByPartName(vendorId);
            return parts.stream().map(this::convertToDTO).collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("Error retrieving parts for vendor id: {}", vendorId, ex);
            throw new RuntimeException("Failed to retrieve vendor parts", ex);
        }
    }

    /**
     * Get parts without vendor assigned
     */
    public List<ProductPartDTO> getPartsWithoutVendor() {
        try {
            List<ProductPart> parts = productPartRepository.findByVendorIsNullOrderByPartName();
            return parts.stream().map(this::convertToDTO).collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("Error retrieving parts without vendor", ex);
            throw new RuntimeException("Failed to retrieve parts without vendor", ex);
        }
    }

    /**
     * Get parts count by product
     */
    public long getPartsCountByProductId(Long productId) {
        return productPartRepository.countByProductId(productId);
    }

    /**
     * Get active parts count by product
     */
    public long getActivePartsCountByProductId(Long productId) {
        return productPartRepository.countByProductIdAndActiveTrue(productId);
    }

    /**
     * Convert ProductPart entity to DTO
     */
    private ProductPartDTO convertToDTO(ProductPart part) {
        return ProductPartDTO.builder()
                .id(part.getId())
                .partName(part.getPartName())
                .partNumber(part.getPartNumber())
                .description(part.getDescription())
                .material(part.getMaterial())
                .dimensions(part.getDimensions())
                .unitOfMeasure(part.getUnitOfMeasure())
                .quantityRequired(part.getQuantityRequired())
                .specifications(part.getSpecifications())
                .active(part.getActive())
                .createdAt(part.getCreatedAt())
                .updatedAt(part.getUpdatedAt())
                .createdBy(part.getCreatedBy())
                .updatedBy(part.getUpdatedBy())
                .productId(part.getProduct().getId())
                .vendorId(part.getVendor() != null ? part.getVendor().getId() : null)
                .vendorName(part.getVendor() != null ? part.getVendor().getCompanyName() : null)
                .build();
    }

    /**
     * Convert ProductPartDTO to entity
     */
    private ProductPart convertToEntity(ProductPartDTO partDTO, Product product, Vendor vendor) {
        return ProductPart.builder()
                .partName(partDTO.getPartName())
                .partNumber(partDTO.getPartNumber())
                .description(partDTO.getDescription())
                .material(partDTO.getMaterial())
                .dimensions(partDTO.getDimensions())
                .unitOfMeasure(partDTO.getUnitOfMeasure())
                .quantityRequired(partDTO.getQuantityRequired())
                .specifications(partDTO.getSpecifications())
                .active(partDTO.getActive())
                .product(product)
                .vendor(vendor)
                .build();
    }

    /**
     * Update part fields from DTO
     */
    private void updatePartFields(ProductPart part, ProductPartDTO partDTO, Vendor vendor) {
        part.setPartName(partDTO.getPartName());
        part.setPartNumber(partDTO.getPartNumber());
        part.setDescription(partDTO.getDescription());
        part.setMaterial(partDTO.getMaterial());
        part.setDimensions(partDTO.getDimensions());
        part.setUnitOfMeasure(partDTO.getUnitOfMeasure());
        part.setQuantityRequired(partDTO.getQuantityRequired());
        part.setSpecifications(partDTO.getSpecifications());
        part.setActive(partDTO.getActive());
        part.setVendor(vendor);
    }

    /**
     * Validate part name uniqueness within product
     */
    private void validatePartName(String partName, Long productId, Long excludeId) {
        if (excludeId != null) {
            if (productPartRepository.existsByPartNameAndProductIdAndIdNot(partName, productId, excludeId)) {
                throw new IllegalArgumentException("Part name already exists in this product: " + partName);
            }
        } else {
            if (productPartRepository.existsByPartNameAndProductId(partName, productId)) {
                throw new IllegalArgumentException("Part name already exists in this product: " + partName);
            }
        }
    }
}
