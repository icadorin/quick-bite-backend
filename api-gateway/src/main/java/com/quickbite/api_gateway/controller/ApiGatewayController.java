package com.quickbite.api_gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class ApiGatewayController {

    private final WebClient webClient;

    public ApiGatewayController() {
        this.webClient = WebClient.builder()
            .baseUrl("http://localhost:8082")
            .build();
    }

    @GetMapping("/auth/**")
    public Mono<ResponseEntity<String>> routeToAuthService() {
        return webClient.get()
                .uri("/api/auth/test")
                .retrieve()
                .toEntity(String.class);
    }
}
