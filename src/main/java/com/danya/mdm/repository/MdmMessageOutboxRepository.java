package com.danya.mdm.repository;

import com.danya.mdm.dto.ResponseDataDto;
import com.danya.mdm.enums.MdmDeliveryStatus;
import com.danya.mdm.model.MdmMessageOutbox;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface MdmMessageOutboxRepository extends JpaRepository<MdmMessageOutbox, UUID> {

    @Modifying
    @Transactional
    @Query("""
            UPDATE MdmMessageOutbox m 
               SET m.status       = :status,
                   m.responseData = :responseData
             WHERE m.mdmMessageId = :id
            """)
    void updateDeliveryStatusById(
            @Param("id") UUID id,
            @Param("status") MdmDeliveryStatus status,
            @Param("responseData") ResponseDataDto responseDataDto
    );

    @Query("""
            SELECT m
              FROM MdmMessageOutbox m
             WHERE m.lastUpdateTime  > :from
               AND m.lastUpdateTime  < :to
               AND m.status     IN :statuses
             ORDER BY m.lastUpdateTime ASC
            """)
    List<MdmMessageOutbox> fetchBatch(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("statuses") Collection<MdmDeliveryStatus> statuses,
            Pageable pageable
    );

    List<MdmMessageOutbox> findByMdmMessageIdAndStatusIn(
            UUID mdmMessageId,
            Collection<MdmDeliveryStatus> statuses
    );
}
