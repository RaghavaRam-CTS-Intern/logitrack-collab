package com.cognizant.logitrack;

import com.cognizant.logitrack.dto.DeliveryEventDTO;
import com.cognizant.logitrack.dto.ShipmentDTO;
import com.cognizant.logitrack.entity.DeliveryEvent;
import com.cognizant.logitrack.entity.FreightOrder;
import com.cognizant.logitrack.entity.Shipment;
import com.cognizant.logitrack.enums.EventType;
import com.cognizant.logitrack.enums.ShipmentStatus;
import com.cognizant.logitrack.repository.DeliveryEventRepository;
import com.cognizant.logitrack.repository.FreightOrderRepository;
import com.cognizant.logitrack.repository.ShipmentRepository;
import com.cognizant.logitrack.service.ShipmentService;
import com.cognizant.logitrack.serviceImplementation.ShipmentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private FreightOrderRepository freightOrderRepository;

    @Mock
    private DeliveryEventRepository deliveryEventRepository;

    @InjectMocks
    private ShipmentServiceImpl shipmentService;

    @Test
    void createShipment_validOrder_savedWithDispatchedStatus() {
        FreightOrder order = FreightOrder.builder().freightOrderId(1).build();
        when(freightOrderRepository.findById(1)).thenReturn(Optional.of(order));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(inv -> {
            Shipment s = inv.getArgument(0);
            s.setShipmentId(1);
            return s;
        });

        ShipmentDTO dto = ShipmentDTO.builder().freightOrderId(1).carrierId(5).vehicleId(7).driverId(9).dispatchDate(LocalDate.now()).estimatedArrival(LocalDate.now().plusDays(3)).build();
        ShipmentDTO result = shipmentService.createShipment(dto);

        assertNotNull(result);
        assertEquals(ShipmentStatus.DISPATCHED, result.getStatus());
        assertEquals(1, result.getFreightOrderId());
    }

    @Test
    void updateStatus_dispatchedToDelivered_updates() {
        Shipment shipment = Shipment.builder().shipmentId(1).status(ShipmentStatus.DISPATCHED).build();
        when(shipmentRepository.findById(1)).thenReturn(Optional.of(shipment));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(inv -> inv.getArgument(0));

        ShipmentDTO result = shipmentService.updateShipmentStatus(1, ShipmentStatus.DELIVERED);

        assertEquals(ShipmentStatus.DELIVERED, result.getStatus());
    }

    @Test
    void addEvent_savedCorrectly() {
        Shipment shipment = Shipment.builder().shipmentId(1).build();
        when(shipmentRepository.findById(1)).thenReturn(Optional.of(shipment));
        when(deliveryEventRepository.save(any(DeliveryEvent.class))).thenAnswer(inv -> {
            DeliveryEvent e = inv.getArgument(0);
            e.setEventId(1);
            return e;
        });

        DeliveryEventDTO dto = DeliveryEventDTO.builder().eventType(EventType.PICKUP).locationId(100).notes("Picked up").build();
        DeliveryEventDTO result = shipmentService.addDeliveryEvent(1, dto);

        assertNotNull(result);
        assertEquals(EventType.PICKUP, result.getEventType());
        assertEquals(1, result.getShipmentId());
    }
}
