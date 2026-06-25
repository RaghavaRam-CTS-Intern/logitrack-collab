package com.cognizant.logitrack;

import com.cognizant.logitrack.exception.BadRequestException;
import com.cognizant.logitrack.dto.PurchaseOrderDTO;
import com.cognizant.logitrack.entity.PurchaseOrder;
import com.cognizant.logitrack.entity.Supplier;
import com.cognizant.logitrack.enums.POStatus;
import com.cognizant.logitrack.repository.PurchaseOrderRepository;
import com.cognizant.logitrack.repository.SupplierRepository;
import com.cognizant.logitrack.service.PurchaseOrderService;
import com.cognizant.logitrack.serviceImplementation.PurchaseOrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private PurchaseOrderServiceImpl purchaseOrderService;

    private PurchaseOrderDTO request() {
        return PurchaseOrderDTO.builder().supplierId(1).warehouseId(1).lineItems("[]").totalValue(new BigDecimal("1000.0"))
                .orderDate(LocalDate.now()).expectedDelivery(LocalDate.now().plusDays(10)).build();
    }

    @Test
    void createPO_validSupplier_savedAsDraft() {
        when(supplierRepository.findById(1)).thenReturn(Optional.of(Supplier.builder().supplierId(1).build()));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(inv -> {
            PurchaseOrder po = inv.getArgument(0);
            po.setPoId(1);
            return po;
        });

        PurchaseOrderDTO result = purchaseOrderService.createPO(request());

        assertNotNull(result);
        assertEquals(POStatus.DRAFT, result.getStatus());
        assertEquals(1, result.getSupplierId());
    }

    @Test
    void createPO_invalidSupplier_throwsBadRequestException() {
        when(supplierRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> purchaseOrderService.createPO(request()));
    }

    @Test
    void updatePOStatus_draftToPlaced() {
        PurchaseOrder po = PurchaseOrder.builder().poId(1).status(POStatus.DRAFT).build();
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(po));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseOrderDTO result = purchaseOrderService.updatePOStatus(1, POStatus.PLACED);

        assertEquals(POStatus.PLACED, result.getStatus());
    }
}
