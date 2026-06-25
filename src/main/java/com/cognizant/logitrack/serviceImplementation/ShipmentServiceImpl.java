package com.cognizant.logitrack.serviceImplementation;

import com.cognizant.logitrack.service.ShipmentService;
import com.cognizant.logitrack.exception.BadRequestException;
import com.cognizant.logitrack.exception.ResourceNotFoundException;
import com.cognizant.logitrack.dto.DeliveryEventDTO;
import com.cognizant.logitrack.dto.ShipmentDTO;
import com.cognizant.logitrack.entity.Carrier;
import com.cognizant.logitrack.entity.DeliveryEvent;
import com.cognizant.logitrack.entity.FreightOrder;
import com.cognizant.logitrack.entity.RateCard;
import com.cognizant.logitrack.entity.Shipment;
import com.cognizant.logitrack.enums.CarrierStatus;
import com.cognizant.logitrack.enums.FreightOrderStatus;
import com.cognizant.logitrack.enums.RateCardStatus;
import com.cognizant.logitrack.enums.ShipmentStatus;
import com.cognizant.logitrack.repository.CarrierRepository;
import com.cognizant.logitrack.repository.DeliveryEventRepository;
import com.cognizant.logitrack.repository.FreightOrderRepository;
import com.cognizant.logitrack.repository.RateCardRepository;
import com.cognizant.logitrack.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class ShipmentServiceImpl implements ShipmentService {

	private final ShipmentRepository shipmentRepository;
	private final FreightOrderRepository freightOrderRepository;
	private final DeliveryEventRepository deliveryEventRepository;
	private final CarrierRepository carrierRepository;
	private final RateCardRepository rateCardRepository;

	public ShipmentServiceImpl(ShipmentRepository shipmentRepository, FreightOrderRepository freightOrderRepository,
			DeliveryEventRepository deliveryEventRepository, CarrierRepository carrierRepository,
			RateCardRepository rateCardRepository) {
		this.shipmentRepository = shipmentRepository;
		this.freightOrderRepository = freightOrderRepository;
		this.deliveryEventRepository = deliveryEventRepository;
		this.carrierRepository = carrierRepository;
		this.rateCardRepository = rateCardRepository;
	}

	@Override
	public ShipmentDTO createShipment(ShipmentDTO dto) {

		FreightOrder freightOrder = freightOrderRepository.findById(dto.getFreightOrderId())
				.orElseThrow(() -> new BadRequestException("Freight order not found: " + dto.getFreightOrderId()));

		if (freightOrder.getStatus() == FreightOrderStatus.CANCELLED) {
			throw new BadRequestException("Cannot create shipment for cancelled freight order");
		}

		if (freightOrder.getStatus() == FreightOrderStatus.DELIVERED) {
			throw new BadRequestException("Cannot create shipment for delivered freight order");
		}

		if (freightOrder.getRoute() == null) {
			throw new BadRequestException("Freight order is not linked with any route");
		}

		Carrier carrier = carrierRepository.findById(dto.getCarrierId())
				.orElseThrow(() -> new BadRequestException("Carrier not found: " + dto.getCarrierId()));

		if (carrier.getStatus() != CarrierStatus.ACTIVE) {
			throw new BadRequestException("Carrier is not active: " + dto.getCarrierId());
		}

		LocalDate shipmentDate = dto.getDispatchDate() != null ? dto.getDispatchDate() : LocalDate.now();

		RateCard rateCard = rateCardRepository
				.findValidRateCard(carrier.getCarrierId(), freightOrder.getRoute().getRouteId(), RateCardStatus.ACTIVE,
						shipmentDate)
				.orElseThrow(() -> new BadRequestException("No active rate card found for carrierId: "
						+ carrier.getCarrierId() + " and routeId: " + freightOrder.getRoute().getRouteId()));

		BigDecimal freightCost = calculateFreightCost(freightOrder, rateCard);

		Shipment shipment = Shipment.builder().freightOrder(freightOrder).carrier(carrier).vehicleId(dto.getVehicleId())
				.driverId(dto.getDriverId()).rateCard(rateCard).freightCost(freightCost).dispatchDate(shipmentDate)
				.estimatedArrival(dto.getEstimatedArrival()).status(ShipmentStatus.DISPATCHED).build();

		Shipment saved = shipmentRepository.save(shipment);

		freightOrder.setStatus(FreightOrderStatus.INTRANSIT);
		freightOrderRepository.save(freightOrder);

		log.info("Shipment created: id={}, freightOrderId={}, carrierId={}, rateCardId={}, freightCost={}",
				saved.getShipmentId(), freightOrder.getFreightOrderId(), carrier.getCarrierId(),
				rateCard.getRateCardId(), freightCost);

		return toDTO(saved);
	}

	private BigDecimal calculateFreightCost(FreightOrder freightOrder, RateCard rateCard) {

		if (rateCard.getBaseRate() == null) {
			throw new BadRequestException("Rate card base rate cannot be null");
		}

		if (freightOrder.getWeight() == null) {
			throw new BadRequestException("Freight order weight cannot be null");
		}

		return rateCard.getBaseRate().multiply(freightOrder.getWeight());
	}

	@Override
	public ShipmentDTO getById(Integer id) {
		return toDTO(findEntity(id));
	}

	@Override
	public ShipmentDTO updateShipmentStatus(Integer id, ShipmentStatus status) {

		Shipment shipment = findEntity(id);
		shipment.setStatus(status);

		if (status == ShipmentStatus.DELIVERED) {
			shipment.setActualArrival(LocalDate.now());

			if (shipment.getFreightOrder() != null) {
				shipment.getFreightOrder().setStatus(FreightOrderStatus.DELIVERED);
				freightOrderRepository.save(shipment.getFreightOrder());
			}
		}

		Shipment saved = shipmentRepository.save(shipment);

		log.info("Shipment {} status updated to {}", id, status);

		return toDTO(saved);
	}

	@Override
	public DeliveryEventDTO addDeliveryEvent(Integer shipmentId, DeliveryEventDTO dto) {

		Shipment shipment = findEntity(shipmentId);

		DeliveryEvent event = DeliveryEvent.builder().shipment(shipment).eventType(dto.getEventType())
				.locationId(dto.getLocationId()).notes(dto.getNotes()).build();

		DeliveryEvent saved = deliveryEventRepository.save(event);

		log.info("Delivery event {} added to shipment {}", dto.getEventType(), shipmentId);

		return toEventDTO(saved);
	}

	@Override
	public List<DeliveryEventDTO> getEventsByShipment(Integer shipmentId) {
		return deliveryEventRepository.findByShipment_ShipmentId(shipmentId).stream().map(this::toEventDTO)
				.collect(Collectors.toList());
	}

	private Shipment findEntity(Integer id) {
		return shipmentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + id));
	}

	private ShipmentDTO toDTO(Shipment shipment) {
		return ShipmentDTO.builder().shipmentId(shipment.getShipmentId())
				.freightOrderId(
						shipment.getFreightOrder() != null ? shipment.getFreightOrder().getFreightOrderId() : null)
				.carrierId(shipment.getCarrier() != null ? shipment.getCarrier().getCarrierId() : null)
				.vehicleId(shipment.getVehicleId()).driverId(shipment.getDriverId())
				.rateCardId(shipment.getRateCard() != null ? shipment.getRateCard().getRateCardId() : null)
				.freightCost(shipment.getFreightCost()).dispatchDate(shipment.getDispatchDate())
				.estimatedArrival(shipment.getEstimatedArrival()).actualArrival(shipment.getActualArrival())
				.status(shipment.getStatus()).build();
	}

	private DeliveryEventDTO toEventDTO(DeliveryEvent event) {
		return DeliveryEventDTO.builder().eventId(event.getEventId())
				.shipmentId(event.getShipment() != null ? event.getShipment().getShipmentId() : null)
				.eventType(event.getEventType()).timestamp(event.getTimestamp()).locationId(event.getLocationId())
				.notes(event.getNotes()).build();
	}
}