package com.danya.mdm.scheduler;

import com.danya.mdm.dto.ChangePhoneDto;
import com.danya.mdm.enums.MdmDeliveryStatus;
import com.danya.mdm.model.MdmMessage;
import com.danya.mdm.model.MdmMessageOutbox;
import com.danya.mdm.property.MdmProperty;
import com.danya.mdm.repository.MdmMessageOutboxRepository;
import com.danya.mdm.repository.MdmMessageRepository;
import com.danya.mdm.service.MessageProcessingService;
import com.danya.mdm.util.JsonUtil;
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

    private final MessageProcessingService messageProcessingService;
    private final MdmMessageOutboxRepository outboxRepository;
    private final MdmMessageRepository messageRepository;
    private final JsonUtil jsonUtil;
    private final MdmProperty property;

    @Async("retrySendMessagesExecutor")
    @Scheduled(cron = "0 0/5 * * * *")
    public void retrySendMessagesJob() {
        log.info("Старт джобы: retrySendMessagesJob");

        var job = property.scheduler().retrySendMessagesJob();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.minusHours(job.lookbackHours());
        LocalDateTime to = now.minusMinutes(job.lagMinutes());

        Sort sort = Sort.by("lastUpdateTime").ascending();
        int pageSize = job.pageSize();
        int pageIndex = 0;

        List<MdmMessageOutbox> batch =
                fetchBatch(from, to, PageRequest.of(pageIndex, pageSize, sort));

        while (!batch.isEmpty()) {
            log.info("Обработка страницы {} ({} сообщений)", pageIndex, batch.size());

            processBatch(batch);

            if (batch.size() < pageSize) {
                log.info("Обработана последняя неполная страница");
                break;
            }

            pageIndex++;
            batch = fetchBatch(from, to, PageRequest.of(pageIndex, pageSize, sort));
        }

        log.info("Финиш джобы: retrySendMessagesJob");
    }

    private List<MdmMessageOutbox> fetchBatch(LocalDateTime from, LocalDateTime to, Pageable page) {
        return outboxRepository.fetchBatch(
                from,
                to,
                List.of(MdmDeliveryStatus.NEW, MdmDeliveryStatus.ERROR),
                page
        );
    }

    private void processBatch(List<MdmMessageOutbox> batch) {
        batch.forEach(outbox -> {
            try {
                MdmMessage msg = messageRepository.findByExternalId(outbox.getMdmMessageId())
                        .orElseThrow(() -> new IllegalStateException(
                                "Message not found for externalId: " + outbox.getMdmMessageId()));

                ChangePhoneDto dto = jsonUtil.fromJson(msg.getPayload(), ChangePhoneDto.class);
                messageProcessingService.process(dto);

            } catch (Exception e) {
                log.error("Ошибка при обработке сообщения с id {}: {}", outbox.getMdmMessageId(), e.getMessage(), e);
            }
        });
    }
}
