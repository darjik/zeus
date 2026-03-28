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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.zeus.ims.dto.ProductDTO;
import org.zeus.ims.dto.ProductPartDTO;
import org.zeus.ims.dto.VendorDTO;
import org.zeus.ims.service.ProductPartService;
import org.zeus.ims.service.ProductService;
import org.zeus.ims.service.VendorService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final ProductPartService productPartService;
    private final VendorService vendorService;

    /**
     * Display products list page
     */
    @GetMapping
    public String listProducts(@RequestParam(value = "search", required = false) String search, Model model) {
        try {
            List<ProductDTO> products = productService.getAllProducts(search);
            long totalProducts = productService.getTotalProductCount();
            long activeProducts = productService.getActiveProductCount();

            model.addAttribute("products", products);
            model.addAttribute("totalProducts", totalProducts);
            model.addAttribute("activeProducts", activeProducts);
            model.addAttribute("search", search);

            return "products/list";
        } catch (Exception ex) {
            log.error("Error loading products list", ex);
            model.addAttribute("errorMessage", "Failed to load products. Please try again.");
            return "products/list";
        }
    }

    /**
     * Display product creation form
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        try {
            ProductDTO productDTO = new ProductDTO();
            List<String> categories = productService.getDistinctCategories();
            List<String> brands = productService.getDistinctBrands();
            List<String> unitOptions = getUnitOfMeasureOptions();

            model.addAttribute("product", productDTO);
            model.addAttribute("categories", categories);
            model.addAttribute("brands", brands);
            model.addAttribute("unitOptions", unitOptions);
            model.addAttribute("isEdit", false);

            return "products/form";
        } catch (Exception ex) {
            log.error("Error loading product creation form", ex);
            return "redirect:/products?error=form_load_failed";
        }
    }

    /**
     * Handle product creation
     */
    @PostMapping("/new")
    public String createProduct(@Valid @ModelAttribute("product") ProductDTO productDTO,
                               BindingResult bindingResult,
                               @RequestParam(value = "cadFile", required = false) MultipartFile cadFile,
                               @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                               @RequestParam(value = "catalogFile", required = false) MultipartFile catalogFile,
                               HttpSession session,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        try {
            if (bindingResult.hasErrors()) {
                List<String> categories = productService.getDistinctCategories();
                List<String> brands = productService.getDistinctBrands();
                List<String> unitOptions = getUnitOfMeasureOptions();

                model.addAttribute("categories", categories);
                model.addAttribute("brands", brands);
                model.addAttribute("unitOptions", unitOptions);
                model.addAttribute("isEdit", false);
                return "products/form";
            }

            String currentUser = (String) session.getAttribute("username");
            if (currentUser == null) {
                currentUser = "system";
            }

            ProductDTO createdProduct = productService.createProduct(productDTO, currentUser);

            // Handle file uploads
            handleFileUploads(createdProduct.getId(), cadFile, imageFile, catalogFile, currentUser);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Product '" + createdProduct.getName() + "' created successfully!");
            return "redirect:/products/" + createdProduct.getId();

        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("name", "error.product", ex.getMessage());
            List<String> categories = productService.getDistinctCategories();
            List<String> brands = productService.getDistinctBrands();
            List<String> unitOptions = getUnitOfMeasureOptions();

            model.addAttribute("categories", categories);
            model.addAttribute("brands", brands);
            model.addAttribute("unitOptions", unitOptions);
            model.addAttribute("isEdit", false);
            return "products/form";
        } catch (Exception ex) {
            log.error("Error creating product", ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create product. Please try again.");
            return "redirect:/products/new";
        }
    }

    /**
     * Display product details
     */
    @GetMapping("/{id}")
    public String viewProduct(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            ProductDTO product = productService.getProductById(id);
            List<ProductPartDTO> parts = productPartService.getPartsByProductId(id);

            model.addAttribute("product", product);
            model.addAttribute("parts", parts);

            return "products/view";
        } catch (EntityNotFoundException ex) {
            log.warn("Product not found with id: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", "Product not found.");
            return "redirect:/products";
        } catch (Exception ex) {
            log.error("Error loading product details for id: {}", id, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to load product details.");
            return "redirect:/products";
        }
    }

    /**
     * Display product edit form
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            ProductDTO product = productService.getProductById(id);
            List<String> categories = productService.getDistinctCategories();
            List<String> brands = productService.getDistinctBrands();
            List<String> unitOptions = getUnitOfMeasureOptions();

            model.addAttribute("product", product);
            model.addAttribute("categories", categories);
            model.addAttribute("brands", brands);
            model.addAttribute("unitOptions", unitOptions);
            model.addAttribute("isEdit", true);

            return "products/form";
        } catch (EntityNotFoundException ex) {
            log.warn("Product not found for edit with id: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", "Product not found.");
            return "redirect:/products";
        } catch (Exception ex) {
            log.error("Error loading product edit form for id: {}", id, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to load product for editing.");
            return "redirect:/products";
        }
    }

    /**
     * Handle product update
     */
    @PostMapping("/{id}/edit")
    public String updateProduct(@PathVariable Long id,
                               @Valid @ModelAttribute("product") ProductDTO productDTO,
                               BindingResult bindingResult,
                               @RequestParam(value = "cadFile", required = false) MultipartFile cadFile,
                               @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                               @RequestParam(value = "catalogFile", required = false) MultipartFile catalogFile,
                               HttpSession session,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        try {
            if (bindingResult.hasErrors()) {
                List<String> categories = productService.getDistinctCategories();
                List<String> brands = productService.getDistinctBrands();
                List<String> unitOptions = getUnitOfMeasureOptions();

                model.addAttribute("categories", categories);
                model.addAttribute("brands", brands);
                model.addAttribute("unitOptions", unitOptions);
                model.addAttribute("isEdit", true);
                return "products/form";
            }

            String currentUser = (String) session.getAttribute("username");
            if (currentUser == null) {
                currentUser = "system";
            }

            ProductDTO updatedProduct = productService.updateProduct(id, productDTO, currentUser);

            // Handle file uploads
            handleFileUploads(id, cadFile, imageFile, catalogFile, currentUser);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Product '" + updatedProduct.getName() + "' updated successfully!");
            return "redirect:/products/" + id;

        } catch (EntityNotFoundException ex) {
            log.warn("Product not found for update with id: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", "Product not found.");
            return "redirect:/products";
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("name", "error.product", ex.getMessage());
            List<String> categories = productService.getDistinctCategories();
            List<String> brands = productService.getDistinctBrands();
            List<String> unitOptions = getUnitOfMeasureOptions();

            model.addAttribute("categories", categories);
            model.addAttribute("brands", brands);
            model.addAttribute("unitOptions", unitOptions);
            model.addAttribute("isEdit", true);
            return "products/form";
        } catch (Exception ex) {
            log.error("Error updating product with id: {}", id, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update product. Please try again.");
            return "redirect:/products/" + id + "/edit";
        }
    }

    /**
     * Toggle product active status
     */
    @PostMapping("/{id}/toggle-status")
    @ResponseBody
    public ResponseEntity<String> toggleProductStatus(@PathVariable Long id, HttpSession session) {
        try {
            String currentUser = (String) session.getAttribute("username");
            if (currentUser == null) {
                currentUser = "system";
            }

            productService.toggleProductStatus(id, currentUser);
            return ResponseEntity.ok("Status updated successfully");
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.badRequest().body("Product not found");
        } catch (Exception ex) {
            log.error("Error toggling product status for id: {}", id, ex);
            return ResponseEntity.badRequest().body("Failed to update status");
        }
    }

    /**
     * Delete product
     */
    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            String currentUser = (String) session.getAttribute("username");
            if (currentUser == null) {
                currentUser = "system";
            }

            ProductDTO product = productService.getProductById(id);
            productService.deleteProduct(id, currentUser);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Product '" + product.getName() + "' deleted successfully!");
            return "redirect:/products";
        } catch (EntityNotFoundException ex) {
            log.warn("Product not found for deletion with id: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", "Product not found.");
            return "redirect:/products";
        } catch (Exception ex) {
            log.error("Error deleting product with id: {}", id, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete product. Please try again.");
            return "redirect:/products";
        }
    }

    /**
     * Display parts management page for a product
     */
    @GetMapping("/{id}/parts")
    public String manageProductParts(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            ProductDTO product = productService.getProductById(id);
            List<ProductPartDTO> parts = productPartService.getPartsByProductId(id);
            List<VendorDTO> vendors = vendorService.getActiveVendors();

            model.addAttribute("product", product);
            model.addAttribute("parts", parts);
            model.addAttribute("vendors", vendors);
            model.addAttribute("newPart", new ProductPartDTO());

            return "products/parts";
        } catch (EntityNotFoundException ex) {
            log.warn("Product not found for parts management with id: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", "Product not found.");
            return "redirect:/products";
        } catch (Exception ex) {
            log.error("Error loading product parts for id: {}", id, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to load product parts.");
            return "redirect:/products/" + id;
        }
    }

    /**
     * API endpoint to get active products for AJAX calls
     */
    @GetMapping("/api/active")
    @ResponseBody
    public ResponseEntity<List<ProductDTO>> getActiveProducts() {
        try {
            List<ProductDTO> activeProducts = productService.getActiveProducts();
            return ResponseEntity.ok(activeProducts);
        } catch (Exception ex) {
            log.error("Error loading active products", ex);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Handle file uploads for product
     */
    private void handleFileUploads(Long productId, MultipartFile cadFile, MultipartFile imageFile,
                                 MultipartFile catalogFile, String currentUser) {
        try {
            if (cadFile != null && !cadFile.isEmpty()) {
                String cadPath = productService.uploadFile(cadFile, "cad", productId);
                // Update product with CAD file path
                ProductDTO product = productService.getProductById(productId);
                product.setCadDrawingsPath(cadPath);
                productService.updateProduct(productId, product, currentUser);
            }

            if (imageFile != null && !imageFile.isEmpty()) {
                String imagePath = productService.uploadFile(imageFile, "image", productId);
                // Update product with image file path
                ProductDTO product = productService.getProductById(productId);
                product.setProductImagesPath(imagePath);
                productService.updateProduct(productId, product, currentUser);
            }

            if (catalogFile != null && !catalogFile.isEmpty()) {
                String catalogPath = productService.uploadFile(catalogFile, "catalog", productId);
                // Update product with catalog file path
                ProductDTO product = productService.getProductById(productId);
                product.setCatalogPath(catalogPath);
                productService.updateProduct(productId, product, currentUser);
            }
        } catch (Exception ex) {
            log.error("Error handling file uploads for product id: {}", productId, ex);
        }
    }

    /**
     * Get unit of measure options
     */
    private List<String> getUnitOfMeasureOptions() {
        return Arrays.asList(
                "Pieces", "Kilograms", "Grams", "Meters", "Centimeters", "Millimeters",
                "Liters", "Milliliters", "Square Meters", "Cubic Meters", "Boxes", "Sets"
        );
    }
}
