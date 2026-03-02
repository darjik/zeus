package org.zeus.ims.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.zeus.ims.dto.OrderDTO;
import org.zeus.ims.entity.Customer;
import org.zeus.ims.entity.CustomerPersonnel;
import org.zeus.ims.entity.Enquiry;
import org.zeus.ims.entity.Order;
import org.zeus.ims.entity.OrderItem;
import org.zeus.ims.repository.CustomerPersonnelRepository;
import org.zeus.ims.repository.CustomerRepository;
import org.zeus.ims.repository.EnquiryRepository;
import org.zeus.ims.repository.OrderRepository;
import org.zeus.ims.repository.OrderItemRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;
    private final CustomerPersonnelRepository customerPersonnelRepository;
    private final EnquiryRepository enquiryRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        CustomerRepository customerRepository,
                        CustomerPersonnelRepository customerPersonnelRepository,
                        EnquiryRepository enquiryRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.customerRepository = customerRepository;
        this.customerPersonnelRepository = customerPersonnelRepository;
        this.enquiryRepository = enquiryRepository;
    }

    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        return orderRepository.findByActiveTrueOrderByCreatedAtDesc(pageable)
                .map(this::convertToDTO);
    }

    public Page<OrderDTO> searchOrders(String searchTerm, Pageable pageable) {
        if (StringUtils.hasText(searchTerm)) {
            return orderRepository.findBySearchTerm(searchTerm.trim(), pageable)
                    .map(this::convertToDTO);
        }
        return getAllOrders(pageable);
    }

    public Page<OrderDTO> getOrdersByCustomer(Long customerId, Pageable pageable) {
        return orderRepository.findByCustomerIdAndActiveTrueOrderByCreatedAtDesc(customerId, pageable)
                .map(this::convertToDTO);
    }

    public Page<OrderDTO> getOrdersByStatus(Order.OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatusAndActiveTrueOrderByCreatedAtDesc(status, pageable)
                .map(this::convertToDTO);
    }

    public Optional<OrderDTO> getOrderById(Long id) {
        return orderRepository.findById(id)
                .filter(Order::getActive)
                .map(this::convertToDTO);
    }

    public Optional<OrderDTO> getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .filter(Order::getActive)
                .map(this::convertToDTO);
    }

    public OrderDTO createOrder(OrderDTO orderDTO, String createdBy) {
        Order order = convertToEntity(orderDTO);
        order.setOrderNumber(generateOrderNumber());
        order.setCreatedBy(createdBy);
        order.setUpdatedBy(createdBy);

        Order savedOrder = orderRepository.save(order);
        return convertToDTO(savedOrder);
    }

    public OrderDTO createOrderFromEnquiry(Long enquiryId, String createdBy) {
        Optional<Enquiry> enquiryOpt = enquiryRepository.findById(enquiryId);
        if (!enquiryOpt.isPresent()) {
            throw new RuntimeException("Enquiry not found with id: " + enquiryId);
        }

        Enquiry enquiry = enquiryOpt.get();

        // Check if enquiry is already converted
        if (enquiry.getStatus() == Enquiry.EnquiryStatus.CONVERTED) {
            throw new RuntimeException("Enquiry has already been converted to an order");
        }

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setCustomer(enquiry.getCustomer());
        order.setEnquiry(enquiry);
        order.setDescription(enquiry.getDescription());
        order.setRequirements(enquiry.getRequirements());
        order.setPriority(convertPriority(enquiry.getPriority()));

        // Convert LocalDateTime to LocalDate for delivery expected date
        if (enquiry.getDeliveryExpectedDate() != null) {
            order.setDeliveryExpectedDate(enquiry.getDeliveryExpectedDate().toLocalDate());
        }

        order.setCreatedBy(createdBy);
        order.setUpdatedBy(createdBy);

        Order savedOrder = orderRepository.save(order);

        // Copy enquiry items to order items
        copyEnquiryItemsToOrder(enquiry, savedOrder);

        // Update enquiry status to converted
        enquiry.setStatus(Enquiry.EnquiryStatus.CONVERTED);
        enquiry.setUpdatedBy(createdBy);
        enquiryRepository.save(enquiry);

        return convertToDTO(savedOrder);
    }

    private void copyEnquiryItemsToOrder(Enquiry enquiry, Order order) {
        enquiry.getItems().forEach(enquiryItem -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(enquiryItem.getProduct());
            orderItem.setQuantity(enquiryItem.getQuantity());
            orderItem.setUnitPrice(enquiryItem.getUnitPrice());
            orderItem.setTotalPrice(enquiryItem.getTotalAmount()); // EnquiryItem has totalAmount, OrderItem has totalPrice
            orderItem.setSpecifications(enquiryItem.getSpecifications());
            orderItem.setNotes(enquiryItem.getRemarks()); // EnquiryItem has remarks, OrderItem has notes
            orderItemRepository.save(orderItem);
        });
    }

    public OrderDTO updateOrder(Long id, OrderDTO orderDTO, String updatedBy) {
        Order existingOrder = orderRepository.findById(id)
                .filter(Order::getActive)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        updateOrderFromDTO(existingOrder, orderDTO);
        existingOrder.setUpdatedBy(updatedBy);

        Order savedOrder = orderRepository.save(existingOrder);
        return convertToDTO(savedOrder);
    }

    public OrderDTO updateOrderStatus(Long id, Order.OrderStatus status, String updatedBy) {
        Order order = orderRepository.findById(id)
                .filter(Order::getActive)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        order.setStatus(status);
        order.setUpdatedBy(updatedBy);

        if (status == Order.OrderStatus.DISPATCHED && order.getDispatchDate() == null) {
            order.setDispatchDate(LocalDateTime.now());
        }
        if (status == Order.OrderStatus.DELIVERED && order.getDeliveryActualDate() == null) {
            order.setDeliveryActualDate(LocalDateTime.now());
        }

        Order savedOrder = orderRepository.save(order);
        return convertToDTO(savedOrder);
    }

    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .filter(Order::getActive)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        order.setActive(false);
        orderRepository.save(order);
    }

    public List<OrderDTO> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomerIdAndActiveTrue(customerId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getOrdersByEnquiryId(Long enquiryId) {
        return orderRepository.findByEnquiryIdAndActiveTrue(enquiryId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getOverdueOrders() {
        return orderRepository.findOverdueOrders(LocalDateTime.now())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public long getTotalOrdersCount() {
        return orderRepository.countActiveOrders();
    }

    public long getOrdersCountByStatus(Order.OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    public long getOrdersCountByCustomer(Long customerId) {
        return orderRepository.countByCustomerId(customerId);
    }

    private String generateOrderNumber() {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        long count = orderRepository.count() + 1;
        return String.format("ORD-%s-%04d", datePrefix, count);
    }

    private Order.OrderPriority convertPriority(Enquiry.EnquiryPriority enquiryPriority) {
        switch (enquiryPriority) {
            case LOW:
                return Order.OrderPriority.LOW;
            case MEDIUM:
                return Order.OrderPriority.MEDIUM;
            case HIGH:
                return Order.OrderPriority.HIGH;
            case URGENT:
                return Order.OrderPriority.URGENT;
            default:
                return Order.OrderPriority.MEDIUM;
        }
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setCustomerId(order.getCustomer().getId());
        dto.setCustomerName(order.getCustomer().getCompanyName());

        if (order.getCustomerPersonnel() != null) {
            dto.setCustomerPersonnelId(order.getCustomerPersonnel().getId());
            dto.setCustomerPersonnelName(order.getCustomerPersonnel().getFullName());
        }

        if (order.getEnquiry() != null) {
            dto.setEnquiryId(order.getEnquiry().getId());
            dto.setEnquiryNumber(order.getEnquiry().getEnquiryNumber());
        }

        dto.setDescription(order.getDescription());
        dto.setRequirements(order.getRequirements());
        dto.setStatus(order.getStatus());
        dto.setPriority(order.getPriority());
        dto.setOrderDate(order.getOrderDate());
        dto.setDeliveryExpectedDate(order.getDeliveryExpectedDate());
        dto.setDeliveryActualDate(order.getDeliveryActualDate());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setPurchaseOrderNumber(order.getPurchaseOrderNumber());
        dto.setTransportDetails(order.getTransportDetails());
        dto.setDispatchDate(order.getDispatchDate());
        dto.setActive(order.getActive());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        dto.setCreatedBy(order.getCreatedBy());
        dto.setUpdatedBy(order.getUpdatedBy());

        return dto;
    }

    private Order convertToEntity(OrderDTO dto) {
        Order order = new Order();

        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + dto.getCustomerId()));
        order.setCustomer(customer);

        if (dto.getCustomerPersonnelId() != null) {
            CustomerPersonnel personnel = customerPersonnelRepository.findById(dto.getCustomerPersonnelId())
                    .orElseThrow(() -> new RuntimeException("Customer personnel not found with id: " + dto.getCustomerPersonnelId()));
            order.setCustomerPersonnel(personnel);
        }

        if (dto.getEnquiryId() != null) {
            Enquiry enquiry = enquiryRepository.findById(dto.getEnquiryId())
                    .orElseThrow(() -> new RuntimeException("Enquiry not found with id: " + dto.getEnquiryId()));
            order.setEnquiry(enquiry);
        }

        order.setDescription(dto.getDescription());
        order.setRequirements(dto.getRequirements());
        order.setStatus(dto.getStatus() != null ? dto.getStatus() : Order.OrderStatus.PENDING);
        order.setPriority(dto.getPriority() != null ? dto.getPriority() : Order.OrderPriority.MEDIUM);
        order.setOrderDate(dto.getOrderDate() != null ? dto.getOrderDate() : LocalDateTime.now());
        order.setDeliveryExpectedDate(dto.getDeliveryExpectedDate());
        order.setDeliveryActualDate(dto.getDeliveryActualDate());
        order.setTotalAmount(dto.getTotalAmount());
        order.setPurchaseOrderNumber(dto.getPurchaseOrderNumber());
        order.setTransportDetails(dto.getTransportDetails());
        order.setDispatchDate(dto.getDispatchDate());

        return order;
    }

    private void updateOrderFromDTO(Order order, OrderDTO dto) {
        if (dto.getCustomerId() != null) {
            Customer customer = customerRepository.findById(dto.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found with id: " + dto.getCustomerId()));
            order.setCustomer(customer);
        }

        if (dto.getCustomerPersonnelId() != null) {
            CustomerPersonnel personnel = customerPersonnelRepository.findById(dto.getCustomerPersonnelId())
                    .orElseThrow(() -> new RuntimeException("Customer personnel not found with id: " + dto.getCustomerPersonnelId()));
            order.setCustomerPersonnel(personnel);
        }

        if (dto.getDescription() != null) {
            order.setDescription(dto.getDescription());
        }
        if (dto.getRequirements() != null) {
            order.setRequirements(dto.getRequirements());
        }
        if (dto.getStatus() != null) {
            order.setStatus(dto.getStatus());
        }
        if (dto.getPriority() != null) {
            order.setPriority(dto.getPriority());
        }
        if (dto.getDeliveryExpectedDate() != null) {
            order.setDeliveryExpectedDate(dto.getDeliveryExpectedDate());
        }
        if (dto.getPurchaseOrderNumber() != null) {
            order.setPurchaseOrderNumber(dto.getPurchaseOrderNumber());
        }
        if (dto.getTransportDetails() != null) {
            order.setTransportDetails(dto.getTransportDetails());
        }
    }
}
