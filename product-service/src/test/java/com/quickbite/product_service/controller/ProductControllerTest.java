package com.quickbite.product_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.product_service.constants.TestConstants;
import com.quickbite.product_service.dto.ProductRequest;
import com.quickbite.product_service.dto.ProductResponse;
import com.quickbite.product_service.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(ProductControllerTest.TestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestConfig {

        @Bean
        ProductService productService() {
            return mock(ProductService.class);
        }
    }

    @WithMockUser(username = "user")
    @Test
    void shouldReturn400_whenNameIsBlank() throws Exception {
        ProductRequest request = ProductRequest.builder()
            .name(TestConstants.BLANK_RESTAURANT_NAME)
            .price(BigDecimal.valueOf(TestConstants.VALID_PRICE))
            .restaurantId(TestConstants.VALID_RESTAURANT_ID)
            .build();

        String body = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.name").value(TestConstants.PRODUCT_NAME_REQUIRED_MESSAGE));
    }

    @WithMockUser(username = "user")
    @Test
    void shouldCreateProduct_whenNameIsValid() throws Exception {
        ProductRequest request = ProductRequest.builder()
            .name(TestConstants.VALID_PRODUCT_NAME)
            .price(BigDecimal.valueOf(TestConstants.VALID_PRICE))
            .restaurantId(TestConstants.VALID_RESTAURANT_ID)
            .build();

        String body = objectMapper.writeValueAsString(request);

        when(productService.createProduct(any()))
            .thenReturn(new ProductResponse());

        mockMvc.perform(post("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isOk());

        verify(productService, times(1)).createProduct(any());
    }
}
