package com.quickbite.api_gateway.controller;

import com.quickbite.api_gateway.service.RoutingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiGatewayController {

    private final RoutingService routingService;
    private final WebClient webClient;

    @RequestMapping(
        value = "/**",
        method = {
            RequestMethod.GET,
            RequestMethod.POST,
            RequestMethod.PUT,
            RequestMethod.DELETE
        }
    )
    public Mono<ResponseEntity<String>> routeAllRequests(
        HttpServletRequest request,
        @RequestBody(required = false) String body
    ) {
        String targetUrl = routingService.determineTargetUrl(request.getRequestURI());

        WebClient.RequestBodySpec requestSpec = webClient.method(HttpMethod.valueOf(request.getMethod()))
            .uri(targetUrl);

        if (body != null && !body.isEmpty()) {
            requestSpec.bodyValue(body).header("Content-Type", "application/json");
        }

        return requestSpec.retrieve().toEntity(String.class);
    }
}