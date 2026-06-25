package com.cognizant.logitrack.serviceImplementation;

import com.cognizant.logitrack.service.RateCardService;
import com.cognizant.logitrack.exception.BadRequestException;
import com.cognizant.logitrack.exception.ResourceNotFoundException;
import com.cognizant.logitrack.dto.RateCardDTO;
import com.cognizant.logitrack.entity.Carrier;
import com.cognizant.logitrack.entity.RateCard;
import com.cognizant.logitrack.entity.Route;
import com.cognizant.logitrack.enums.CarrierStatus;
import com.cognizant.logitrack.enums.RateCardStatus;
import com.cognizant.logitrack.enums.RouteStatus;
import com.cognizant.logitrack.repository.CarrierRepository;
import com.cognizant.logitrack.repository.RateCardRepository;
import com.cognizant.logitrack.repository.RouteRepository;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RateCardServiceImpl implements RateCardService {

    private final RateCardRepository rateCardRepository;
    private final CarrierRepository carrierRepository;
    private final RouteRepository routeRepository;

    public RateCardServiceImpl(
            RateCardRepository rateCardRepository,
            CarrierRepository carrierRepository,
            RouteRepository routeRepository
    ) {
        this.rateCardRepository = rateCardRepository;
        this.carrierRepository = carrierRepository;
        this.routeRepository = routeRepository;
    }

    @Override
    public RateCardDTO addRateCard(RateCardDTO dto) {

        Carrier carrier = carrierRepository.findById(dto.getCarrierId())
                .orElseThrow(() -> new BadRequestException(
                        "Carrier not found with id: " + dto.getCarrierId()
                ));

        Route route = routeRepository.findById(dto.getRouteId())
                .orElseThrow(() -> new BadRequestException(
                        "Route not found with id: " + dto.getRouteId()
                ));

        if (carrier.getStatus() != CarrierStatus.ACTIVE) {
            throw new BadRequestException(
                    "Cannot create rate card because carrier is not active. carrierId: "
                            + carrier.getCarrierId()
            );
        }

        if (route.getStatus() != RouteStatus.ACTIVE) {
            throw new BadRequestException(
                    "Cannot create rate card because route is not active. routeId: "
                            + route.getRouteId()
            );
        }

        if (carrier.getMode() != route.getMode()) {
            throw new BadRequestException(
                    "Carrier mode and route mode must match. Carrier mode: "
                            + carrier.getMode()
                            + ", Route mode: "
                            + route.getMode()
            );
        }

        RateCard rateCard = RateCard.builder()
                .carrier(carrier)
                .route(route)
                .baseRate(dto.getBaseRate())
                .weightSlab(dto.getWeightSlab())
                .effectiveDate(dto.getEffectiveDate())
                .expiryDate(dto.getExpiryDate())
                .status(RateCardStatus.ACTIVE)
                .build();

        RateCard saved = rateCardRepository.save(rateCard);

        log.info(
                "Rate card added: id={}, carrierId={}, routeId={}, mode={}",
                saved.getRateCardId(),
                carrier.getCarrierId(),
                route.getRouteId(),
                route.getMode()
        );

        return toDTO(saved);
    }

    @Override
    public List<RateCardDTO> getRateCardsByCarrier(Integer carrierId) {
        return rateCardRepository.findByCarrier_CarrierId(carrierId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RateCardDTO> getRateCardsByRoute(Integer routeId) {
        return rateCardRepository.findByRoute_RouteId(routeId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RateCardDTO getById(Integer id) {
        return toDTO(findEntity(id));
    }

    @Override
    public RateCardDTO updateStatus(Integer id, RateCardStatus status) {
        RateCard rateCard = findEntity(id);
        rateCard.setStatus(status);
        return toDTO(rateCardRepository.save(rateCard));
    }

    private RateCard findEntity(Integer id) {
        return rateCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Rate card not found with id: " + id
                ));
    }

    private RateCardDTO toDTO(RateCard rc) {
        return RateCardDTO.builder()
                .rateCardId(rc.getRateCardId())
                .carrierId(rc.getCarrier() != null ? rc.getCarrier().getCarrierId() : null)
                .routeId(rc.getRoute() != null ? rc.getRoute().getRouteId() : null)
                .baseRate(rc.getBaseRate())
                .weightSlab(rc.getWeightSlab())
                .effectiveDate(rc.getEffectiveDate())
                .expiryDate(rc.getExpiryDate())
                .status(rc.getStatus())
                .build();
    }
}