package com.danya.mdm.config;

import com.danya.mdm.property.MdmProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.*;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class MdmConfig {

    private final MdmProperty mdmProperty;

    @Bean(name = "serviceOneExecutor")
    public Executor serviceOneCallExecutor() {
        return createElasticExecutor(mdmProperty.executor().serviceOne().threads(),
                mdmProperty.executor().serviceOne().queueDepth());
    }

    @Bean(name = "serviceTwoExecutor")
    public Executor serviceTwoCallExecutor() {
        return createElasticExecutor(mdmProperty.executor().serviceTwo().threads(),
                mdmProperty.executor().serviceTwo().queueDepth());
    }

    @Bean
    public ExecutorService retrySendMessagesExecutor() {
        return new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(1), new ThreadPoolExecutor.DiscardPolicy());
    }

    private ThreadPoolExecutor createElasticExecutor(int threads, int queueCapacity) {
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(queueCapacity);

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                threads, threads,
                60L, TimeUnit.SECONDS,
                queue,
                new ThreadPoolExecutor.AbortPolicy()
        );

        threadPoolExecutor.allowCoreThreadTimeOut(true);

        return threadPoolExecutor;
    }

}
