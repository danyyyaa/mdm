package com.danya.mdm.service.impl;

import com.danya.mdm.repository.MdmMessageOutboxRepository;
import com.danya.mdm.service.MetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.danya.mdm.enums.MdmDeliveryStatus.ERROR;
import static com.danya.mdm.enums.MdmDeliveryStatus.FATAL_ERROR;

@Service
@RequiredArgsConstructor
public class MetricsServiceImpl implements MetricsService {

    private final MdmMessageOutboxRepository mdmMessageOutboxRepository;

    @Override
//    @Cacheable(cacheNames = "${spring.cache.cache-names}")
    public Long countUndelivered() {
        return mdmMessageOutboxRepository.countByStatusIn(List.of(ERROR, FATAL_ERROR));
    }
}
