package com.quickbite.product_service.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.product_service.constants.TestConstants;
import com.quickbite.product_service.dto.CategoryRequest;
import com.quickbite.product_service.dto.CategoryResponse;
import com.quickbite.product_service.service.CategoryService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(CategoryController.class)
@Import(CategoryControllerTest.TestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        CategoryService categoryService() {
            return mock(CategoryService.class);
        }
    }

    @WithMockUser
    @Test
    void shouldReturn400_whenNameIsBlank() throws Exception {
        CategoryRequest categoryRequest = new CategoryRequest();
        categoryRequest.setName("");

        mockMvc.perform(post("/api/categories")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(categoryRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.name")
                .value(TestConstants.CATEGORY_NAME_REQUIRED_MESSAGE));
    }

    @WithMockUser
    @Test
    void shouldCreateCategory_whenRequestIsValid() throws Exception {
        CategoryRequest categoryRequest = new CategoryRequest();
        categoryRequest.setName(TestConstants.VALID_CATEGORY_NAME);

        when(categoryService.createCategory(any()))
            .thenReturn(new CategoryResponse());

        mockMvc.perform(post("/api/categories")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(categoryRequest)))
            .andExpect(status().isOk());

        verify(categoryService).createCategory(any());
    }
}
