package com.quickbite.product_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.core.config.JacksonCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper(ObjectMapper baseMapper) {
        return JacksonCustomizer.withJavaTime(baseMapper);
    }
}
