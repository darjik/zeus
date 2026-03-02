package org.zeus.ims.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.zeus.ims.dto.EnquiryConversationDTO;
import org.zeus.ims.entity.Enquiry;
import org.zeus.ims.entity.EnquiryConversation;
import org.zeus.ims.entity.EnquiryConversationAttachment;
import org.zeus.ims.repository.EnquiryConversationRepository;
import org.zeus.ims.repository.EnquiryConversationAttachmentRepository;
import org.zeus.ims.repository.EnquiryRepository;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EnquiryConversationService {

    private final EnquiryConversationRepository conversationRepository;
    private final EnquiryConversationAttachmentRepository attachmentRepository;
    private final EnquiryRepository enquiryRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public EnquiryConversationService(EnquiryConversationRepository conversationRepository,
                                      EnquiryConversationAttachmentRepository attachmentRepository,
                                      EnquiryRepository enquiryRepository,
                                      FileStorageService fileStorageService) {
        this.conversationRepository = conversationRepository;
        this.attachmentRepository = attachmentRepository;
        this.enquiryRepository = enquiryRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional(readOnly = true)
    public List<EnquiryConversation> getConversationsByEnquiryId(Long enquiryId) {
        return conversationRepository.findByEnquiryIdOrderByCreatedAtAsc(enquiryId);
    }

    @Transactional(readOnly = true)
    public List<EnquiryConversation> getConversationsByEnquiryIdDesc(Long enquiryId) {
        return conversationRepository.findByEnquiryIdOrderByCreatedAtDesc(enquiryId);
    }

    @Transactional(readOnly = true)
    public List<EnquiryConversation> getOutgoingMessagesByEnquiryId(Long enquiryId) {
        return conversationRepository.findOutgoingMessagesByEnquiryId(enquiryId);
    }

    @Transactional(readOnly = true)
    public List<EnquiryConversation> getIncomingMessagesByEnquiryId(Long enquiryId) {
        return conversationRepository.findIncomingMessagesByEnquiryId(enquiryId);
    }

    @Transactional(readOnly = true)
    public long getConversationCount(Long enquiryId) {
        return conversationRepository.countByEnquiryId(enquiryId);
    }

    @Transactional(readOnly = true)
    public Optional<EnquiryConversation> getConversationById(Long id) {
        return conversationRepository.findById(id);
    }

    public EnquiryConversation addConversation(EnquiryConversationDTO conversationDTO) {
        Enquiry enquiry = enquiryRepository.findByIdAndActiveTrue(conversationDTO.getEnquiryId())
                .orElseThrow(() -> new IllegalArgumentException("Enquiry not found"));

        EnquiryConversation conversation = new EnquiryConversation();
        conversation.setEnquiry(enquiry);
        conversation.setMessage(conversationDTO.getMessage());
        conversation.setMessageType(EnquiryConversation.MessageType.valueOf(conversationDTO.getMessageType()));
        conversation.setDirection(EnquiryConversation.MessageDirection.valueOf(conversationDTO.getDirection()));
        conversation.setSenderName(conversationDTO.getSenderName());
        conversation.setSenderEmail(conversationDTO.getSenderEmail());
        conversation.setCreatedBy(getCurrentUsername());

        return conversationRepository.save(conversation);
    }

    public EnquiryConversation addConversationWithAttachments(EnquiryConversationDTO conversationDTO,
                                                              List<MultipartFile> attachments) throws IOException {
        EnquiryConversation conversation = addConversation(conversationDTO);

        if (attachments != null && !attachments.isEmpty()) {
            processAttachments(conversation, attachments);
        }

        return conversation;
    }

    public EnquiryConversation updateConversation(Long id, EnquiryConversationDTO conversationDTO) {
        EnquiryConversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        if (conversationDTO.getMessage() != null) {
            conversation.setMessage(conversationDTO.getMessage());
        }
        if (conversationDTO.getMessageType() != null) {
            conversation.setMessageType(EnquiryConversation.MessageType.valueOf(conversationDTO.getMessageType()));
        }
        if (conversationDTO.getDirection() != null) {
            conversation.setDirection(EnquiryConversation.MessageDirection.valueOf(conversationDTO.getDirection()));
        }
        if (conversationDTO.getSenderName() != null) {
            conversation.setSenderName(conversationDTO.getSenderName());
        }
        if (conversationDTO.getSenderEmail() != null) {
            conversation.setSenderEmail(conversationDTO.getSenderEmail());
        }

        return conversationRepository.save(conversation);
    }

    public void deleteConversation(Long id) {
        EnquiryConversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        // Delete associated files
        List<EnquiryConversationAttachment> attachments = attachmentRepository.findByConversationIdOrderByCreatedAtAsc(id);
        for (EnquiryConversationAttachment attachment : attachments) {
            try {
                fileStorageService.deleteFile(attachment.getFilePath());
            } catch (IOException ex) {
                // Log the error but don't fail the deletion
                System.err.println("Failed to delete file: " + attachment.getFilePath() + ", Error: " + ex.getMessage());
            }
        }

        conversationRepository.delete(conversation);
    }

    public void deleteAttachment(Long attachmentId) {
        EnquiryConversationAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("Attachment not found"));

        try {
            fileStorageService.deleteFile(attachment.getFilePath());
        } catch (IOException ex) {
            // Log the error but don't fail the deletion
            System.err.println("Failed to delete file: " + attachment.getFilePath() + ", Error: " + ex.getMessage());
        }
        attachmentRepository.delete(attachment);
    }

    @Transactional(readOnly = true)
    public List<EnquiryConversationAttachment> getAttachmentsByConversationId(Long conversationId) {
        return attachmentRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    public EnquiryConversationDTO convertToDTO(EnquiryConversation conversation) {
        EnquiryConversationDTO dto = new EnquiryConversationDTO();
        dto.setId(conversation.getId());
        dto.setEnquiryId(conversation.getEnquiry().getId());
        dto.setMessage(conversation.getMessage());
        dto.setMessageType(conversation.getMessageType().name());
        dto.setDirection(conversation.getDirection().name());
        dto.setSenderName(conversation.getSenderName());
        dto.setSenderEmail(conversation.getSenderEmail());
        return dto;
    }

    private void processAttachments(EnquiryConversation conversation, List<MultipartFile> attachments) throws IOException {
        String currentUser = getCurrentUsername();

        for (MultipartFile file : attachments) {
            if (!file.isEmpty()) {
                if (!fileStorageService.isValidFileSize(file)) {
                    throw new IOException("File " + file.getOriginalFilename() + " exceeds maximum size of 5MB");
                }

                String filePath = fileStorageService.storeFile(file,
                        conversation.getEnquiry().getId(), conversation.getId());

                EnquiryConversationAttachment attachment = new EnquiryConversationAttachment(
                        conversation,
                        file.getOriginalFilename(),
                        Paths.get(filePath).getFileName().toString(),
                        filePath,
                        file.getSize(),
                        file.getContentType()
                );
                attachment.setCreatedBy(currentUser);

                attachmentRepository.save(attachment);
            }
        }
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }
}
