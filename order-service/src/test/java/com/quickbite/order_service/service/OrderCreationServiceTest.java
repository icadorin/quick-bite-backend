package com.quickbite.order_service.service;

import com.quickbite.core.exception.BusinessRuleViolationException;
import com.quickbite.order_service.client.ProductServiceClient;
import com.quickbite.order_service.dto.OrderItemRequest;
import com.quickbite.order_service.dto.OrderRequest;
import com.quickbite.order_service.dto.OrderResponse;
import com.quickbite.order_service.dto.ProductResponse;
import com.quickbite.order_service.entity.Order;
import com.quickbite.order_service.entity.OrderItem;
import com.quickbite.order_service.mappers.OrderCreateMapper;
import com.quickbite.order_service.mappers.OrderItemCreateMapper;
import com.quickbite.order_service.mappers.OrderResponseMapper;
import com.quickbite.order_service.repositories.OrderRepository;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static com.quickbite.order_service.constants.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderCreationServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderResponseMapper responseMapper;

    @Mock
    private OrderItemCreateMapper itemMapper;

    @Mock
    private OrderCreateMapper createMapper;

    @Mock
    private ProductServiceClient productClient;

    @InjectMocks
    private OrderCreationService service;

    private OrderRequest buildOrderRequest(
        Long restaurantId,
        Long productId,
        Integer quantity
    ) {
        return OrderRequest.builder()
            .restaurantId(restaurantId)
            .items(List.of(
                OrderItemRequest.builder()
                    .productId(productId)
                    .quantity(quantity)
                    .build()
            )).build();
    }

    private ProductResponse buildProduct(
        Long id,
        String name,
        BigDecimal price,
        boolean available
    ) {
        ProductResponse product = new ProductResponse();
        product.setId(id);
        product.setName(name);
        product.setPrice(price);
        product.setIsAvailable(available);

        return product;
    }

    private void mockItemMapper() {
        when(itemMapper.toEntity(any()))
            .thenAnswer(invocation -> {
                OrderItemRequest req = invocation.getArgument(0);

                return OrderItem.builder()
                    .quantity(DOUBLE_QUANTITY)
                    .build();
            });
    }

    @Test
    void shouldCreateOrderSuccessfully() {

        OrderRequest request = buildOrderRequest(
            VALID_RESTAURANT_ID,
            VALID_PRODUCT_ID,
            DOUBLE_QUANTITY
        );

        Order order = new Order();

        ProductResponse product =  buildProduct(
            VALID_PRODUCT_ID,
            VALID_PRODUCT_NAME,
            VALID_PRODUCT_PRICE,
            true
        );

        when(productClient.validateRestaurant(VALID_RESTAURANT_ID))
            .thenReturn(true);
        when(productClient.getProduct(VALID_PRODUCT_ID))
            .thenReturn(product);
        when(createMapper.toEntity(request))
            .thenReturn(order);

        mockItemMapper();

        when(orderRepository.save(order))
            .thenReturn(order);
        when(responseMapper.toResponse(order))
            .thenReturn(new OrderResponse());

        OrderResponse response = service.createOrder(request, CUSTOMER_USER_ID);

        assertNotNull(response);

        verify(orderRepository).save(order);
        verify(productClient).getProduct(VALID_PRODUCT_ID);
    }

    @Test
    void shouldThrow_whenRestaurantDoesNotExist() {

        OrderRequest request = OrderRequest.builder()
            .restaurantId(VALID_RESTAURANT_ID)
            .build();

        when(productClient.validateRestaurant(VALID_RESTAURANT_ID))
            .thenReturn(false);

        assertThrows(
            BusinessRuleViolationException.class,
            () -> service.createOrder(request, VALID_USER_ID)
        );
    }

    @Test
    void shouldThrow_whenProductUnavailable() {

        OrderRequest request = buildOrderRequest(
            VALID_RESTAURANT_ID,
            VALID_PRODUCT_ID,
            SINGLE_QUANTITY
        );

        Order order = Order.builder().build();

        ProductResponse product = buildProduct(
            VALID_PRODUCT_ID,
            VALID_PRODUCT_NAME,
            VALID_PRODUCT_PRICE,
            false
        );

        when(productClient.validateRestaurant(VALID_RESTAURANT_ID))
            .thenReturn(true);
        when(productClient.getProduct(VALID_PRODUCT_ID))
            .thenReturn(product);
        when(createMapper.toEntity(request))
            .thenReturn(order);

        assertThrows(
            BusinessRuleViolationException.class,
            () -> service.createOrder(request, VALID_USER_ID)
        );
    }

    @Test
    void shouldThrow_whenProductNotFound() {

        OrderRequest request = buildOrderRequest(
            VALID_RESTAURANT_ID,
            VALID_PRODUCT_ID,
            SINGLE_QUANTITY
        );

        Order order = new Order();

        when(productClient.validateRestaurant(VALID_RESTAURANT_ID))
            .thenReturn(true);
        when(productClient.getProduct(VALID_PRODUCT_ID))
            .thenThrow(FeignException.NotFound.class);
        when(createMapper.toEntity(request))
            .thenReturn(order);

        assertThrows(
            BusinessRuleViolationException.class,
            () -> service.createOrder(request, VALID_USER_ID)
        );
    }

    @Test
    void shouldThrow_whenProductServiceFails() {

        OrderRequest request = buildOrderRequest(
            VALID_RESTAURANT_ID,
            VALID_PRODUCT_ID,
            SINGLE_QUANTITY
        );

        Order order = new Order();

        when(productClient.validateRestaurant(VALID_RESTAURANT_ID))
            .thenReturn(true);
        when(productClient.getProduct(VALID_PRODUCT_ID))
            .thenThrow(FeignException.class);
        when(createMapper.toEntity(request))
            .thenReturn(order);

        assertThrows(
            BusinessRuleViolationException.class,
            () -> service.createOrder(request, VALID_USER_ID)
        );
    }

    @Test
    void shouldAddItemsToOrderCorrectly() {

        OrderRequest request = buildOrderRequest(
            VALID_RESTAURANT_ID,
            VALID_PRODUCT_ID,
            DOUBLE_QUANTITY
        );

        Order order = new Order();

        ProductResponse product =  buildProduct(
            VALID_PRODUCT_ID,
            VALID_PRODUCT_NAME,
            VALID_PRODUCT_PRICE,
            true
        );

        when(productClient.validateRestaurant(VALID_RESTAURANT_ID))
            .thenReturn(true);
        when(productClient.getProduct(VALID_PRODUCT_ID))
            .thenReturn(product);
        when(createMapper.toEntity(request))
            .thenReturn(order);

        mockItemMapper();

        when(orderRepository.save(any())).
            thenAnswer(invocation -> invocation.getArgument(0));
        when(responseMapper.toResponse(any()))
            .thenReturn(new OrderResponse());

        OrderResponse response = service.createOrder(request, CUSTOMER_USER_ID);

        assertNotNull(response);
        assertEquals(1, order.getItems().size());

        OrderItem savedItem = order.getItems().getFirst();

        assertEquals(
            VALID_PRODUCT_NAME,
            savedItem.getProductName()
        );

        assertEquals(
            VALID_PRODUCT_PRICE,
            savedItem.getUnitPrice()
        );

        assertEquals(
            DOUBLE_QUANTITY,
            savedItem.getQuantity()
        );
    }

    @Test
    void shouldCallProductClientForEachItem() {

        OrderRequest request = OrderRequest.builder()
            .restaurantId(VALID_RESTAURANT_ID)
            .items(List.of(
                OrderItemRequest.builder()
                    .productId(VALID_PRODUCT_ID)
                    .quantity(SINGLE_QUANTITY)
                    .build(),
                OrderItemRequest.builder()
                    .productId(SECOND_PRODUCT_ID)
                    .quantity(SINGLE_QUANTITY)
                    .build()
            )).build();

        Order order = new Order();

        ProductResponse product = buildProduct(
            VALID_PRODUCT_ID,
            GENERIC_PRODUCT_NAME,
            VALID_PRODUCT_PRICE,
            true
        );

        when(productClient.validateRestaurant(VALID_RESTAURANT_ID))
            .thenReturn(true);
        when(productClient.getProduct(any()))
            .thenReturn(product);
        when(createMapper.toEntity(request))
            .thenReturn(order);

        mockItemMapper();

        when(orderRepository.save(any()))
            .thenAnswer(invocation -> invocation.getArgument(0));

        when(responseMapper.toResponse(any()))
            .thenReturn(new OrderResponse());


        service.createOrder(request, VALID_USER_ID);

        verify(productClient, times(2))
            .getProduct(any());
    }
}
