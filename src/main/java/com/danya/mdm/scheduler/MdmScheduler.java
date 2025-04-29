package com.danya.mdm.scheduler;

import com.danya.mdm.enums.MdmDeliveryStatus;
import com.danya.mdm.model.MdmMessageOutbox;
import com.danya.mdm.property.MdmProperty;
import com.danya.mdm.repository.MdmMessageOutboxRepository;
import com.danya.mdm.service.impl.MessageProcessingServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MdmScheduler {

    private final MessageProcessingServiceImpl messageProcessingService;
    private final MdmMessageOutboxRepository outboxRepository;
    private final MdmProperty property;

    @Async("retrySendMessagesExecutor")
    @Scheduled(cron = "0 0/5 * * * *")
    public void retrySendMessagesJob() {
        log.info("Старт джобы: retrySendMessagesJob");

        var job = property.scheduler().retrySendMessagesJob();
        LocalDateTime now  = LocalDateTime.now();
        LocalDateTime from = now.minusHours(job.lookbackHours());
        LocalDateTime to   = now.minusMinutes(job.lagMinutes());

        Pageable page = PageRequest.of(
                0,
                job.pageSize(),
                Sort.by("lastUpdateTime").ascending()
        );

        List<MdmMessageOutbox> batch = outboxRepository.fetchBatch(
                from, to, List.of(MdmDeliveryStatus.NEW, MdmDeliveryStatus.ERROR), page
        );

        messageProcessingService.process(batch);
        log.info("Финиш джобы: retrySendMessagesJob");
    }

}