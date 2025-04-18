package com.danya.mdm.repository;

import com.danya.mdm.model.MdmMessageOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MdmMessageOutboxRepository extends JpaRepository<MdmMessageOutbox, UUID> {
}
