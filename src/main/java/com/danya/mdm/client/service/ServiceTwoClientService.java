package com.danya.mdm.client.service;

import com.danya.mdm.client.ServiceTwoClient;
import com.danya.mdm.client.dto.ServiceTwoUpdatePhoneRequestDto;
import com.danya.mdm.client.dto.ServiceUpdatePhoneResponseDto;
import com.danya.mdm.exception.ServiceClientException;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceTwoClientService {

    private final ServiceTwoClient client;
    private final Executor serviceTwoExecutor;

    @RateLimiter(name = "service2-client", fallbackMethod = "fallback")
    public CompletableFuture<ServiceUpdatePhoneResponseDto> send(ServiceTwoUpdatePhoneRequestDto dto) {
        return CompletableFuture.supplyAsync(() -> client.send(dto), serviceTwoExecutor);
    }

    public CompletableFuture<ServiceUpdatePhoneResponseDto> fallback(Exception e) {
        log.error("Сервис 2 недоступен", e);
        return CompletableFuture.failedFuture(new ServiceClientException("Сервис 2 недоступен", e));
    }
}