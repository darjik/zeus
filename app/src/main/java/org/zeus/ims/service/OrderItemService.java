package org.zeus.ims.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zeus.ims.dto.OrderItemDTO;
import org.zeus.ims.entity.Order;
import org.zeus.ims.entity.OrderItem;
import org.zeus.ims.entity.Product;
import org.zeus.ims.repository.OrderItemRepository;
import org.zeus.ims.repository.OrderRepository;
import org.zeus.ims.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderItemPartService orderItemPartService;

    @Autowired
    public OrderItemService(OrderItemRepository orderItemRepository,
                           OrderRepository orderRepository,
                           ProductRepository productRepository,
                           OrderItemPartService orderItemPartService) {
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.orderItemPartService = orderItemPartService;
    }

    public List<OrderItemDTO> getItemsByOrderId(Long orderId) {
        return orderItemRepository.findByOrderIdOrderByCreatedAt(orderId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<OrderItemDTO> getItemById(Long id) {
        return orderItemRepository.findById(id)
                .map(this::convertToDTO);
    }

    public OrderItemDTO createOrderItem(OrderItemDTO orderItemDTO) {
        OrderItem orderItem = convertToEntity(orderItemDTO);
        OrderItem savedItem = orderItemRepository.save(orderItem);

        // Copy parts from product to order item
        orderItemPartService.copyPartsFromProduct(savedItem.getId(), "system");

        // Update order total amount
        updateOrderTotalAmount(orderItem.getOrder().getId());

        return convertToDTO(savedItem);
    }

    public OrderItemDTO updateOrderItem(Long id, OrderItemDTO orderItemDTO) {
        OrderItem existingItem = orderItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order item not found with id: " + id));

        updateOrderItemFromDTO(existingItem, orderItemDTO);
        OrderItem savedItem = orderItemRepository.save(existingItem);

        // Update order total amount
        updateOrderTotalAmount(existingItem.getOrder().getId());

        return convertToDTO(savedItem);
    }

    public void deleteOrderItem(Long id) {
        OrderItem orderItem = orderItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order item not found with id: " + id));

        Long orderId = orderItem.getOrder().getId();
        orderItemRepository.deleteById(id);

        // Update order total amount
        updateOrderTotalAmount(orderId);
    }

    public List<OrderItemDTO> getItemsByProductId(Long productId) {
        return orderItemRepository.findByProductIdAndOrderActive(productId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public BigDecimal calculateOrderTotal(Long orderId) {
        BigDecimal total = orderItemRepository.calculateTotalAmountByOrderId(orderId);
        return total != null ? total : BigDecimal.ZERO;
    }

    public long getItemsCountByOrderId(Long orderId) {
        return orderItemRepository.countByOrderId(orderId);
    }

    private void updateOrderTotalAmount(Long orderId) {
        BigDecimal totalAmount = calculateOrderTotal(orderId);
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setTotalAmount(totalAmount);
            orderRepository.save(order);
        }
    }

    private OrderItemDTO convertToDTO(OrderItem orderItem) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(orderItem.getId());
        dto.setOrderId(orderItem.getOrder().getId());
        dto.setProductId(orderItem.getProduct().getId());
        dto.setProductName(orderItem.getProduct().getName());
        dto.setQuantity(orderItem.getQuantity());
        dto.setUnitPrice(orderItem.getUnitPrice());
        dto.setTotalPrice(orderItem.getTotalPrice());
        dto.setSpecifications(orderItem.getSpecifications());
        dto.setNotes(orderItem.getNotes());
        dto.setProductStatus(orderItem.getProductStatus() != null ? orderItem.getProductStatus().name() : OrderItem.ProductStatus.PENDING.name());
        dto.setCreatedAt(orderItem.getCreatedAt());
        dto.setUpdatedAt(orderItem.getUpdatedAt());
        return dto;
    }

    private OrderItem convertToEntity(OrderItemDTO dto) {
        OrderItem orderItem = new OrderItem();

        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + dto.getOrderId()));
        orderItem.setOrder(order);

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + dto.getProductId()));
        orderItem.setProduct(product);

        orderItem.setQuantity(dto.getQuantity());
        orderItem.setUnitPrice(dto.getUnitPrice());
        orderItem.setSpecifications(dto.getSpecifications());
        orderItem.setNotes(dto.getNotes());

        return orderItem;
    }

    private void updateOrderItemFromDTO(OrderItem orderItem, OrderItemDTO dto) {
        if (dto.getProductId() != null) {
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + dto.getProductId()));
            orderItem.setProduct(product);
        }

        if (dto.getQuantity() != null) {
            orderItem.setQuantity(dto.getQuantity());
        }
        if (dto.getUnitPrice() != null) {
            orderItem.setUnitPrice(dto.getUnitPrice());
        }
        if (dto.getSpecifications() != null) {
            orderItem.setSpecifications(dto.getSpecifications());
        }
        if (dto.getNotes() != null) {
            orderItem.setNotes(dto.getNotes());
        }
        if (dto.getProductStatus() != null) {
            orderItem.setProductStatus(OrderItem.ProductStatus.valueOf(dto.getProductStatus()));
        }
    }
}
