package com.danya.mdm.service;

import com.danya.mdm.dto.ChangePhoneDto;
import com.danya.mdm.model.MdmMessageOutbox;

import java.util.List;

public interface MessageProcessingService {

    void process(ChangePhoneDto dto);
    void process(List<MdmMessageOutbox> batch);
}
