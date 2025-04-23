package com.danya.mdm.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("mdm")
public record MdmProperty(
        Executor executor
) {
    public record Executor(
            ServiceConfig serviceOne,
            ServiceConfig serviceTwo
    ) {
        public record ServiceConfig(int threads, int queueDepth) {
        }
    }
}
