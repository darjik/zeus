package org.zeus.ims.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zeus.ims.entity.EnquiryItem;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface EnquiryItemRepository extends JpaRepository<EnquiryItem, Long> {

    List<EnquiryItem> findByEnquiryIdOrderByCreatedAtAsc(Long enquiryId);

    List<EnquiryItem> findByEnquiryId(Long enquiryId);

    List<EnquiryItem> findByProductId(Long productId);

    @Query("SELECT SUM(ei.totalAmount) FROM EnquiryItem ei WHERE ei.enquiry.id = :enquiryId")
    BigDecimal calculateTotalAmountForEnquiry(@Param("enquiryId") Long enquiryId);

    @Query("SELECT COUNT(ei) FROM EnquiryItem ei WHERE ei.enquiry.id = :enquiryId")
    long countByEnquiryId(@Param("enquiryId") Long enquiryId);

    void deleteByEnquiryId(Long enquiryId);
}
