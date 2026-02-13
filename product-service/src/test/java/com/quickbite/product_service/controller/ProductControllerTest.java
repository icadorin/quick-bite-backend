package com.quickbite.product_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.product_service.constants.ApiPaths;
import com.quickbite.product_service.constants.TestConstants;
import com.quickbite.product_service.dto.ProductRequest;
import com.quickbite.product_service.dto.ProductResponse;
import com.quickbite.product_service.dto.filter.ProductFilter;
import com.quickbite.product_service.security.JwtAuthenticationFilter;
import com.quickbite.product_service.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private ProductService service;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductResponse response;

    @WithMockUser(roles = {"ADMIN", "RESTAURANT_OWNER"})
    @Test
    void create_shouldReturn400_whenNameIsBlank() throws Exception {
        ProductRequest request = ProductRequest.builder()
            .restaurantId(TestConstants.VALID_RESTAURANT_ID)
            .name(TestConstants.BLANK_RESTAURANT_NAME)
            .price(BigDecimal.valueOf(TestConstants.VALID_PRICE))
            .costPrice(BigDecimal.valueOf(TestConstants.VALID_COST_PRICE))
            .build();

        mockMvc.perform(post(ApiPaths.PRODUCTS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.name").exists());

        verifyNoMoreInteractions(service);
    }

    @Test
    void getProducts_shouldReturnPaginatedProducts() throws Exception {
        Page<ProductResponse> page =
            new PageImpl<>(List.of(mock(ProductResponse.class)));

        when(service.getProducts(any(), any()))
            .thenReturn(page);

        mockMvc.perform(get(ApiPaths.PRODUCTS)
                .param("page", TestConstants.DEFAULT_PAGE)
                .param("size", TestConstants.DEFAULT_SIZE)
                .param("sort", TestConstants.DEFAULT_SORT))
            .andExpect(status().isOk());

        verify(service).getProducts(any(ProductFilter.class), any());
    }

    @Test
    void getById_shouldReturnProduct() throws Exception {
        ProductResponse response = ProductResponse.builder()
            .id(TestConstants.VALID_PRODUCT_ID)
            .name(TestConstants.VALID_PRODUCT_NAME)
            .build();

        when(service.getProductById(TestConstants.VALID_PRODUCT_ID))
            .thenReturn(response);

        mockMvc.perform(get(ApiPaths.PRODUCTS + ApiPaths.BY_ID,
                TestConstants.VALID_PRODUCT_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(TestConstants.VALID_PRODUCT_ID));

        verify(service).getProductById(TestConstants.VALID_PRODUCT_ID);
    }

    @WithMockUser(roles = {"ADMIN", "RESTAURANT_OWNER"})
    @Test
    void create_shouldReturn201_whenRequestIsValid() throws Exception {
        ProductRequest request = ProductRequest.builder()
            .restaurantId(TestConstants.VALID_RESTAURANT_ID)
            .name(TestConstants.VALID_PRODUCT_NAME)
            .price(BigDecimal.valueOf(TestConstants.VALID_PRICE))
            .costPrice(BigDecimal.valueOf(TestConstants.VALID_COST_PRICE))
            .build();

        ProductResponse response = ProductResponse.builder()
            .id(TestConstants.VALID_PRODUCT_ID)
            .name(TestConstants.VALID_PRODUCT_NAME)
            .price(BigDecimal.valueOf(TestConstants.VALID_PRICE))
            .build();

        when(service.createProduct(any(ProductRequest.class)))
            .thenReturn(response);

        mockMvc.perform(post(ApiPaths.PRODUCTS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(TestConstants.VALID_PRODUCT_ID));

        verify(service).createProduct(any(ProductRequest.class));
    }
}
