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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

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
                .content(body)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.name")
                .value(TestConstants.PRODUCT_NAME_REQUIRED_MESSAGE));
    }

    @WithMockUser(username = "user")
    @Test
    void shouldCreateProduct_whenNameIsValid() throws Exception {
        ProductRequest request = ProductRequest.builder()
            .name(TestConstants.VALID_PRODUCT_NAME)
            .price(BigDecimal.valueOf(TestConstants.VALID_PRICE))
            .costPrice(BigDecimal.valueOf(TestConstants.VALID_COST_PRICE))
            .restaurantId(TestConstants.VALID_RESTAURANT_ID)
            .build();

        String body = objectMapper.writeValueAsString(request);

        when(productService.createProduct(any()))
            .thenReturn(new ProductResponse());

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
            )
            .andExpect(status().isCreated());

        verify(productService, times(1)).createProduct(any());
    }
}
