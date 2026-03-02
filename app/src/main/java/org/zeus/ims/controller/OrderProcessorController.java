package org.zeus.ims.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.zeus.ims.dto.OrderTaskDTO;
import org.zeus.ims.dto.OrderTaskDocumentDTO;
import org.zeus.ims.entity.OrderTask;
import org.zeus.ims.entity.OrderTaskDocument;
import org.zeus.ims.service.OrderTaskService;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/orders/{orderId}/tasks")
@PreAuthorize("hasAnyAuthority('OWNER', 'SALES', 'PRODUCTION_MANAGER', 'WORKSHOP_PERSONNEL')")
public class OrderProcessorController {

    private final OrderTaskService orderTaskService;

    @Autowired
    public OrderProcessorController(OrderTaskService orderTaskService) {
        this.orderTaskService = orderTaskService;
    }

    @GetMapping
    public String listTasks(@PathVariable Long orderId, Model model, Authentication authentication) {
        List<OrderTaskDTO> tasks = orderTaskService.getTasksByOrderId(orderId);

        // Add read-only mode for production users
        String userRole = authentication.getAuthorities().iterator().next().getAuthority();
        boolean isReadOnly = "WORKSHOP_PERSONNEL".equals(userRole);

        model.addAttribute("orderId", orderId);
        model.addAttribute("tasks", tasks);
        model.addAttribute("taskTypes", OrderTask.TaskType.values());
        model.addAttribute("taskStatuses", OrderTask.TaskStatus.values());
        model.addAttribute("documentTypes", OrderTaskDocument.DocumentType.values());
        model.addAttribute("readOnlyMode", isReadOnly);
        System.out.println(System.lineSeparator());
        return "orders/tasks/list";
    }

    @GetMapping("/{taskId}")
    public String viewTask(@PathVariable Long orderId, @PathVariable Long taskId,
                          Model model, Authentication authentication) {
        Optional<OrderTaskDTO> taskOpt = orderTaskService.getTaskById(taskId);
        if (!taskOpt.isPresent()) {
            return "redirect:/orders/" + orderId + "/tasks?error=Task not found";
        }

        OrderTaskDTO task = taskOpt.get();
        List<OrderTaskDocumentDTO> documents = orderTaskService.getDocumentsByTaskId(taskId);

        // Add read-only mode for production users
        String userRole = authentication.getAuthorities().iterator().next().getAuthority();
        boolean isReadOnly = "WORKSHOP_PERSONNEL".equals(userRole);

        model.addAttribute("orderId", orderId);
        model.addAttribute("task", task);
        model.addAttribute("documents", documents);
        model.addAttribute("taskStatuses", OrderTask.TaskStatus.values());
        model.addAttribute("documentTypes", OrderTaskDocument.DocumentType.values());
        model.addAttribute("readOnlyMode", isReadOnly);

        return "orders/tasks/view";
    }

    @PostMapping("/{taskId}/update")
    @PreAuthorize("hasAnyAuthority('OWNER', 'SALES', 'PRODUCTION_MANAGER')")
    public String updateTask(@PathVariable Long orderId, @PathVariable Long taskId,
                            @ModelAttribute OrderTaskDTO taskDTO,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            orderTaskService.updateTask(taskId, taskDTO, username);
            redirectAttributes.addFlashAttribute("success", "Task updated successfully");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Failed to update task: " + ex.getMessage());
        }
        return "redirect:/orders/" + orderId + "/tasks/" + taskId;
    }

    @PostMapping("/{taskId}/status")
    @PreAuthorize("hasAnyAuthority('OWNER', 'SALES', 'PRODUCTION_MANAGER')")
    public String updateTaskStatus(@PathVariable Long orderId, @PathVariable Long taskId,
                                  @RequestParam OrderTask.TaskStatus status,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            orderTaskService.updateTaskStatus(taskId, status, username);
            redirectAttributes.addFlashAttribute("success", "Task status updated successfully");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Failed to update task status: " + ex.getMessage());
        }
        return "redirect:/orders/" + orderId + "/tasks/" + taskId;
    }

    @PostMapping("/{taskId}/documents")
    @PreAuthorize("hasAnyAuthority('OWNER', 'SALES', 'PRODUCTION_MANAGER')")
    public String uploadDocument(@PathVariable Long orderId, @PathVariable Long taskId,
                                @RequestParam("file") MultipartFile file,
                                @RequestParam OrderTaskDocument.DocumentType documentType,
                                @RequestParam(required = false) String description,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please select a file to upload");
                return "redirect:/orders/" + orderId + "/tasks/" + taskId;
            }

            String username = authentication.getName();
            orderTaskService.uploadDocument(taskId, file, documentType, description, username);
            redirectAttributes.addFlashAttribute("success", "Document uploaded successfully");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload document: " + ex.getMessage());
        }
        return "redirect:/orders/" + orderId + "/tasks/" + taskId;
    }

    @GetMapping("/{taskId}/documents/{documentId}/download")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long orderId,
                                                  @PathVariable Long taskId,
                                                  @PathVariable Long documentId) {
        try {
            Optional<OrderTaskDocumentDTO> documentOpt = orderTaskService.getDocumentById(documentId);
            if (!documentOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            OrderTaskDocumentDTO document = documentOpt.get();

            // Verify the document belongs to the specified task
            if (!document.getOrderTaskId().equals(taskId)) {
                return ResponseEntity.notFound().build();
            }

            byte[] fileContent = orderTaskService.downloadDocument(documentId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", document.getOriginalFilename());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileContent);
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{taskId}/documents/{documentId}/delete")
    @PreAuthorize("hasAnyAuthority('OWNER', 'SALES', 'PRODUCTION_MANAGER')")
    public String deleteDocument(@PathVariable Long orderId, @PathVariable Long taskId,
                                @PathVariable Long documentId,
                                RedirectAttributes redirectAttributes) {
        try {
            orderTaskService.deleteDocument(documentId);
            redirectAttributes.addFlashAttribute("success", "Document deleted successfully");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete document: " + ex.getMessage());
        }
        return "redirect:/orders/" + orderId + "/tasks/" + taskId;
    }

    @PostMapping("/create-default")
    @PreAuthorize("hasAnyAuthority('OWNER', 'SALES', 'PRODUCTION_MANAGER')")
    public String createDefaultTasks(@PathVariable Long orderId,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            orderTaskService.createDefaultTasksForOrder(orderId, username);
            redirectAttributes.addFlashAttribute("success", "Default tasks created successfully");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Failed to create default tasks: " + ex.getMessage());
        }
        return "redirect:/orders/" + orderId + "/tasks";
    }
}
