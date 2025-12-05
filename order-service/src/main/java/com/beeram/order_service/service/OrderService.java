package com.beeram.order_service.service;

import com.beeram.order_service.dto.InventoryResponse;
import com.beeram.order_service.dto.OrderLineItemsDto;
import com.beeram.order_service.dto.OrderRequest;
import com.beeram.order_service.model.Order;
import com.beeram.order_service.model.OrderLineItems;
import com.beeram.order_service.repo.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;

    public void placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems =  orderRequest.getOrderLineItemsDtoList().stream().map(this::mapToDTo).toList();
        order.setOrderLineItems(orderLineItems);

        List<String> skuCodes = order.getOrderLineItems().stream().map(OrderLineItems::getSkuCode).toList();

       InventoryResponse[] inventoryResponseArr =  webClient.get().uri("http://localhost:8082/api/inventory", uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                .retrieve()
                        .bodyToMono(InventoryResponse[].class)
                                .block();

        assert inventoryResponseArr != null;
        boolean isStockAvailable = Arrays.stream(inventoryResponseArr).allMatch(InventoryResponse::getIsInStock);

        if (!isStockAvailable) {
            throw new IllegalArgumentException("Product is out of stock, will notify once product available");
        } else {
            orderRepository.save(order);
        }
    }

    private OrderLineItems mapToDTo(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
}
