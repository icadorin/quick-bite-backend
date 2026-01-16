package com.quickbite.product_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.core.config.JacksonCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        return JacksonCustomizer.withJavaTime(builder.build());
    }
}
