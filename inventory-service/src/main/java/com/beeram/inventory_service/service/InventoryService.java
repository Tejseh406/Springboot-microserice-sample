package com.beeram.inventory_service.service;

import com.beeram.inventory_service.dto.InventoryResponse;
import com.beeram.inventory_service.repo.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private  final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public List<InventoryResponse> isStockAvailable(List<String> skuCode){
       return inventoryRepository.findAllBySkuCodeIn(skuCode).stream().map(inventory ->
           InventoryResponse.builder()
                   .skuCode(inventory.getSkuCode())
                   .isInStock(inventory.getQuantity() > 0)
                   .build()
       ).toList();
    }
}
