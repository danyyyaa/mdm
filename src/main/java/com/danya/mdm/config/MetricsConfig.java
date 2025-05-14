package com.danya.mdm.config;

import com.danya.mdm.property.MdmProperty;
import com.danya.mdm.service.MetricsService;
import io.micrometer.core.aop.CountedAspect;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MetricsConfig {

    private final MetricsService metricsService;
    private final MdmProperty mdmProperty;

    @Bean
    public Gauge mdmUndeliveredGauge(MeterRegistry registry) {
        return Gauge.builder(
                        mdmProperty.metrics().undeliveredEvents(),
                        metricsService,
                        MetricsService::countUndelivered
                )
                .description("Количество недоставленных MDM-событий (ERROR/FATAL_ERROR)")
                .register(registry);
    }

    @Bean
    public TimedAspect timesAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    @Bean
    public CountedAspect countedAspect(MeterRegistry registry) {
        return new CountedAspect(registry);
    }
}