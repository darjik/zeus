package org.zeus.ims.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zeus.ims.dto.PurchaseOrderDTO;
import org.zeus.ims.dto.PurchaseOrderItemDTO;
import org.zeus.ims.entity.Order;
import org.zeus.ims.entity.OrderItem;
import org.zeus.ims.entity.PurchaseOrder;
import org.zeus.ims.entity.Vendor;
import org.zeus.ims.entity.PurchaseOrderItem;
import org.zeus.ims.repository.OrderItemRepository;
import org.zeus.ims.repository.OrderRepository;
import org.zeus.ims.repository.PurchaseOrderRepository;
import org.zeus.ims.repository.VendorRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final OrderRepository orderRepository;
    private final VendorRepository vendorRepository;
    private final OrderItemRepository orderItemRepository;

    @Autowired
    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
                                OrderRepository orderRepository,
                                VendorRepository vendorRepository,
                                OrderItemRepository orderItemRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.orderRepository = orderRepository;
        this.vendorRepository = vendorRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderDTO> getPurchaseOrdersByOrderId(Long orderId) {
        return purchaseOrderRepository.findByOrderIdOrderByCreatedAtDesc(orderId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PurchaseOrderDTO createPurchaseOrder(Long orderId,
                                                Long vendorId,
                                                List<Long> orderItemIds,
                                                LocalDate expectedDeliveryDate,
                                                String notes,
                                                String createdBy) {
        if (orderItemIds == null || orderItemIds.isEmpty()) {
            throw new IllegalArgumentException("At least one order item must be selected");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found with id: " + vendorId));

        List<OrderItem> selectedItems = orderItemRepository.findAllById(orderItemIds)
                .stream()
                .filter(item -> item.getOrder().getId().equals(orderId))
                .collect(Collectors.toList());

        if (selectedItems.isEmpty()) {
            throw new IllegalArgumentException("No valid order items were selected");
        }

        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setOrder(order);
        purchaseOrder.setVendor(vendor);
        purchaseOrder.setPoNumber(generatePurchaseOrderNumber(order));
        purchaseOrder.setExpectedDeliveryDate(expectedDeliveryDate);
        purchaseOrder.setNotes(notes);
        purchaseOrder.setCreatedBy(createdBy);
        purchaseOrder.setUpdatedBy(createdBy);

        selectedItems.forEach(orderItem -> {
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setOrderItem(orderItem);
            item.setQuantity(orderItem.getQuantity());
            item.setUnitPrice(orderItem.getUnitPrice() != null ? orderItem.getUnitPrice() : BigDecimal.ZERO);
            item.setNotes(orderItem.getSpecifications());
            purchaseOrder.addItem(item);
        });

        PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
        return convertToDTO(saved);
    }

    private String generatePurchaseOrderNumber(Order order) {
        String prefix = order.getOrderNumber() != null ? order.getOrderNumber().replaceAll("[^A-Z0-9-]", "") : "ORD";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return String.format("PO-%s-%s", prefix, timestamp);
    }

    private PurchaseOrderDTO convertToDTO(PurchaseOrder purchaseOrder) {
        PurchaseOrderDTO dto = new PurchaseOrderDTO();
        dto.setId(purchaseOrder.getId());
        dto.setOrderId(purchaseOrder.getOrder().getId());
        dto.setOrderNumber(purchaseOrder.getOrder().getOrderNumber());
        dto.setVendorId(purchaseOrder.getVendor().getId());
        dto.setVendorName(purchaseOrder.getVendor().getCompanyName());
        dto.setPoNumber(purchaseOrder.getPoNumber());
        dto.setStatus(purchaseOrder.getStatus());
        dto.setExpectedDeliveryDate(purchaseOrder.getExpectedDeliveryDate());
        dto.setActualDeliveryDate(purchaseOrder.getActualDeliveryDate());
        dto.setNotes(purchaseOrder.getNotes());
        dto.setCreatedAt(purchaseOrder.getCreatedAt());
        dto.setUpdatedAt(purchaseOrder.getUpdatedAt());
        dto.setCreatedBy(purchaseOrder.getCreatedBy());
        dto.setUpdatedBy(purchaseOrder.getUpdatedBy());
        dto.setItems(purchaseOrder.getItems().stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    private PurchaseOrderItemDTO convertItemToDTO(PurchaseOrderItem item) {
        PurchaseOrderItemDTO dto = new PurchaseOrderItemDTO();
        dto.setId(item.getId());
        dto.setOrderItemId(item.getOrderItem().getId());
        dto.setProductId(item.getOrderItem().getProduct().getId());
        dto.setProductName(item.getOrderItem().getProduct().getName());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setTotalPrice(item.getTotalPrice());
        return dto;
    }
}
