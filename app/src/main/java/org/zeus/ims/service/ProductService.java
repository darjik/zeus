package org.zeus.ims.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.zeus.ims.dto.ProductDTO;
import org.zeus.ims.dto.ProductPartDTO;
import org.zeus.ims.entity.Product;
import org.zeus.ims.entity.ProductPart;
import org.zeus.ims.entity.Vendor;
import org.zeus.ims.repository.ProductRepository;
import org.zeus.ims.repository.ProductPartRepository;
import org.zeus.ims.repository.VendorRepository;

import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {


    private final ProductRepository productRepository;
    private final ProductPartRepository productPartRepository;
    private final VendorRepository vendorRepository;

    private static final String UPLOAD_DIR = "uploads/products/";
    private static final String CAD_DRAWINGS_DIR = UPLOAD_DIR + "cad-drawings/";
    private static final String PRODUCT_IMAGES_DIR = UPLOAD_DIR + "images/";
    private static final String CATALOGS_DIR = UPLOAD_DIR + "catalogs/";

    /**
     * Get all products with optional search
     */
    public List<ProductDTO> getAllProducts(String search) {
        try {
            List<Product> products;
            if (search != null && !search.trim().isEmpty()) {
                products = productRepository.findBySearchTerm(search.trim());
            } else {
                products = productRepository.findAllByOrderByName();
            }
            return products.stream().map(this::convertToDTO).collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("Error retrieving products with search: {}", search, ex);
            throw new RuntimeException("Failed to retrieve products", ex);
        }
    }

    /**
     * Get active products only
     */
    public List<ProductDTO> getActiveProducts() {
        try {
            List<Product> activeProducts = productRepository.findByActiveTrueOrderByNameAsc();
            return activeProducts.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("Error getting active products", ex);
            throw new RuntimeException("Failed to load active products", ex);
        }
    }

    /**
     * Get product by ID
     */
    public ProductDTO getProductById(Long id) {
        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
            return convertToDTO(product);
        } catch (EntityNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error retrieving product with id: {}", id, ex);
            throw new RuntimeException("Failed to retrieve product", ex);
        }
    }

    /**
     * Create new product
     */
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO, String currentUser) {
        try {
            validateProductName(productDTO.getName(), null);

            Product product = convertToEntity(productDTO);
            product.setCreatedBy(currentUser);
            product.setUpdatedBy(currentUser);

            Product savedProduct = productRepository.save(product);
            log.info("Product created successfully with id: {} by user: {}", savedProduct.getId(), currentUser);

            return convertToDTO(savedProduct);
        } catch (Exception ex) {
            log.error("Error creating product: {}", productDTO.getName(), ex);
            throw new RuntimeException("Failed to create product", ex);
        }
    }

    /**
     * Update existing product
     */
    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO, String currentUser) {
        try {
            Product existingProduct = productRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

            validateProductName(productDTO.getName(), id);

            updateProductFields(existingProduct, productDTO);
            existingProduct.setUpdatedBy(currentUser);

            Product savedProduct = productRepository.save(existingProduct);
            log.info("Product updated successfully with id: {} by user: {}", id, currentUser);

            return convertToDTO(savedProduct);
        } catch (EntityNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error updating product with id: {}", id, ex);
            throw new RuntimeException("Failed to update product", ex);
        }
    }

    /**
     * Delete product
     */
    @Transactional
    public void deleteProduct(Long id, String currentUser) {
        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

            productRepository.delete(product);
            log.info("Product deleted successfully with id: {} by user: {}", id, currentUser);
        } catch (EntityNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error deleting product with id: {}", id, ex);
            throw new RuntimeException("Failed to delete product", ex);
        }
    }

    /**
     * Toggle product active status
     */
    @Transactional
    public void toggleProductStatus(Long id, String currentUser) {
        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

            product.setActive(!product.getActive());
            product.setUpdatedBy(currentUser);
            productRepository.save(product);

            log.info("Product status toggled for id: {} to {} by user: {}", id, product.getActive(), currentUser);
        } catch (EntityNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error toggling product status for id: {}", id, ex);
            throw new RuntimeException("Failed to toggle product status", ex);
        }
    }

    /**
     * Handle file upload for product
     */
    public String uploadFile(MultipartFile file, String fileType, Long productId) throws IOException {
        if (file.isEmpty()) {
            return null;
        }

        try {
            String uploadDir = getUploadDirectory(fileType);
            createDirectoryIfNotExists(uploadDir);

            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String uniqueFilename = String.format("product_%d_%s_%s%s",
                    productId, fileType, timestamp, fileExtension);

            Path filePath = Paths.get(uploadDir + uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("File uploaded successfully: {}", uniqueFilename);
            return uniqueFilename;
        } catch (IOException ex) {
            log.error("Error uploading file for product id: {}", productId, ex);
            throw ex;
        }
    }

    /**
     * Get total product count
     */
    public long getTotalProductCount() {
        return productRepository.count();
    }

    /**
     * Get active product count
     */
    public long getActiveProductCount() {
        return productRepository.countByActiveTrue();
    }

    /**
     * Get distinct categories
     */
    public List<String> getDistinctCategories() {
        return productRepository.findDistinctCategories();
    }

    /**
     * Get distinct brands
     */
    public List<String> getDistinctBrands() {
        return productRepository.findDistinctBrands();
    }

    /**
     * Convert Product entity to DTO
     */
    private ProductDTO convertToDTO(Product product) {
        List<ProductPartDTO> partDTOs = product.getParts().stream()
                .map(this::convertPartToDTO)
                .collect(Collectors.toList());

        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .modelNumber(product.getModelNumber())
                .brand(product.getBrand())
                .category(product.getCategory())
                .unitOfMeasure(product.getUnitOfMeasure())
                .weight(product.getWeight())
                .dimensions(product.getDimensions())
                .material(product.getMaterial())
                .specifications(product.getSpecifications())
                .cadDrawingsPath(product.getCadDrawingsPath())
                .productImagesPath(product.getProductImagesPath())
                .catalogPath(product.getCatalogPath())
                .active(product.getActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .createdBy(product.getCreatedBy())
                .updatedBy(product.getUpdatedBy())
                .parts(partDTOs)
                .build();
    }

    /**
     * Convert ProductPart entity to DTO
     */
    private ProductPartDTO convertPartToDTO(ProductPart part) {
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
     * Convert ProductDTO to entity
     */
    private Product convertToEntity(ProductDTO productDTO) {
        return Product.builder()
                .name(productDTO.getName())
                .description(productDTO.getDescription())
                .modelNumber(productDTO.getModelNumber())
                .brand(productDTO.getBrand())
                .category(productDTO.getCategory())
                .unitOfMeasure(productDTO.getUnitOfMeasure())
                .weight(productDTO.getWeight())
                .dimensions(productDTO.getDimensions())
                .material(productDTO.getMaterial())
                .specifications(productDTO.getSpecifications())
                .cadDrawingsPath(productDTO.getCadDrawingsPath())
                .productImagesPath(productDTO.getProductImagesPath())
                .catalogPath(productDTO.getCatalogPath())
                .active(productDTO.getActive())
                .build();
    }

    /**
     * Update product fields from DTO
     */
    private void updateProductFields(Product product, ProductDTO productDTO) {
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setModelNumber(productDTO.getModelNumber());
        product.setBrand(productDTO.getBrand());
        product.setCategory(productDTO.getCategory());
        product.setUnitOfMeasure(productDTO.getUnitOfMeasure());
        product.setWeight(productDTO.getWeight());
        product.setDimensions(productDTO.getDimensions());
        product.setMaterial(productDTO.getMaterial());
        product.setSpecifications(productDTO.getSpecifications());
        product.setActive(productDTO.getActive());
    }

    /**
     * Validate product name uniqueness
     */
    private void validateProductName(String name, Long excludeId) {
        if (excludeId != null) {
            if (productRepository.existsByNameAndIdNot(name, excludeId)) {
                throw new IllegalArgumentException("Product name already exists: " + name);
            }
        } else {
            if (productRepository.existsByName(name)) {
                throw new IllegalArgumentException("Product name already exists: " + name);
            }
        }
    }

    /**
     * Get upload directory based on file type
     */
    private String getUploadDirectory(String fileType) {
        switch (fileType.toLowerCase()) {
            case "cad":
                return CAD_DRAWINGS_DIR;
            case "image":
                return PRODUCT_IMAGES_DIR;
            case "catalog":
                return CATALOGS_DIR;
            default:
                return UPLOAD_DIR;
        }
    }

    /**
     * Create directory if it doesn't exist
     */
    private void createDirectoryIfNotExists(String directory) throws IOException {
        Path path = Paths.get(directory);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }
}
