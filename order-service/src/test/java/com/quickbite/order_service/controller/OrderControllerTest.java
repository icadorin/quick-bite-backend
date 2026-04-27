    package com.quickbite.order_service.controller;

    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.quickbite.core.security.UserRole;
    import com.quickbite.order_service.client.ProductServiceClient;
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
    import org.mockito.Mockito;
    import org.springframework.beans.factory.annotation.Autowired;

    import org.springframework.boot.test.context.TestConfiguration;
    import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
    import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Import;
    import org.springframework.http.MediaType;
    import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
    import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
    import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
    import org.springframework.security.core.Authentication;
    import org.springframework.security.web.SecurityFilterChain;
    import org.springframework.test.context.ActiveProfiles;
    import org.springframework.test.context.bean.override.mockito.MockitoBean;
    import org.springframework.test.web.servlet.MockMvc;

    import java.util.List;

    import static com.quickbite.order_service.constants.TestConstants.*;
    import static org.mockito.ArgumentMatchers.*;
    import static org.mockito.Mockito.*;
    import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
    import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
    import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
    import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
    import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

    @WebMvcTest(OrderController.class)
    @AutoConfigureMockMvc
    @ActiveProfiles("test")
    @Import(OrderControllerTest.TestSecurityConfig.class)
    @EnableWebSecurity
    public class OrderControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private OrderService orderService;

        @MockitoBean
        private JwtService jwtService;

        @MockitoBean
        private ProductServiceClient productServiceClient;

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Test
        void createOrder_shouldCreateOrderAndReturnResponse_whenRequestIsValid() throws Exception {

            OrderRequest request = OrderRequest.builder()
                .restaurantId(VALID_RESTAURANT_ID)
                .deliveryAddress(
                    DeliveryAddressRequest.builder()
                        .street(CONTROLLER_STREET)
                        .number(VALID_NUMBER)
                        .city(VALID_CITY)
                        .state(VALID_STATE)
                        .zipCode(VALID_ZIP_CODE)
                        .complement(VALID_COMPLEMENT)
                        .build()
                )
                .paymentMethod(PaymentMethod.PIX)
                .items(List.of(
                    OrderItemRequest.builder()
                        .productId(VALID_PRODUCT_ID)
                        .quantity(SINGLE_QUANTITY)
                        .build()
                ))
                .build();

            OrderResponse expectedResponse = OrderResponse.builder()
                .id(VALID_ORDER_ID)
                .userId(VALID_USER_ID)
                .restaurantId(VALID_RESTAURANT_ID)
                .build();

            when(orderService.createOrder(any(), anyLong()))
                .thenReturn(expectedResponse);

            JwtUser user = new JwtUser(
                VALID_USER_ID,
                VALID_RESTAURANT_ID,
                UserRole.CUSTOMER
            );

            Authentication auth =
                new UsernamePasswordAuthenticationToken(user, null, List.of());

            mockMvc.perform(post(ApiPaths.ORDERS)
                    .with(authentication(auth))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(VALID_ORDER_ID))
                .andExpect(jsonPath("$.userId").value(VALID_USER_ID))
                .andExpect(jsonPath("$.restaurantId").value(VALID_RESTAURANT_ID));

            verify(orderService).createOrder(
                any(),
                eq(1L)
            );
        }

        @Test
        void createOrder_shouldReturn400_whenRequestIdInvalid() throws Exception {

            OrderRequest request = OrderRequest.builder()
                .restaurantId(null)
                .build();

            JwtUser user = new JwtUser(
                VALID_USER_ID,
                VALID_RESTAURANT_ID,
                UserRole.CUSTOMER
            );

            Authentication auth =
                new UsernamePasswordAuthenticationToken(user, null, List.of());

            mockMvc.perform(post(ApiPaths.ORDERS)
                    .with(authentication(auth))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

            verify(orderService, Mockito.never())
                .createOrder(any(), anyLong());
        }

        @Test
        void shouldGetUserOrders() throws Exception {
            JwtUser user = new JwtUser(
                VALID_USER_ID,
                INVALID_ID,
                UserRole.CUSTOMER
            );

            Authentication auth =
                new UsernamePasswordAuthenticationToken(
                    user,
                    INVALID_ID,
                    List.of()
                );

            when(orderService.getUserOrders(VALID_USER_ID))
                .thenReturn(List.of(new OrderResponse()));

            mockMvc.perform(get(ApiPaths.ORDERS)
                    .with(authentication(auth)))
                .andExpect(status().isOk());
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
