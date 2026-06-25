package com.cognizant.logitrack;

import com.cognizant.logitrack.dto.RouteDTO;
import com.cognizant.logitrack.entity.Route;
import com.cognizant.logitrack.enums.RouteMode;
import com.cognizant.logitrack.enums.RouteStatus;
import com.cognizant.logitrack.repository.RouteRepository;
import com.cognizant.logitrack.service.RouteService;
import com.cognizant.logitrack.serviceImplementation.RouteServiceImpl;
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
class RouteServiceTest {

    @Mock
    private RouteRepository routeRepository;

    @InjectMocks
    private RouteServiceImpl routeService;

    @Test
    void addRoute_valid_returnsDTO() {
        when(routeRepository.save(any(Route.class))).thenAnswer(inv -> {
            Route r = inv.getArgument(0);
            r.setRouteId(1);
            return r;
        });

        RouteDTO dto = RouteDTO.builder().originHubId(1).destinationHubId(2).transitDays(3).mode(RouteMode.ROAD).build();
        RouteDTO result = routeService.addRoute(dto);

        assertNotNull(result);
        assertEquals(RouteStatus.ACTIVE, result.getStatus());
        assertEquals(RouteMode.ROAD, result.getMode());
    }

    @Test
    void getAllRoutes_returnsList() {
        Route r1 = Route.builder().routeId(1).mode(RouteMode.ROAD).status(RouteStatus.ACTIVE).build();
        Route r2 = Route.builder().routeId(2).mode(RouteMode.AIR).status(RouteStatus.ACTIVE).build();
        when(routeRepository.findAll()).thenReturn(List.of(r1, r2));

        List<RouteDTO> result = routeService.getAllRoutes();

        assertEquals(2, result.size());
    }

    @Test
    void updateStatus_changesToInactive() {
        Route route = Route.builder().routeId(1).status(RouteStatus.ACTIVE).build();
        when(routeRepository.findById(1)).thenReturn(Optional.of(route));
        when(routeRepository.save(any(Route.class))).thenAnswer(inv -> inv.getArgument(0));

        RouteDTO result = routeService.updateRouteStatus(1, RouteStatus.INACTIVE);

        assertEquals(RouteStatus.INACTIVE, result.getStatus());
    }
}
