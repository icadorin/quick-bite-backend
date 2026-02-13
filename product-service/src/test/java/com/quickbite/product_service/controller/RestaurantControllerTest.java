package com.quickbite.product_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.product_service.constants.ApiPaths;
import com.quickbite.product_service.constants.TestConstants;
import com.quickbite.product_service.dto.RestaurantRequest;
import com.quickbite.product_service.dto.RestaurantResponse;
import com.quickbite.product_service.dto.filter.RestaurantFilter;
import com.quickbite.product_service.security.JwtAuthenticationFilter;
import com.quickbite.product_service.service.RestaurantService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RestaurantController.class)
@AutoConfigureMockMvc(addFilters = false)
class RestaurantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private RestaurantService restaurantService;

    @Autowired
    private ObjectMapper objectMapper;

    @WithMockUser(roles = {"ADMIN", "RESTAURANT_OWNED"})
    @Test
    void create_shouldReturn400_whenNameIsBlank() throws Exception {
        RestaurantRequest request = RestaurantRequest.builder()
            .ownerId(TestConstants.VALID_OWNER_ID)
            .name("")
            .build();

        mockMvc.perform(post(ApiPaths.RESTAURANTS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verifyNoMoreInteractions(restaurantService);
    }

    @Test
    void getById_shouldReturnRestaurant() throws Exception {
        when(restaurantService.getRestaurantById(TestConstants.VALID_RESTAURANT_ID))
            .thenReturn(RestaurantResponse.builder()
                .id(TestConstants.VALID_RESTAURANT_ID)
                .build());

        mockMvc.perform(get(ApiPaths.RESTAURANTS + ApiPaths.BY_ID,
                TestConstants.VALID_RESTAURANT_ID))
            .andExpect(status().isOk());

        verify(restaurantService).getRestaurantById(TestConstants.VALID_RESTAURANT_ID);
    }

    @Test
    void getRestaurants_shouldReturnPaginatedRestaurants() throws Exception {
        mockMvc.perform(get(ApiPaths.RESTAURANTS)
                .param("page", "0")
                .param("size", "20")
                .param("sort", "name,asc"))
            .andExpect(status().isOk());

        verify(restaurantService).getRestaurants(any(RestaurantFilter.class), any());
    }

    @Test
    void getRestaurants_shouldBuildFilterCorrectly() throws Exception {
        Page<RestaurantResponse> page =
            new PageImpl<>(List.of(mock(RestaurantResponse.class)));

        when(restaurantService.getRestaurants(any(), any()))
            .thenReturn(page);

        mockMvc.perform(get(ApiPaths.RESTAURANTS)
                .param("name", TestConstants.VALID_PRODUCT_NAME))
            .andExpect(status().isOk());

        verify(restaurantService).getRestaurants(
            argThat(filter ->
                filter != null &&
                TestConstants.VALID_PRODUCT_NAME.equals(filter.name())
            ),
            any(Pageable.class)
        );
    }

    @WithMockUser
    @Test
    void create_shouldRestaurant201_whenRequestIsValid() throws Exception {
        RestaurantRequest request = RestaurantRequest.builder()
            .name(TestConstants.VALID_RESTAURANT_NAME)
            .ownerId(TestConstants.VALID_OWNER_ID)
            .build();

        RestaurantResponse response = RestaurantResponse.builder()
            .id(TestConstants.VALID_RESTAURANT_ID)
            .name(TestConstants.VALID_RESTAURANT_NAME)
            .build();

        when(restaurantService.createRestaurant(any()))
            .thenReturn(response);

        mockMvc.perform(post(ApiPaths.RESTAURANTS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(TestConstants.VALID_RESTAURANT_ID));

        verify(restaurantService).createRestaurant(any());
    }
}
