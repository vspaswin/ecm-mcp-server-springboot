package com.jpmc.ecm.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * WebClient configuration for ECM API communication.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final EcmApiProperties ecmApiProperties;

    @Bean
    public WebClient ecmWebClient() {
        // Configure connection provider
        ConnectionProvider connectionProvider = ConnectionProvider.builder("ecm-pool")
                .maxConnections(100)
                .maxIdleTime(Duration.ofSeconds(20))
                .maxLifeTime(Duration.ofSeconds(60))
                .pendingAcquireTimeout(Duration.ofSeconds(60))
                .evictInBackground(Duration.ofSeconds(120))
                .build();

        // Configure HTTP client
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        (int) ecmApiProperties.getTimeout().getConnect().toMillis())
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(
                                ecmApiProperties.getTimeout().getRead().toSeconds(),
                                TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(30, TimeUnit.SECONDS)))
                .compress(true)
                .responseTimeout(ecmApiProperties.getTimeout().getRead());

        return WebClient.builder()
                .baseUrl(ecmApiProperties.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .filter(authenticationFilter())
                .filter(loggingFilter())
                .filter(errorHandlingFilter())
                .build();
    }

    /**
     * Authentication filter for adding credentials to requests
     */
    private ExchangeFilterFunction authenticationFilter() {
        return (request, next) -> {
            var builder = request.mutate();

            // Add API key if configured
            if (ecmApiProperties.getApiKey() != null && !ecmApiProperties.getApiKey().isEmpty()) {
                builder.header("X-API-Key", ecmApiProperties.getApiKey());
            }
            // Add basic auth if username/password configured
            else if (ecmApiProperties.getUsername() != null && 
                     !ecmApiProperties.getUsername().isEmpty()) {
                String auth = ecmApiProperties.getUsername() + ":" + 
                              ecmApiProperties.getPassword();
                String encodedAuth = Base64.getEncoder()
                        .encodeToString(auth.getBytes());
                builder.header(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth);
            }

            return next.exchange(builder.build());
        };
    }

    /**
     * Logging filter for request/response logging
     */
    private ExchangeFilterFunction loggingFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            log.debug("ECM API Request: {} {}", request.method(), request.url());
            return Mono.just(request);
        });
    }

    /**
     * Error handling filter
     */
    private ExchangeFilterFunction errorHandlingFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            if (response.statusCode().isError()) {
                log.error("ECM API Error Response: {} {}", 
                         response.statusCode(), 
                         response.statusCode().getReasonPhrase());
            }
            return Mono.just(response);
        });
    }
}
