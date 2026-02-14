package com.jpmc.ecm.mcp;

import com.jpmc.ecm.client.EcmApiClient;
import com.jpmc.ecm.config.McpProtocolConfig;
import com.jpmc.ecm.mcp.model.McpRequest;
import com.jpmc.ecm.mcp.model.McpResponse;
import com.jpmc.ecm.mcp.tools.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class McpProtocolHandlerTest {

    @Mock
    private EcmApiClient ecmApiClient;

    @Mock
    private DocumentTools documentTools;

    @Mock
    private SearchTools searchTools;

    @Mock
    private FolderTools folderTools;

    @Mock
    private MetadataTools metadataTools;

    @Mock
    private VersionTools versionTools;

    @Mock
    private WorkflowTools workflowTools;

    private McpProtocolHandler handler;
    private McpProtocolConfig config;

    @BeforeEach
    void setUp() {
        config = new McpProtocolConfig();
        config.setVersion("2024-11-05");
        
        handler = new McpProtocolHandler(
            config,
            ecmApiClient,
            documentTools,
            searchTools,
            folderTools,
            metadataTools,
            versionTools,
            workflowTools
        );
    }

    @Test
    void testInitialize() {
        McpRequest request = new McpRequest();
        request.setId("1");
        request.setMethod("initialize");

        Mono<McpResponse> response = handler.handleRequest(request);

        StepVerifier.create(response)
            .assertNext(resp -> {
                assertThat(resp.getId()).isEqualTo("1");
                assertThat(resp.getResult()).isNotNull();
                assertThat(resp.getError()).isNull();
            })
            .verifyComplete();
    }

    @Test
    void testHealthCheck() {
        when(ecmApiClient.healthCheck())
            .thenReturn(Mono.just(Map.of("status", "healthy")));

        McpRequest request = new McpRequest();
        request.setId("2");
        request.setMethod("health");

        Mono<McpResponse> response = handler.handleRequest(request);

        StepVerifier.create(response)
            .assertNext(resp -> {
                assertThat(resp.getId()).isEqualTo("2");
                assertThat(resp.getResult()).isNotNull();
                assertThat(resp.getError()).isNull();
            })
            .verifyComplete();
    }

    @Test
    void testUnknownMethod() {
        McpRequest request = new McpRequest();
        request.setId("3");
        request.setMethod("unknown_method");

        Mono<McpResponse> response = handler.handleRequest(request);

        StepVerifier.create(response)
            .assertNext(resp -> {
                assertThat(resp.getId()).isEqualTo("3");
                assertThat(resp.getError()).isNotNull();
                assertThat(resp.getError().getCode()).isEqualTo(-32601);
            })
            .verifyComplete();
    }
}
