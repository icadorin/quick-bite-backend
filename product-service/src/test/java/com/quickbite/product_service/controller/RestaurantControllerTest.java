package com.quickbite.product_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.product_service.constants.TestConstants;
import com.quickbite.product_service.dto.RestaurantRequest;
import com.quickbite.product_service.dto.RestaurantResponse;
import com.quickbite.product_service.service.RestaurantService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RestaurantController.class)
@Import(RestaurantControllerTest.TestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class RestaurantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        RestaurantService restaurantService() {
            return mock(RestaurantService.class);
        }
    }

    @WithMockUser
    @Test
    void shouldReturn400_whenNameIsBlank() throws Exception {
        RestaurantRequest request = RestaurantRequest.builder()
            .ownerId(TestConstants.VALID_OWNER_ID)
            .name("")
            .build();

        mockMvc.perform(post("/api/restaurants")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.name")
            .value(TestConstants.RESTAURANT_NAME_REQUIRED_MESSAGE));
    }

    @WithMockUser
    @Test
    void shouldCreateRestaurant_whenRequestIsValid() throws Exception {
        RestaurantRequest request = RestaurantRequest.builder()
            .ownerId(TestConstants.VALID_OWNER_ID)
            .name(TestConstants.VALID_RESTAURANT_NAME)
            .build();

        when(restaurantService.createRestaurant(any()))
            .thenReturn(RestaurantResponse.builder().build());

        mockMvc.perform(post("/api/restaurants")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        verify(restaurantService).createRestaurant(any());
    }

    @WithMockUser
    @Test
    void shouldGetRestaurantById() throws Exception {
        when(restaurantService.getRestaurantById(TestConstants.VALID_RESTAURANT_ID))
            .thenReturn(RestaurantResponse.builder().build());

        mockMvc.perform(
                get("/api/restaurants/{id}",
                TestConstants.VALID_RESTAURANT_ID)
            )
            .andExpect(status().isOk());

        verify(restaurantService).getRestaurantById(TestConstants.VALID_RESTAURANT_ID);
    }
}
