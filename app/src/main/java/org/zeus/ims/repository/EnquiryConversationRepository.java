package org.zeus.ims.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zeus.ims.entity.EnquiryConversation;

import java.util.List;

@Repository
public interface EnquiryConversationRepository extends JpaRepository<EnquiryConversation, Long> {

    List<EnquiryConversation> findByEnquiryIdOrderByCreatedAtAsc(Long enquiryId);

    List<EnquiryConversation> findByEnquiryIdOrderByCreatedAtDesc(Long enquiryId);

    @Query("SELECT ec FROM EnquiryConversation ec WHERE ec.enquiry.id = :enquiryId AND ec.direction = 'OUTGOING' ORDER BY ec.createdAt DESC")
    List<EnquiryConversation> findOutgoingMessagesByEnquiryId(@Param("enquiryId") Long enquiryId);

    @Query("SELECT ec FROM EnquiryConversation ec WHERE ec.enquiry.id = :enquiryId AND ec.direction = 'INCOMING' ORDER BY ec.createdAt DESC")
    List<EnquiryConversation> findIncomingMessagesByEnquiryId(@Param("enquiryId") Long enquiryId);

    @Query("SELECT COUNT(ec) FROM EnquiryConversation ec WHERE ec.enquiry.id = :enquiryId")
    long countByEnquiryId(@Param("enquiryId") Long enquiryId);
}
