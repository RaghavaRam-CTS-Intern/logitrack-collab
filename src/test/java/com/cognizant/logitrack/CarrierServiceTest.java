package com.cognizant.logitrack;

import com.cognizant.logitrack.exception.ResourceNotFoundException;
import com.cognizant.logitrack.dto.CarrierDTO;
import com.cognizant.logitrack.entity.Carrier;
import com.cognizant.logitrack.enums.CarrierServiceLevel;
import com.cognizant.logitrack.enums.CarrierStatus;
import com.cognizant.logitrack.enums.RouteMode;
import com.cognizant.logitrack.repository.CarrierRepository;
import com.cognizant.logitrack.service.CarrierService;
import com.cognizant.logitrack.serviceImplementation.CarrierServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarrierServiceTest {

    @Mock
    private CarrierRepository carrierRepository;

    @InjectMocks
    private CarrierServiceImpl carrierService;

    @Test
    void addCarrier_valid_returnsDTO() {
        when(carrierRepository.save(any(Carrier.class))).thenAnswer(inv -> {
            Carrier c = inv.getArgument(0);
            c.setCarrierId(1);
            return c;
        });

        CarrierDTO dto = CarrierDTO.builder().name("FastCarrier").mode(RouteMode.ROAD)
                .serviceLevel(CarrierServiceLevel.EXPRESS).contactDetails("contact@fast.com").build();
        CarrierDTO result = carrierService.addCarrier(dto);

        assertNotNull(result);
        assertEquals("FastCarrier", result.getName());
        assertEquals(CarrierStatus.ACTIVE, result.getStatus());
    }

    @Test
    void getCarrierById_invalid_throwsResourceNotFoundException() {
        when(carrierRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> carrierService.getById(99));
    }

    @Test
    void updateCarrierStatus() {
        Carrier carrier = Carrier.builder().carrierId(1).status(CarrierStatus.ACTIVE).build();
        when(carrierRepository.findById(1)).thenReturn(Optional.of(carrier));
        when(carrierRepository.save(any(Carrier.class))).thenAnswer(inv -> inv.getArgument(0));

        CarrierDTO result = carrierService.updateCarrierStatus(1, CarrierStatus.SUSPENDED);

        assertEquals(CarrierStatus.SUSPENDED, result.getStatus());
    }
}
