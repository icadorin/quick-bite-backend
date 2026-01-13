package com.quickbite.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class JacksonCustomizer {

    private JacksonCustomizer() {}

    public static ObjectMapper withJavaTime(ObjectMapper mapper) {
        return mapper.registerModule(new JavaTimeModule());
    }
}