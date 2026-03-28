package org.zeus.ims.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zeus.ims.dto.OrderItemPartDTO;
import org.zeus.ims.entity.OrderItem;
import org.zeus.ims.entity.OrderItemPart;
import org.zeus.ims.entity.Product;
import org.zeus.ims.entity.ProductPart;
import org.zeus.ims.repository.OrderItemPartRepository;
import org.zeus.ims.repository.OrderItemRepository;
import org.zeus.ims.repository.ProductPartRepository;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderItemPartService {

    private final OrderItemPartRepository orderItemPartRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductPartRepository productPartRepository;

    @Autowired
    public OrderItemPartService(OrderItemPartRepository orderItemPartRepository,
                                OrderItemRepository orderItemRepository,
                                ProductPartRepository productPartRepository) {
        this.orderItemPartRepository = orderItemPartRepository;
        this.orderItemRepository = orderItemRepository;
        this.productPartRepository = productPartRepository;
    }

    public void copyPartsFromProduct(Long orderItemId, String createdBy) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new RuntimeException("Order item not found with id: " + orderItemId));

        Product product = orderItem.getProduct();

        if (product.getParts() != null && !product.getParts().isEmpty()) {
            product.getParts().forEach(productPart -> {
                if (productPart.getActive()) {
                    OrderItemPart orderItemPart = new OrderItemPart();
                    orderItemPart.setOrderItem(orderItem);
                    orderItemPart.setProductPart(productPart);
                    orderItemPart.setQuantityRequired(productPart.getQuantityRequired() * orderItem.getQuantity());
                    orderItemPart.setCreatedBy(createdBy);
                    orderItemPart.setUpdatedBy(createdBy);
                    orderItemPartRepository.save(orderItemPart);
                }
            });
        }
    }

    @Transactional(readOnly = true)
    public List<OrderItemPartDTO> getPartsByOrderItemId(Long orderItemId) {
        return orderItemPartRepository.findByOrderItemIdOrderByCreatedAt(orderItemId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public OrderItemPartDTO addPartToOrderItem(Long orderItemId, Long productPartId, Integer quantityRequired, String createdBy) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new RuntimeException("Order item not found with id: " + orderItemId));

        ProductPart productPart = productPartRepository.findById(productPartId)
                .orElseThrow(() -> new RuntimeException("Product part not found with id: " + productPartId));

        OrderItemPart orderItemPart = new OrderItemPart();
        orderItemPart.setOrderItem(orderItem);
        orderItemPart.setProductPart(productPart);
        orderItemPart.setQuantityRequired(quantityRequired != null ? quantityRequired : productPart.getQuantityRequired());
        orderItemPart.setCreatedBy(createdBy);
        orderItemPart.setUpdatedBy(createdBy);

        OrderItemPart saved = orderItemPartRepository.save(orderItemPart);
        return convertToDTO(saved);
    }

    public void removePartFromOrderItem(Long orderItemPartId) {
        orderItemPartRepository.deleteById(orderItemPartId);
    }

    public OrderItemPartDTO updatePartQuantity(Long orderItemPartId, Integer quantityRequired, String updatedBy) {
        OrderItemPart orderItemPart = orderItemPartRepository.findById(orderItemPartId)
                .orElseThrow(() -> new RuntimeException("Order item part not found with id: " + orderItemPartId));

        orderItemPart.setQuantityRequired(quantityRequired);
        orderItemPart.setUpdatedBy(updatedBy);

        OrderItemPart saved = orderItemPartRepository.save(orderItemPart);
        return convertToDTO(saved);
    }

    private OrderItemPartDTO convertToDTO(OrderItemPart orderItemPart) {
        OrderItemPartDTO dto = new OrderItemPartDTO();
        dto.setId(orderItemPart.getId());
        dto.setOrderItemId(orderItemPart.getOrderItem().getId());
        dto.setProductPartId(orderItemPart.getProductPart().getId());
        dto.setPartName(orderItemPart.getProductPart().getPartName());
        dto.setPartNumber(orderItemPart.getProductPart().getPartNumber());
        dto.setQuantityRequired(orderItemPart.getQuantityRequired());
        dto.setDimensions(orderItemPart.getProductPart().getDimensions());
        dto.setMaterial(orderItemPart.getProductPart().getMaterial());
        dto.setVendorName(orderItemPart.getProductPart().getVendor() != null ?
            orderItemPart.getProductPart().getVendor().getCompanyName() : null);
        dto.setNotes(orderItemPart.getNotes());
        return dto;
    }
}

