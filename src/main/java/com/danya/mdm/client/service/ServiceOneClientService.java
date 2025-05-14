package com.danya.mdm.client.service;

import com.danya.mdm.client.ServiceOneClient;
import com.danya.mdm.dto.ServiceOneUpdatePhoneRequestDto;
import com.danya.mdm.dto.ServiceUpdatePhoneResponseDto;
import com.danya.mdm.exception.ServiceClientException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
    @CircuitBreaker(name = "service1-client", fallbackMethod = "fallback")
    public CompletableFuture<ResponseEntity<ServiceUpdatePhoneResponseDto>> send(ServiceOneUpdatePhoneRequestDto dto) {
        return CompletableFuture.supplyAsync(() -> client.send(dto), serviceOneExecutor);
    }

    public CompletableFuture<ResponseEntity<ServiceUpdatePhoneResponseDto>> fallback(Exception e) {
        log.warn("Сервис 1 недоступен", e);
        return CompletableFuture.failedFuture(new ServiceClientException("Сервис 1 недоступен", e));
    }
}

