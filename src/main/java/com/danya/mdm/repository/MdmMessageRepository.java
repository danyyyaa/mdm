package com.danya.mdm.repository;

import com.danya.mdm.model.MdmMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface MdmMessageRepository extends JpaRepository<MdmMessage, UUID> {

    Optional<MdmMessage> findByExternalId(UUID externalId);

    @Modifying
    @Transactional
    @Query("""
            DELETE FROM MdmMessage m
             WHERE m.externalId IN :ids
            """)
    void deleteByExternalIdIn(@Param("ids") Collection<UUID> ids);
}
