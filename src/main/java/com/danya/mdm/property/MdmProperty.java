package com.danya.mdm.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("mdm")
public record MdmProperty(
        ProcessingPool processingPool,
        Scheduler scheduler,
        Metrics metrics
) {

    public record ProcessingPool(
            ServiceConfig serviceOne,
            ServiceConfig serviceTwo
    ) {
        public record ServiceConfig(int threads, int queueDepth) {
        }
    }

    public record Scheduler(
            RetrySendMessagesJob retrySendMessagesJob,
            DeleteOldMessagesJob deleteOldMessagesJob
    ) {
        public record RetrySendMessagesJob(
                int lookbackHours,
                int lagMinutes,
                int pageSize
        ) {
        }

        public record DeleteOldMessagesJob(
                int thresholdWeeks
        ) {
        }
    }

    public record Metrics(
            UndeliveredEvents undeliveredEvents
    ) {
        public record UndeliveredEvents(
                String name,
                Long ttlMillis
        ) {

        }
    }
}
