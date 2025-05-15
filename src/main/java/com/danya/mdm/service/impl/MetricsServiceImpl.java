package com.danya.mdm.service.impl;

import com.danya.mdm.property.MdmProperty;
import com.danya.mdm.repository.MdmMessageOutboxRepository;
import com.danya.mdm.service.MetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.danya.mdm.enums.MdmDeliveryStatus.ERROR;
import static com.danya.mdm.enums.MdmDeliveryStatus.FATAL_ERROR;

@Service
@RequiredArgsConstructor
public class MetricsServiceImpl implements MetricsService {

    private final MdmMessageOutboxRepository repository;
    private final MdmProperty mdmProperty;

    private final AtomicReference<Cache> cacheRef = new AtomicReference<>(new Cache(0, 0));

    @Override
    public Long countUndelivered() {
        long now = System.currentTimeMillis();
        Cache current = cacheRef.get();

        if (now - current.timestampMillis >= mdmProperty.metrics().undeliveredEvents().ttlMillis()) {
            long newCount = repository.countByStatusIn(List.of(ERROR, FATAL_ERROR));
            Cache updated = new Cache(now, newCount);
            cacheRef.compareAndSet(current, updated);
        }

        return cacheRef.get().value;
    }

    private record Cache(long timestampMillis, long value) {
    }
}

