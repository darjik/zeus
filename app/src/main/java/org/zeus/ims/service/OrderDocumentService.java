package org.zeus.ims.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.zeus.ims.dto.OrderDocumentDTO;
import org.zeus.ims.entity.Order;
import org.zeus.ims.entity.OrderDocument;
import org.zeus.ims.repository.OrderDocumentRepository;
import org.zeus.ims.repository.OrderRepository;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderDocumentService {

    private final OrderDocumentRepository orderDocumentRepository;
    private final OrderRepository orderRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public OrderDocumentService(OrderDocumentRepository orderDocumentRepository,
                               OrderRepository orderRepository,
                               FileStorageService fileStorageService) {
        this.orderDocumentRepository = orderDocumentRepository;
        this.orderRepository = orderRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<OrderDocumentDTO> getDocumentsByOrderId(Long orderId) {
        return orderDocumentRepository.findByOrderIdOrderByCreatedAtDesc(orderId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDocumentDTO> getDocumentsByOrderIdAndType(Long orderId, OrderDocument.DocumentType documentType) {
        return orderDocumentRepository.findByOrderIdAndDocumentType(orderId, documentType)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<OrderDocumentDTO> getDocumentById(Long id) {
        return orderDocumentRepository.findById(id)
                .map(this::convertToDTO);
    }

    public OrderDocumentDTO uploadDocument(Long orderId, MultipartFile file, OrderDocument.DocumentType documentType,
                                          String description, String uploadedBy) throws IOException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        String subDirectory = "order-documents/" + orderId;
        String filePath = fileStorageService.storeFile(file, subDirectory);

        OrderDocument document = new OrderDocument();
        document.setOrder(order);
        document.setDocumentName(file.getOriginalFilename());
        document.setOriginalFilename(file.getOriginalFilename());
        document.setFilePath(filePath);
        document.setFileSize(file.getSize());
        document.setContentType(file.getContentType());
        document.setDocumentType(documentType);
        document.setDescription(description);
        document.setUploadedBy(uploadedBy);

        OrderDocument savedDocument = orderDocumentRepository.save(document);
        return convertToDTO(savedDocument);
    }

    public OrderDocumentDTO updateDocument(Long id, OrderDocumentDTO documentDTO) {
        OrderDocument existingDocument = orderDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order document not found with id: " + id));

        updateDocumentFromDTO(existingDocument, documentDTO);
        OrderDocument savedDocument = orderDocumentRepository.save(existingDocument);
        return convertToDTO(savedDocument);
    }

    public void deleteDocument(Long id) {
        OrderDocument document = orderDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order document not found with id: " + id));

        try {
            fileStorageService.deleteFile(document.getFilePath());
        } catch (Exception ex) {
            // Log the error but don't fail the deletion
            System.err.println("Failed to delete file: " + document.getFilePath() + ", Error: " + ex.getMessage());
        }

        orderDocumentRepository.deleteById(id);
    }

    public byte[] downloadDocument(Long id) throws IOException {
        OrderDocument document = orderDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order document not found with id: " + id));

        return fileStorageService.loadFileAsBytes(document.getFilePath());
    }

    public long getDocumentsCountByOrderId(Long orderId) {
        return orderDocumentRepository.countByOrderId(orderId);
    }

    public long getDocumentsCountByOrderIdAndType(Long orderId, OrderDocument.DocumentType documentType) {
        return orderDocumentRepository.countByOrderIdAndDocumentType(orderId, documentType);
    }

    private OrderDocumentDTO convertToDTO(OrderDocument document) {
        OrderDocumentDTO dto = new OrderDocumentDTO();
        dto.setId(document.getId());
        dto.setOrderId(document.getOrder().getId());
        dto.setDocumentName(document.getDocumentName());
        dto.setOriginalFilename(document.getOriginalFilename());
        dto.setFilePath(document.getFilePath());
        dto.setFileSize(document.getFileSize());
        dto.setContentType(document.getContentType());
        dto.setDocumentType(document.getDocumentType());
        dto.setDescription(document.getDescription());
        dto.setUploadedBy(document.getUploadedBy());
        dto.setCreatedAt(document.getCreatedAt());
        dto.setUpdatedAt(document.getUpdatedAt());
        return dto;
    }

    private void updateDocumentFromDTO(OrderDocument document, OrderDocumentDTO dto) {
        if (dto.getDocumentName() != null) {
            document.setDocumentName(dto.getDocumentName());
        }
        if (dto.getDocumentType() != null) {
            document.setDocumentType(dto.getDocumentType());
        }
        if (dto.getDescription() != null) {
            document.setDescription(dto.getDescription());
        }
    }
}
