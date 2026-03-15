    package com.quickbite.order_service.controller;

    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.quickbite.core.security.UserRole;
    import com.quickbite.order_service.constants.ApiPaths;
    import com.quickbite.order_service.dto.DeliveryAddressRequest;
    import com.quickbite.order_service.dto.OrderItemRequest;
    import com.quickbite.order_service.dto.OrderRequest;
    import com.quickbite.order_service.dto.OrderResponse;
    import com.quickbite.order_service.entity.PaymentMethod;
    import com.quickbite.order_service.security.JwtUser;
    import com.quickbite.order_service.service.JwtService;
    import com.quickbite.order_service.service.OrderService;
    import org.junit.jupiter.api.Test;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

    import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
    import org.springframework.boot.test.context.TestConfiguration;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Import;
    import org.springframework.http.MediaType;
    import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
    import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
    import org.springframework.security.core.Authentication;
    import org.springframework.security.web.SecurityFilterChain;
    import org.springframework.test.context.ActiveProfiles;
    import org.springframework.test.context.bean.override.mockito.MockitoBean;
    import org.springframework.test.web.servlet.MockMvc;

    import java.util.List;

    import static org.mockito.ArgumentMatchers.any;
    import static org.mockito.ArgumentMatchers.anyLong;
    import static org.mockito.Mockito.when;
    import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
    import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
    import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

    @WebMvcTest(OrderController.class)
    @AutoConfigureMockMvc
    @ActiveProfiles("test")
    @Import(OrderControllerTest.TestSecurityConfig.class)
    public class OrderControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private OrderService orderService;

        @MockitoBean
        private JwtService jwtService;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        void createOrder_shouldReturn201_whenRequestIsValid() throws Exception {

            OrderRequest request = OrderRequest.builder()
                .restaurantId(1L)
                .deliveryAddress(
                    DeliveryAddressRequest.builder()
                        .street("Z street")
                        .number("123")
                        .city("Join")
                        .state("SC")
                        .zipCode("999999-999")
                        .complement("Z complement")
                        .build()
                )
                .paymentMethod(PaymentMethod.PIX)
                .items(List.of(
                    OrderItemRequest.builder()
                        .productId(1L)
                        .quantity(1)
                        .build()
                ))
                .build();

            OrderResponse expectedResponse = OrderResponse.builder()
                .id(1L)
                .userId(1L)
                .restaurantId(1L)
                .build();

            when(orderService.createOrder(any(), anyLong()))
                .thenReturn(expectedResponse);

            JwtUser user = new JwtUser(
                1L,
                1L,
                UserRole.CUSTOMER
            );

            Authentication auth =
                new UsernamePasswordAuthenticationToken(user, null, List.of());

            mockMvc.perform(post(ApiPaths.ORDERS)
                    .with(authentication(auth))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        }

        @TestConfiguration
        public static class TestSecurityConfig {

            @Bean
            SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
                return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
            }
        }
    }
