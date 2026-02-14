package com.jpmc.ecm.controller;

import com.jpmc.ecm.client.EcmApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Health check controller.
 */
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

    private final EcmApiClient ecmApiClient;

    @GetMapping
    public Mono<Map<String, Object>> healthCheck() {
        return ecmApiClient.healthCheck()
            .map(ecmHealth -> Map.of(
                "status", "UP",
                "server", "ecm-mcp-server",
                "ecmApi", ecmHealth
            ))
            .onErrorResume(error -> Mono.just(Map.of(
                "status", "DOWN",
                "server", "ecm-mcp-server",
                "error", error.getMessage()
            )));
    }
}
