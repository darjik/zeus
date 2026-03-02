package org.zeus.ims.dto;

import org.zeus.ims.entity.OrderTaskDocument;
import java.time.LocalDateTime;

public class OrderTaskDocumentDTO {

    private Long id;
    private Long orderTaskId;
    private String documentName;
    private String originalFilename;
    private String filePath;
    private Long fileSize;
    private String contentType;
    private String documentType;
    private String description;
    private String uploadedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OrderTaskDocumentDTO() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderTaskId() {
        return orderTaskId;
    }

    public void setOrderTaskId(Long orderTaskId) {
        this.orderTaskId = orderTaskId;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods
    public OrderTaskDocument.DocumentType getDocumentTypeEnum() {
        return documentType != null ? OrderTaskDocument.DocumentType.valueOf(documentType) : null;
    }

    public String getDocumentTypeDisplayName() {
        return getDocumentTypeEnum() != null ? getDocumentTypeEnum().getDisplayName() : null;
    }

    public String getFormattedFileSize() {
        if (fileSize == null) {
            return "Unknown";
        }

        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        }
    }
}
