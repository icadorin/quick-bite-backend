package com.quickbite.api_gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
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
    public Mono<ResponseEntity<String>> routeToAuthService(HttpServletRequest request) {
        String path = request.getRequestURI();
        System.out.println("Gateway roteando: " + request.getRequestURI() + " → " + path);

        return webClient.get()
            .uri(path)
            .retrieve()
            .toEntity(String.class);
    }

    @PostMapping("/auth/**")
    public Mono<ResponseEntity<String>> routeToAthServicePost(
        HttpServletRequest request,
        @RequestBody(required = false) String body
    ) {
        String path = request.getRequestURI().replaceFirst("/api","");
        System.out.println("Getway POST roteando: " + request.getRequestURI() + " → " + path);

        return webClient.post()
            .uri(path)
            .bodyValue(body != null ? body : "{}")
            .header("Content-type", "application/json")
            .retrieve()
            .toEntity(String.class);
    }

    @PutMapping("/auth/**")
    public Mono<ResponseEntity<String>> routeToAuthServicePut(
        HttpServletRequest request,
        @RequestBody(required = false) String body
    ) {
        String path = request.getRequestURI().replaceFirst("/api", "");
        System.out.println("Gateway PUT roteandod: " + request.getRequestURI() + " → " + path);

        return webClient.put()
            .uri(path)
            .bodyValue(body != null ? body : "{}")
            .header("Content-type", "application/json")
            .retrieve()
            .toEntity(String.class);
    }

    @DeleteMapping("/auth/**")
    public Mono<ResponseEntity<String>> routeToAuthServiceDelete(HttpServletRequest request) {
        String path = request.getRequestURI().replaceFirst("/api","");
        System.out.println("Getway DELETE roteando: " + request.getRequestURI() + " → " + path);

        return webClient.delete()
            .uri(path)
            .retrieve()
            .toEntity(String.class);
    }
}