package com.quickbite.product_service.config;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public JsonMapper objectMapper() {

        return JsonMapper.builder()
            .changeDefaultPropertyInclusion(incl ->
                incl.withValueInclusion(JsonInclude.Include.NON_NULL))
            .build();
    }
}
