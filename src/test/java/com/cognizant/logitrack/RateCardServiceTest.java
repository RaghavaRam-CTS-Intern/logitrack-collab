package com.cognizant.logitrack;

import com.cognizant.logitrack.exception.BadRequestException;
import com.cognizant.logitrack.dto.RateCardDTO;
import com.cognizant.logitrack.entity.Carrier;
import com.cognizant.logitrack.entity.RateCard;
import com.cognizant.logitrack.entity.Route;
import com.cognizant.logitrack.repository.CarrierRepository;
import com.cognizant.logitrack.repository.RateCardRepository;
import com.cognizant.logitrack.repository.RouteRepository;
import com.cognizant.logitrack.service.RateCardService;
import com.cognizant.logitrack.serviceImplementation.RateCardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateCardServiceTest {

    @Mock
    private RateCardRepository rateCardRepository;

    @Mock
    private CarrierRepository carrierRepository;

    @Mock
    private RouteRepository routeRepository;

    @InjectMocks
    private RateCardServiceImpl rateCardService;

    private RateCardDTO request() {
        return RateCardDTO.builder().carrierId(1).routeId(1).baseRate(new BigDecimal("500.0")).weightSlab("0-100kg")
                .effectiveDate(LocalDate.now()).expiryDate(LocalDate.now().plusYears(1)).build();
    }

    @Test
    void addRateCard_validCarrierAndRoute_saved() {
        when(carrierRepository.findById(1)).thenReturn(Optional.of(Carrier.builder().carrierId(1).build()));
        when(routeRepository.findById(1)).thenReturn(Optional.of(Route.builder().routeId(1).build()));
        when(rateCardRepository.save(any(RateCard.class))).thenAnswer(inv -> {
            RateCard rc = inv.getArgument(0);
            rc.setRateCardId(1);
            return rc;
        });

        RateCardDTO result = rateCardService.addRateCard(request());

        assertNotNull(result);
        assertEquals(1, result.getCarrierId());
        assertEquals(1, result.getRouteId());
    }

    @Test
    void addRateCard_invalidCarrier_throwsBadRequestException() {
        when(carrierRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> rateCardService.addRateCard(request()));
    }

    @Test
    void getRateCardsByCarrier_returnsList() {
        RateCard rc = RateCard.builder().rateCardId(1)
                .carrier(Carrier.builder().carrierId(1).build())
                .route(Route.builder().routeId(1).build())
                .build();
        when(rateCardRepository.findByCarrier_CarrierId(1)).thenReturn(List.of(rc));

        List<RateCardDTO> result = rateCardService.getRateCardsByCarrier(1);

        assertEquals(1, result.size());
    }
}
