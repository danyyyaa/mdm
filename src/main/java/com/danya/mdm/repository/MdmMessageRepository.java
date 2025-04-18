package com.danya.mdm.repository;

import com.danya.mdm.model.MdmMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MdmMessageRepository extends JpaRepository<MdmMessage, UUID> {
}
