package com.jpmc.ecm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application class for ECM MCP Server.
 * 
 * This Spring Boot application provides a Model Context Protocol (MCP) server
 * for interacting with Enterprise Content Management REST APIs.
 * 
 * @author Aswini Pasupuleti
 * @since 1.0.0
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableAsync
public class EcmMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcmMcpServerApplication.class, args);
    }
}
