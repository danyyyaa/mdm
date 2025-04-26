package com.danya.mdm.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("mdm")
public record MdmProperty(
        Executor executor,
        Scheduler scheduler
) {
    public record Executor(
            ServiceConfig serviceOne,
            ServiceConfig serviceTwo
    ) {
        public record ServiceConfig(int threads, int queueDepth) {
        }
    }
    public record Scheduler(
            RetrySendMessagesJob retrySendMessagesJob,
            Lookback lookback,
            Page page
    ) {
        public record RetrySendMessagesJob(String cron) {}
        public record Lookback(int hours, int minutes) {}
        public record Page(int size) {}
    }
}
