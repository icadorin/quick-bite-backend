package com.quickbite.product_service.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.product_service.constants.ApiPaths;
import com.quickbite.product_service.constants.TestConstants;
import com.quickbite.product_service.dto.CategoryRequest;
import com.quickbite.product_service.dto.CategoryResponse;
import com.quickbite.product_service.security.JwtAuthenticationFilter;
import com.quickbite.product_service.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @WithMockUser
    @Test
    void create_shouldReturn400_whenNameIsBlank() throws Exception {
        CategoryRequest request = CategoryRequest.builder()
            .name("")
            .build();

        mockMvc.perform(post(ApiPaths.CATEGORIES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.name").value(TestConstants.CATEGORY_NAME_REQUIRED_MESSAGE));

        verifyNoInteractions(categoryService);
    }

    @Test
    void getCategories_shouldReturnPaginatedCategories() throws Exception {
        Page<CategoryResponse> page =
            new PageImpl<>(List.of(mock(CategoryResponse.class)));

        when(categoryService.getCategories(any(), any()))
            .thenReturn(page);

        mockMvc.perform(get(ApiPaths.CATEGORIES)
                .param("name", "Food"))
            .andExpect(status().isOk());

        verify(categoryService).getCategories(
            argThat(filter ->
                filter != null &&
                "Food".equals(filter.name())
            ),
            any(Pageable.class)
        );
    }

    @WithMockUser
    @Test
    void create_shouldReturn201_whenRequestIdValid() throws Exception {
        CategoryRequest request = CategoryRequest.builder()
            .name(TestConstants.VALID_CATEGORY_NAME)
            .build();

        CategoryResponse response = CategoryResponse.builder()
            .id(TestConstants.VALID_CATEGORY_ID)
            .name(TestConstants.VALID_CATEGORY_NAME)
            .build();

        when(categoryService.createCategory(any(CategoryRequest.class)))
            .thenReturn(response);

        mockMvc.perform(post(ApiPaths.CATEGORIES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(TestConstants.VALID_CATEGORY_ID))
            .andExpect(jsonPath("$.name").value(TestConstants.VALID_CATEGORY_NAME));

        verify(categoryService).createCategory(any(CategoryRequest.class));
    }
}
