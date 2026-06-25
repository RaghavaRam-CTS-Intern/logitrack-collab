package com.cognizant.logitrack.service;

import com.cognizant.logitrack.dto.RouteDTO;
import com.cognizant.logitrack.enums.RouteMode;
import com.cognizant.logitrack.enums.RouteStatus;
import java.util.List;

public interface RouteService {
    RouteDTO addRoute(RouteDTO dto);
    List<RouteDTO> getAllRoutes();
    List<RouteDTO> getByMode(RouteMode mode);
    RouteDTO getById(Integer id);
    RouteDTO updateRouteStatus(Integer id, RouteStatus status);
    void deleteRoute(Integer id);
}
