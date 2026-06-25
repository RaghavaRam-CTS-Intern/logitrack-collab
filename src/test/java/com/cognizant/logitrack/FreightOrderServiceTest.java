package com.cognizant.logitrack;

import com.cognizant.logitrack.exception.BadRequestException;
import com.cognizant.logitrack.dto.FreightOrderDTO;
import com.cognizant.logitrack.entity.FreightOrder;
import com.cognizant.logitrack.enums.FreightOrderStatus;
import com.cognizant.logitrack.repository.FreightOrderRepository;
import com.cognizant.logitrack.service.FreightOrderService;
import com.cognizant.logitrack.serviceImplementation.FreightOrderServiceImpl;
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
class FreightOrderServiceTest {

    @Mock
    private FreightOrderRepository freightOrderRepository;

    @InjectMocks
    private FreightOrderServiceImpl freightOrderService;

    private FreightOrderDTO request() {
        return FreightOrderDTO.builder().shipperId(1).originLocationId(10).destinationLocationId(20).cargoDescription("Electronics").weight(new BigDecimal("100.5")).volume(new BigDecimal("2.0")).requiredDeliveryDate(LocalDate.now().plusDays(5)).build();
    }

    @Test
    void createOrder_validInput_savedWithDraftStatus() {
        when(freightOrderRepository.save(any(FreightOrder.class))).thenAnswer(inv -> {
            FreightOrder fo = inv.getArgument(0);
            fo.setFreightOrderId(1);
            return fo;
        });

        FreightOrderDTO result = freightOrderService.createFreightOrder(request());

        assertNotNull(result);
        assertEquals(FreightOrderStatus.DRAFT, result.getStatus());
    }

    @Test
    void cancelOrder_bookedStatus_setsCancelled() {
        FreightOrder order = FreightOrder.builder().freightOrderId(1).status(FreightOrderStatus.BOOKED).build();
        when(freightOrderRepository.findById(1)).thenReturn(Optional.of(order));
        when(freightOrderRepository.save(any(FreightOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        FreightOrderDTO result = freightOrderService.cancelOrder(1);

        assertEquals(FreightOrderStatus.CANCELLED, result.getStatus());
    }

    @Test
    void cancelOrder_deliveredStatus_throwsBadRequestException() {
        FreightOrder order = FreightOrder.builder().freightOrderId(1).status(FreightOrderStatus.DELIVERED).build();
        when(freightOrderRepository.findById(1)).thenReturn(Optional.of(order));

        assertThrows(BadRequestException.class, () -> freightOrderService.cancelOrder(1));
    }
}
