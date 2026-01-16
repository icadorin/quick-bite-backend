package com.quickbite.api_gateway.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class RouteConfig {

    @Getter
    @RequiredArgsConstructor
    public enum Service {
        AUTH("auth", "8082"),
        PRODUCT("product", "8083"),
        ORDER("order", "8084"),
        PAYMENT("payment", "8085"),
        NOTIFICATION("notification", "8086");

        private final String pathPrefix;
        private final String port;
    }

    private static final Map<String, Service> PATH_TO_SERVICE = Map.of(
        "auth", Service.AUTH,
        "products", Service.PRODUCT,
        "restaurants", Service.PRODUCT,
        "categories", Service.PRODUCT,
        "order", Service.ORDER,
        "payment", Service.PAYMENT,
        "notification", Service.NOTIFICATION
    );

    public Optional<Service> findServiceByPath(String requestPath) {
        return PATH_TO_SERVICE.entrySet().stream()
            .filter(entry -> requestPath.startsWith("/api/" + entry.getKey()))
            .map(Map.Entry::getValue)
            .findFirst();
    }
}
