package com.cognizant.logitrack;

import com.cognizant.logitrack.dto.InventoryDTO;
import com.cognizant.logitrack.entity.WarehouseInventory;
import com.cognizant.logitrack.repository.WarehouseInventoryRepository;
import com.cognizant.logitrack.service.InventoryService;
import com.cognizant.logitrack.serviceImplementation.InventoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private WarehouseInventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @Test
    void getInventory_byWarehouse_returnsList() {
        WarehouseInventory inv = WarehouseInventory.builder().inventoryId(1).warehouseId(1)
                .quantityOnHand(100).quantityReserved(0).build();
        when(inventoryRepository.findByWarehouseId(1)).thenReturn(List.of(inv));

        List<InventoryDTO> result = inventoryService.getInventoryByWarehouse(1);

        assertEquals(1, result.size());
    }

    @Test
    void reserveStock_sufficientQty_reducesOnHand() {
        WarehouseInventory inv = WarehouseInventory.builder().inventoryId(1)
                .quantityOnHand(100).quantityReserved(0).build();
        when(inventoryRepository.findById(1)).thenReturn(Optional.of(inv));
        when(inventoryRepository.save(any(WarehouseInventory.class))).thenAnswer(i -> i.getArgument(0));

        InventoryDTO result = inventoryService.reserveStock(1, 30);

        assertEquals(70, result.getQuantityOnHand());
        assertEquals(30, result.getQuantityReserved());
    }

    @Test
    void releaseStock_restoresOnHand() {
        WarehouseInventory inv = WarehouseInventory.builder().inventoryId(1)
                .quantityOnHand(70).quantityReserved(30).build();
        when(inventoryRepository.findById(1)).thenReturn(Optional.of(inv));
        when(inventoryRepository.save(any(WarehouseInventory.class))).thenAnswer(i -> i.getArgument(0));

        InventoryDTO result = inventoryService.releaseStock(1, 30);

        assertEquals(100, result.getQuantityOnHand());
        assertEquals(0, result.getQuantityReserved());
    }
}
