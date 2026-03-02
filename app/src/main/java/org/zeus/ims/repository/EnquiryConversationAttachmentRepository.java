package org.zeus.ims.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zeus.ims.entity.EnquiryConversationAttachment;

import java.util.List;

@Repository
public interface EnquiryConversationAttachmentRepository extends JpaRepository<EnquiryConversationAttachment, Long> {

    List<EnquiryConversationAttachment> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

    @Query("SELECT COUNT(a) FROM EnquiryConversationAttachment a WHERE a.conversation.id = :conversationId")
    long countByConversationId(@Param("conversationId") Long conversationId);

    @Query("SELECT SUM(a.fileSize) FROM EnquiryConversationAttachment a WHERE a.conversation.id = :conversationId")
    Long sumFileSizeByConversationId(@Param("conversationId") Long conversationId);

    void deleteByConversationId(Long conversationId);
}
