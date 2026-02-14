package com.jpmc.ecm.client;

import com.jpmc.ecm.dto.DocumentDto;
import com.jpmc.ecm.dto.SearchRequestDto;
import com.jpmc.ecm.dto.SearchResultDto;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EcmApiClientTest {

    private MockWebServer mockWebServer;
    private EcmApiClient ecmApiClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();
        WebClient webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .build();

        ecmApiClient = new EcmApiClient(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testHealthCheck() {
        mockWebServer.enqueue(new MockResponse()
            .setBody("{\"status\":\"healthy\"}")
            .addHeader("Content-Type", "application/json"));

        StepVerifier.create(ecmApiClient.healthCheck())
            .assertNext(response -> {
                assertThat(response).containsKey("status");
                assertThat(response.get("status")).isEqualTo("healthy");
            })
            .verifyComplete();
    }

    @Test
    void testGetDocument() {
        String documentJson = """{
            "id": "doc123",
            "title": "Test Document",
            "fileName": "test.pdf",
            "mimeType": "application/pdf",
            "size": 1024
        }""";

        mockWebServer.enqueue(new MockResponse()
            .setBody(documentJson)
            .addHeader("Content-Type", "application/json"));

        StepVerifier.create(ecmApiClient.getDocument("doc123"))
            .assertNext(doc -> {
                assertThat(doc.getId()).isEqualTo("doc123");
                assertThat(doc.getTitle()).isEqualTo("Test Document");
                assertThat(doc.getFileName()).isEqualTo("test.pdf");
            })
            .verifyComplete();
    }
}
