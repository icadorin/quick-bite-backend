package com.quickbite.order_service.service;

import com.quickbite.core.exception.BusinessRuleViolationException;
import com.quickbite.order_service.client.ProductServiceClient;
import com.quickbite.order_service.dto.OrderItemRequest;
import com.quickbite.order_service.dto.OrderRequest;
import com.quickbite.order_service.dto.OrderResponse;
import com.quickbite.order_service.dto.ProductResponse;
import com.quickbite.order_service.entity.Order;
import com.quickbite.order_service.entity.OrderItem;
import com.quickbite.order_service.entity.PaymentMethod;
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

    @Test
    void shouldCreateOrderSuccessfully() {

        OrderRequest request = OrderRequest.builder()
            .restaurantId(1L)
            .paymentMethod(PaymentMethod.PIX)
            .items(List.of(
                OrderItemRequest.builder()
                    .productId(1L)
                    .quantity(2)
                    .build()
                )
            ).build();

        Order order = new Order();

        ProductResponse product = new ProductResponse();
        product.setId(1L);
        product.setName("Burger");
        product.setPrice(BigDecimal.valueOf(10));
        product.setIsAvailable(true);

        when(productClient.validateRestaurant(1L)).thenReturn(true);
        when(productClient.getProduct(1L)).thenReturn(product);
        when(createMapper.toEntity(request)).thenReturn(order);

        when(itemMapper.toEntity(any())).thenAnswer(invocation -> {
            OrderItemRequest req = invocation.getArgument(0);

            return OrderItem.builder()
                .quantity(req.getQuantity())
                .build();
        });

        when(orderRepository.save(order)).thenReturn(order);
        when(responseMapper.toResponse(order)).thenReturn(new OrderResponse());

        OrderResponse response = service.createOrder(request, 99L);

        assertNotNull(response);
        verify(orderRepository).save(order);
        verify(productClient).getProduct(1L);
    }

    @Test
    void shouldThrow_whenRestaurantDoesNotExist() {

        OrderRequest request = OrderRequest.builder()
            .restaurantId(1L)
            .build();

        when(productClient.validateRestaurant(1L)).thenReturn(false);

        assertThrows(
            BusinessRuleViolationException.class,
            () -> service.createOrder(request, 1L)
        );
    }

    @Test
    void shouldThrow_whenProductUnavailable() {

        OrderRequest request = OrderRequest.builder()
            .restaurantId(1L)
            .items(List.of(
                OrderItemRequest.builder()
                    .productId(1L)
                    .quantity(1)
                    .build()
            ))
            .build();

        Order order = Order.builder().build();

        ProductResponse product = new ProductResponse();
        product.setIsAvailable(false);

        when(productClient.validateRestaurant(1L)).thenReturn(true);
        when(productClient.getProduct(1L)).thenReturn(product);
        when(createMapper.toEntity(request)).thenReturn(order);

        assertThrows(
            BusinessRuleViolationException.class,
            () -> service.createOrder(request, 1L)
        );
    }

    @Test
    void shouldThrow_whenProductNotFound() {

        OrderRequest request = OrderRequest.builder()
            .restaurantId(1L)
            .items(List.of(
                OrderItemRequest.builder()
                    .productId(1L)
                    .quantity(1)
                    .build()
            )).build();

        Order order = Order.builder().build();

        when(productClient.validateRestaurant(1L)).thenReturn(true);
        when(productClient.getProduct(1L)).thenThrow(FeignException.NotFound.class);
        when(createMapper.toEntity(request)).thenReturn(order);

        assertThrows(
            BusinessRuleViolationException.class,
            () -> service.createOrder(request, 1L)
        );
    }

    @Test
    void shouldThrow_whenProductServiceFails() {

        OrderRequest request = OrderRequest.builder()
            .restaurantId(1L)
            .items(List.of(
                OrderItemRequest.builder()
                    .productId(1L)
                    .quantity(1)
                    .build()
            )).build();

        Order order = Order.builder().build();

        when(productClient.validateRestaurant(1L)).thenReturn(true);
        when(productClient.getProduct(1L)).thenThrow(FeignException.class);
        when(createMapper.toEntity(request)).thenReturn(order);

        assertThrows(
            BusinessRuleViolationException.class,
            () -> service.createOrder(request, 1L)
        );
    }

    @Test
    void shouldAddItemsToOrderCorrectly() {

        OrderRequest request = OrderRequest.builder()
            .restaurantId(1L)
            .items(List.of(
                OrderItemRequest.builder()
                    .productId(1L)
                    .quantity(2)
                    .build()
            )).build();

        Order order = new Order();

        ProductResponse product = new ProductResponse();
        product.setId(1L);
        product.setName("Burger");
        product.setPrice(BigDecimal.valueOf(10));
        product.setIsAvailable(true);

        when(productClient.validateRestaurant(1L)).thenReturn(true);
        when(productClient.getProduct(1L)).thenReturn(product);
        when(createMapper.toEntity(request)).thenReturn(order);

        when(itemMapper.toEntity(any())).thenAnswer(invocation -> {
            OrderItemRequest req = invocation.getArgument(0);

            return OrderItem.builder()
                .quantity(req.getQuantity())
                .build();
        });

        when(orderRepository.save(any())).
            thenAnswer(invocation -> invocation.getArgument(0));
        when(responseMapper.toResponse(any())).thenReturn(new OrderResponse());

        OrderResponse response = service.createOrder(request, 99L);

        assertNotNull(response);
        assertEquals(1, order.getItems().size());

        OrderItem savedItem = order.getItems().getFirst();

        assertEquals("Burger", savedItem.getProductName());
        assertEquals(BigDecimal.valueOf(10), savedItem.getUnitPrice());
        assertEquals(2, savedItem.getQuantity());
    }

    @Test
    void shouldCalculateOrderResponse() {

        OrderRequest request = OrderRequest.builder()
            .restaurantId(1L)
            .items(List.of(
                OrderItemRequest.builder()
                    .productId(1L)
                    .quantity(2)
                    .build(),
                OrderItemRequest.builder()
                    .productId(2L)
                    .quantity(1)
                    .build()
            )).build();

        Order order = new Order();

        ProductResponse product1 = new ProductResponse();
        product1.setId(1L);
        product1.setName("Burger");
        product1.setPrice(BigDecimal.valueOf(10));
        product1.setIsAvailable(true);

        ProductResponse product2 = new ProductResponse();
        product2.setId(2L);
        product2.setName("Fries");
        product2.setPrice(BigDecimal.valueOf(5));
        product2.setIsAvailable(true);

        when(productClient.validateRestaurant(1L)).thenReturn(true);

        when(productClient.getProduct(1L)).thenReturn(product1);
        when(productClient.getProduct(2L)).thenReturn(product2);

        when(createMapper.toEntity(request)).thenReturn(order);

        when(itemMapper.toEntity(any())).thenAnswer(invocation -> {
            OrderItemRequest req = invocation.getArgument(0);

            return OrderItem.builder()
                .quantity(req.getQuantity())
                .build();
        });

        when(orderRepository.save(any()))
            .thenAnswer(invocation -> invocation.getArgument(0));

        when(responseMapper.toResponse(any()))
            .thenReturn(new OrderResponse());

        service.createOrder(request, 99L);

        assertEquals(0, order.getTotalAmount().compareTo(BigDecimal.valueOf(25)));
    }

    @Test
    void shouldCallProductClientForEachItem() {

        OrderRequest request = OrderRequest.builder()
            .restaurantId(1L)
            .items(List.of(
                OrderItemRequest.builder()
                    .productId(1L)
                    .quantity(1)
                    .build(),
                OrderItemRequest.builder()
                    .productId(2L)
                    .quantity(1)
                    .build()
            )).build();

        Order order = new Order();

        ProductResponse product = new ProductResponse();
        product.setName("Any");
        product.setPrice(BigDecimal.valueOf(10));
        product.setIsAvailable(true);

        when(productClient.validateRestaurant(1L)).thenReturn(true);
        when(productClient.getProduct(any())).thenReturn(product);
        when(createMapper.toEntity(request)).thenReturn(order);

        when(itemMapper.toEntity(any())).thenAnswer(invocation -> {
            OrderItemRequest req = invocation.getArgument(0);

            return OrderItem.builder()
                .quantity(req.getQuantity())
                .build();
        });

        when(orderRepository.save(any()))
            .thenAnswer(invocation -> invocation.getArgument(0));

        when(responseMapper.toResponse(any()))
            .thenReturn(new OrderResponse());


        service.createOrder(request, 1L);

        verify(productClient, times(2)).getProduct(any());
    }

    @Test
    void shouldCallRepositorySaveOnce() {

        OrderRequest request = OrderRequest.builder()
            .restaurantId(1L)
            .items(List.of(
                OrderItemRequest.builder()
                    .productId(1L)
                    .quantity(1)
                    .build()
            )).build();

        Order order = new Order();

        ProductResponse product = new ProductResponse();
        product.setName("Burger");
        product.setPrice(BigDecimal.valueOf(10));
        product.setIsAvailable(true);

        when(productClient.validateRestaurant(1L)).thenReturn(true);
        when(productClient.getProduct(1L)).thenReturn(product);
        when(createMapper.toEntity(request)).thenReturn(order);

        when(itemMapper.toEntity(any())).thenAnswer(invocation -> {
            OrderItemRequest req = invocation.getArgument(0);

            return OrderItem.builder()
                .quantity(req.getQuantity())
                .build();
        });

        when(orderRepository.save(any()))
            .thenAnswer(invocation -> invocation.getArgument(0));

        when(responseMapper.toResponse(any()))
            .thenReturn(new OrderResponse());

        service.createOrder(request, 1L);

        verify(orderRepository, times(1)).save(any());
    }
}
