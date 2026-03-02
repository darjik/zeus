package org.zeus.ims.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.zeus.ims.dto.OrderTaskDTO;
import org.zeus.ims.dto.OrderTaskDocumentDTO;
import org.zeus.ims.entity.Order;
import org.zeus.ims.entity.OrderTask;
import org.zeus.ims.entity.OrderTaskDocument;
import org.zeus.ims.repository.OrderRepository;
import org.zeus.ims.repository.OrderTaskRepository;
import org.zeus.ims.repository.OrderTaskDocumentRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderTaskService {

    private final OrderTaskRepository orderTaskRepository;
    private final OrderTaskDocumentRepository orderTaskDocumentRepository;
    private final OrderRepository orderRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public OrderTaskService(OrderTaskRepository orderTaskRepository,
                           OrderTaskDocumentRepository orderTaskDocumentRepository,
                           OrderRepository orderRepository,
                           FileStorageService fileStorageService) {
        this.orderTaskRepository = orderTaskRepository;
        this.orderTaskDocumentRepository = orderTaskDocumentRepository;
        this.orderRepository = orderRepository;
        this.fileStorageService = fileStorageService;
    }

    public void createDefaultTasksForOrder(Long orderId, String createdBy) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        // Create the 4 default tasks
        OrderTask.TaskType[] taskTypes = {
            OrderTask.TaskType.INTERNAL_MEETING,
            OrderTask.TaskType.DESIGNING,
            OrderTask.TaskType.PART_LIST,
            OrderTask.TaskType.INVENTORY_STORE_CHECK
        };

        for (OrderTask.TaskType taskType : taskTypes) {
            // Check if task already exists
            Optional<OrderTask> existingTask = orderTaskRepository.findByOrderIdAndTaskType(orderId, taskType);
            if (!existingTask.isPresent()) {
                OrderTask task = new OrderTask();
                task.setOrder(order);
                task.setTaskType(taskType);
                task.setStatus(OrderTask.TaskStatus.PENDING);
                task.setCreatedBy(createdBy);
                task.setUpdatedBy(createdBy);
                orderTaskRepository.save(task);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<OrderTaskDTO> getTasksByOrderId(Long orderId) {
        return orderTaskRepository.findByOrderIdOrderByTaskTypeAsc(orderId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<OrderTaskDTO> getTaskById(Long id) {
        return orderTaskRepository.findById(id)
                .map(this::convertToDTO);
    }

    public OrderTaskDTO updateTask(Long id, OrderTaskDTO taskDTO, String updatedBy) {
        OrderTask task = orderTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order task not found with id: " + id));

        updateTaskFromDTO(task, taskDTO);
        task.setUpdatedBy(updatedBy);

        if (taskDTO.getStatus() != null && OrderTask.TaskStatus.COMPLETED.name().equals(taskDTO.getStatus())) {
            task.setCompletedDate(LocalDateTime.now());
        }

        OrderTask savedTask = orderTaskRepository.save(task);
        return convertToDTO(savedTask);
    }

    public OrderTaskDTO updateTaskStatus(Long id, OrderTask.TaskStatus status, String updatedBy) {
        OrderTask task = orderTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order task not found with id: " + id));

        task.setStatus(status);
        task.setUpdatedBy(updatedBy);

        if (status == OrderTask.TaskStatus.COMPLETED) {
            task.setCompletedDate(LocalDateTime.now());
        }

        OrderTask savedTask = orderTaskRepository.save(task);
        return convertToDTO(savedTask);
    }

    public OrderTaskDocumentDTO uploadDocument(Long taskId, MultipartFile file,
                                             OrderTaskDocument.DocumentType documentType,
                                             String description, String uploadedBy) throws IOException {
        OrderTask task = orderTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Order task not found with id: " + taskId));

        String subDirectory = "order-task-documents/" + task.getOrder().getId() + "/" + taskId;
        String filePath = fileStorageService.storeFile(file, subDirectory);

        OrderTaskDocument document = new OrderTaskDocument();
        document.setOrderTask(task);
        document.setDocumentName(file.getOriginalFilename());
        document.setOriginalFilename(file.getOriginalFilename());
        document.setFilePath(filePath);
        document.setFileSize(file.getSize());
        document.setContentType(file.getContentType());
        document.setDocumentType(documentType);
        document.setDescription(description);
        document.setUploadedBy(uploadedBy);

        OrderTaskDocument savedDocument = orderTaskDocumentRepository.save(document);
        return convertDocumentToDTO(savedDocument);
    }

    public byte[] downloadDocument(Long documentId) throws IOException {
        OrderTaskDocument document = orderTaskDocumentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));

        return fileStorageService.loadFileAsBytes(document.getFilePath());
    }

    public void deleteDocument(Long documentId) {
        OrderTaskDocument document = orderTaskDocumentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));

        try {
            fileStorageService.deleteFile(document.getFilePath());
        } catch (IOException ex) {
            System.err.println("Failed to delete file: " + document.getFilePath() + ", Error: " + ex.getMessage());
        }

        orderTaskDocumentRepository.deleteById(documentId);
    }

    @Transactional(readOnly = true)
    public List<OrderTaskDocumentDTO> getDocumentsByTaskId(Long taskId) {
        return orderTaskDocumentRepository.findByOrderTaskIdOrderByCreatedAtDesc(taskId)
                .stream()
                .map(this::convertDocumentToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<OrderTaskDocumentDTO> getDocumentById(Long id) {
        return orderTaskDocumentRepository.findById(id)
                .map(this::convertDocumentToDTO);
    }

    @Transactional(readOnly = true)
    public List<OrderTaskDTO> getTasksByResponsiblePerson(String responsiblePerson) {
        return orderTaskRepository.findByResponsiblePersonOrderByCreatedAtDesc(responsiblePerson)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderTaskDTO> getPendingTasks() {
        return orderTaskRepository.findByTaskTypeAndStatusOrderByCreatedAtDesc(null, OrderTask.TaskStatus.PENDING)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private OrderTaskDTO convertToDTO(OrderTask task) {
        OrderTaskDTO dto = new OrderTaskDTO();
        dto.setId(task.getId());
        dto.setOrderId(task.getOrder().getId());
        dto.setOrderNumber(task.getOrder().getOrderNumber());
        dto.setTaskType(task.getTaskType().name());
        dto.setStatus(task.getStatus().name());
        dto.setDescription(task.getDescription());
        dto.setResponsiblePerson(task.getResponsiblePerson());
        dto.setAssignedTo(task.getAssignedTo());
        dto.setDueDate(task.getDueDate());
        dto.setCompletedDate(task.getCompletedDate());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        dto.setCreatedBy(task.getCreatedBy());
        dto.setUpdatedBy(task.getUpdatedBy());

        // Set document count
        dto.setDocumentCount((int) orderTaskDocumentRepository.countByOrderTaskId(task.getId()));

        return dto;
    }

    private OrderTaskDocumentDTO convertDocumentToDTO(OrderTaskDocument document) {
        OrderTaskDocumentDTO dto = new OrderTaskDocumentDTO();
        dto.setId(document.getId());
        dto.setOrderTaskId(document.getOrderTask().getId());
        dto.setDocumentName(document.getDocumentName());
        dto.setOriginalFilename(document.getOriginalFilename());
        dto.setFilePath(document.getFilePath());
        dto.setFileSize(document.getFileSize());
        dto.setContentType(document.getContentType());
        dto.setDocumentType(document.getDocumentType() != null ? document.getDocumentType().name() : null);
        dto.setDescription(document.getDescription());
        dto.setUploadedBy(document.getUploadedBy());
        dto.setCreatedAt(document.getCreatedAt());
        dto.setUpdatedAt(document.getUpdatedAt());
        return dto;
    }

    private void updateTaskFromDTO(OrderTask task, OrderTaskDTO dto) {
        if (dto.getStatus() != null) {
            task.setStatus(OrderTask.TaskStatus.valueOf(dto.getStatus()));
        }
        if (dto.getDescription() != null) {
            task.setDescription(dto.getDescription());
        }
        if (dto.getResponsiblePerson() != null) {
            task.setResponsiblePerson(dto.getResponsiblePerson());
        }
        if (dto.getAssignedTo() != null) {
            task.setAssignedTo(dto.getAssignedTo());
        }
        if (dto.getDueDate() != null) {
            task.setDueDate(dto.getDueDate());
        }
    }
}
