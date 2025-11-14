package com.quickbite.api_gateway.controller;

import com.quickbite.api_gateway.service.RoutingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
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
        String requestPath = request.getRequestURI();
        String targetUrl = routingService.determineTargetUrl(requestPath);

        log.info("Gateway roteando: {} {} -> {}", request.getMethod(), requestPath, targetUrl);

        WebClient.RequestBodySpec requestSpec = webClient.method(HttpMethod.valueOf(request.getMethod()))
            .uri(targetUrl)
            .headers(headers -> {
                var headerNames = request.getHeaderNames();
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    if (!headerName.equalsIgnoreCase("Content-Length")) {
                        headers.addAll(headerName, java.util.Collections.list(request.getHeaders(headerName)));
                    }
                }
            });

        if (body != null && !body.isEmpty() &&
            (request.getMethod().equals("POST") || request.getMethod().equals("PUT"))) {
            requestSpec.bodyValue(body);
        }

        return requestSpec.retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response ->
                response.bodyToMono(String.class)
                    .defaultIfEmpty("")
                    .flatMap(errorBody -> {
                        log.warn("Erro {} do serviço backend: {}", response.statusCode(), errorBody);

                        return Mono.error(WebClientResponseException.create(
                            response.statusCode().value(),
                            "HTTP Error" + response.statusCode().value(),
                            response.headers().asHttpHeaders(),
                            errorBody.getBytes(StandardCharsets.UTF_8),
                            StandardCharsets.UTF_8
                        ));
                    })
            )
            .toEntity(String.class)
            .doOnSuccess(result ->
                log.info("Resposta do serviço: {}", result.getStatusCode())
            )
            .doOnError(error ->
                log.error("Erro ao chamar serviço: {}", error.getMessage())
            );
    }
}