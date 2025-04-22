package com.danya.mdm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class MdmConfig {

    @Bean(name = "serviceOneExecutor")
    public Executor serviceOneCallExecutor() {
        return createElasticExecutor(10, 10);
    }

    @Bean(name = "serviceTwoExecutor")
    public Executor serviceTwoCallExecutor() {
        return createElasticExecutor(10, 10);
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
