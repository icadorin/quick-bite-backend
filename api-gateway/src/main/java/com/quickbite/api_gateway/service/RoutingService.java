package com.quickbite.api_gateway.service;

import com.quickbite.api_gateway.config.RouteConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutingService {

    private final RouteConfig routeConfig;

    public String determineTargetUrl(String requestPath) {

        RouteConfig.Service service = routeConfig.findServiceByPath(requestPath).orElse(RouteConfig.Service.AUTH);

        String targetUrl = "http://localhost:" + service.getPort() + requestPath;

        log.info("Roteamento: {} -> {}", requestPath, targetUrl);

        return targetUrl;
    }
}
