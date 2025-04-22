package com.danya.mdm.client.service;

import com.danya.mdm.client.ServiceOneClient;
import com.danya.mdm.client.dto.ServiceOneUpdatePhoneRequestDto;
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
public class ServiceOneClientService {

    private final ServiceOneClient client;
    private final Executor serviceOneExecutor;

    @RateLimiter(name = "service1-client", fallbackMethod = "fallback")
    public CompletableFuture<ServiceUpdatePhoneResponseDto> send(ServiceOneUpdatePhoneRequestDto dto) {
        return CompletableFuture.supplyAsync(() -> client.send(dto), serviceOneExecutor);
    }

    public CompletableFuture<ServiceUpdatePhoneResponseDto> fallback(Exception e) {
        log.error("Сервис 1 недоступен", e);
        return CompletableFuture.failedFuture(new ServiceClientException("Сервис 1 недоступен", e));
    }
}

