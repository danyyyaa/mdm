package com.danya.mdm.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("mdm")
public record MdmProperty(
        ProcessingPool processingPool,
        Scheduler scheduler
) {

    public record ProcessingPool(
            ServiceConfig serviceOne,
            ServiceConfig serviceTwo
    ) {
        public record ServiceConfig(int threads, int queueDepth) {
        }
    }

    public record Scheduler(
            RetrySendMessagesJob retrySendMessagesJob
    ) {
        public record RetrySendMessagesJob(
                int lookbackHours,
                int lagMinutes,
                int pageSize
        ) {
        }
    }
}
