package com.danya.mdm.service;

import com.danya.mdm.dto.ChangePhoneDto;

public interface MessageProcessingService {

    void process(ChangePhoneDto dto);
}
