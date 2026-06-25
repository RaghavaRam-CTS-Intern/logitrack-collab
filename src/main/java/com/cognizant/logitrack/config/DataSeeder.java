package com.cognizant.logitrack.config;
 
import com.cognizant.logitrack.entity.AuditLog;
import com.cognizant.logitrack.entity.User;
import com.cognizant.logitrack.enums.Role;
import com.cognizant.logitrack.enums.UserStatus;
import com.cognizant.logitrack.repository.AuditLogRepository;
import com.cognizant.logitrack.repository.UserRepository;
import com.cognizant.logitrack.entity.Notification;
import com.cognizant.logitrack.entity.PickList;
import com.cognizant.logitrack.entity.PurchaseOrder;
import com.cognizant.logitrack.enums.NotificationCategory;
import com.cognizant.logitrack.enums.NotificationStatus;
import com.cognizant.logitrack.repository.NotificationRepository;
import com.cognizant.logitrack.repository.PickListRepository;
import com.cognizant.logitrack.repository.PurchaseOrderRepository;
import com.cognizant.logitrack.entity.Carrier;
import com.cognizant.logitrack.entity.ComplianceFlag;
import com.cognizant.logitrack.entity.DeliveryEvent;
import com.cognizant.logitrack.entity.FreightOrder;
import com.cognizant.logitrack.entity.InboundReceipt;
import com.cognizant.logitrack.entity.LogisticsReport;
import com.cognizant.logitrack.entity.RateCard;
import com.cognizant.logitrack.entity.Route;
import com.cognizant.logitrack.entity.Shipment;
import com.cognizant.logitrack.entity.ShipmentDocument;
import com.cognizant.logitrack.enums.*;
import com.cognizant.logitrack.repository.CarrierRepository;
import com.cognizant.logitrack.repository.ComplianceFlagRepository;
import com.cognizant.logitrack.repository.DeliveryEventRepository;
import com.cognizant.logitrack.repository.FreightOrderRepository;
import com.cognizant.logitrack.repository.InboundReceiptRepository;
import com.cognizant.logitrack.repository.LogisticsReportRepository;
import com.cognizant.logitrack.repository.RateCardRepository;
import com.cognizant.logitrack.repository.RouteRepository;
import com.cognizant.logitrack.repository.ShipmentDocumentRepository;
import com.cognizant.logitrack.repository.ShipmentRepository;
import com.cognizant.logitrack.entity.Supplier;
import com.cognizant.logitrack.enums.SupplierStatus;
import com.cognizant.logitrack.repository.SupplierRepository;
import com.cognizant.logitrack.entity.WarehouseInventory;
import com.cognizant.logitrack.repository.WarehouseInventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
 
@Component
@Slf4j
public class DataSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final CarrierRepository carrierRepository;
    private final RouteRepository routeRepository;
    private final RateCardRepository rateCardRepository;
    private final SupplierRepository supplierRepository;
    private final WarehouseInventoryRepository inventoryRepository;
    private final NotificationRepository notificationRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final FreightOrderRepository freightOrderRepository;
    private final ShipmentRepository shipmentRepository;
    private final DeliveryEventRepository deliveryEventRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final InboundReceiptRepository inboundReceiptRepository;
    private final PickListRepository pickListRepository;
    private final ShipmentDocumentRepository shipmentDocumentRepository;
    private final ComplianceFlagRepository complianceFlagRepository;
    private final LogisticsReportRepository logisticsReportRepository;
 
    
 
    public DataSeeder(UserRepository userRepository, AuditLogRepository auditLogRepository,
			CarrierRepository carrierRepository, RouteRepository routeRepository, RateCardRepository rateCardRepository,
			SupplierRepository supplierRepository, WarehouseInventoryRepository inventoryRepository,
			NotificationRepository notificationRepository, BCryptPasswordEncoder passwordEncoder,
			FreightOrderRepository freightOrderRepository, ShipmentRepository shipmentRepository,
			DeliveryEventRepository deliveryEventRepository, PurchaseOrderRepository purchaseOrderRepository,
			InboundReceiptRepository inboundReceiptRepository, PickListRepository pickListRepository,
			ShipmentDocumentRepository shipmentDocumentRepository, ComplianceFlagRepository complianceFlagRepository,
			LogisticsReportRepository logisticsReportRepository) {
		super();
		this.userRepository = userRepository;
		this.auditLogRepository = auditLogRepository;
		this.carrierRepository = carrierRepository;
		this.routeRepository = routeRepository;
		this.rateCardRepository = rateCardRepository;
		this.supplierRepository = supplierRepository;
		this.inventoryRepository = inventoryRepository;
		this.notificationRepository = notificationRepository;
		this.passwordEncoder = passwordEncoder;
		this.freightOrderRepository = freightOrderRepository;
		this.shipmentRepository = shipmentRepository;
		this.deliveryEventRepository = deliveryEventRepository;
		this.purchaseOrderRepository = purchaseOrderRepository;
		this.inboundReceiptRepository = inboundReceiptRepository;
		this.pickListRepository = pickListRepository;
		this.shipmentDocumentRepository = shipmentDocumentRepository;
		this.complianceFlagRepository = complianceFlagRepository;
		this.logisticsReportRepository = logisticsReportRepository;
	}
 
	@Override
    public void run(String... args) {
        seedUsers();
        seedAuditLogs();
        seedCarriers();
        seedRoutes();
        seedRateCards();
        seedSuppliers();
        seedInventory();
        seedNotifications();
        seedFreightOrders();
        seedShipments();
        seedDeliveryEvents();
        seedPurchaseOrders();
        seedInboundReceipts();
        seedPickLists();
        seedShipmentDocuments();
        seedComplianceFlags();
        seedLogisticsReports();
        log.info("Data seeding complete.");
    }
 
    private void seedUsers() {
        if (userRepository.count() != 0) {
            return;
        }
        String hash = passwordEncoder.encode("password123");
        Role[] roles = {Role.SHIPPER, Role.COORDINATOR, Role.WAREHOUSEOPS, Role.DRIVER, Role.COMPLIANCE, Role.ANALYST, Role.ADMIN};
        for (Role role : roles) {
            String emailPrefix = role.name().toLowerCase();
            User user = User.builder().name(role.name() + " User").role(role).email(emailPrefix + "@logitrack.com").phone("9000000000").hubId(1).passwordHash(hash).status(UserStatus.ACTIVE).build();
            userRepository.save(user);
        }
        log.info("Seeded {} users", roles.length);
    }
 
    private void seedAuditLogs() {
        if (auditLogRepository.count() != 0) {
            return;
        }
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            return;
        }
        User u1 = users.get(0);
        User u2 = users.size() > 1 ? users.get(1) : u1;
        List<AuditLog> logs = new ArrayList<>();
        logs.add(AuditLog.builder().user(u1).action("USER_CREATED").entityType("User").build());
        logs.add(AuditLog.builder().user(u1).action("LOGIN").entityType("User").build());
        logs.add(AuditLog.builder().user(u2).action("FREIGHT_ORDER_CREATED").entityType("FreightOrder").build());
        auditLogRepository.saveAll(logs);
        log.info("Seeded {} audit logs", logs.size());
    }
 
    private void seedCarriers() {
        if (carrierRepository.count() != 0) {
            return;
        }
        carrierRepository.save(Carrier.builder().name("Roadways Express").mode(RouteMode.ROAD).serviceLevel(CarrierServiceLevel.STANDARD).contactDetails("contact@roadways.com").status(CarrierStatus.ACTIVE).build());
        carrierRepository.save(Carrier.builder().name("SkyCargo Air").mode(RouteMode.AIR).serviceLevel(CarrierServiceLevel.EXPRESS).contactDetails("contact@skycargo.com").status(CarrierStatus.ACTIVE).build());
        carrierRepository.save(Carrier.builder().name("OceanLine Shipping").mode(RouteMode.SEA).serviceLevel(CarrierServiceLevel.STANDARD).contactDetails("contact@oceanline.com").status(CarrierStatus.ACTIVE).build());
        log.info("Seeded 3 carriers");
    }
 
    private void seedRoutes() {
        if (routeRepository.count() != 0) {
            return;
        }
        routeRepository.save(Route.builder().originHubId(1).destinationHubId(2).transitDays(2).mode(RouteMode.ROAD).status(RouteStatus.ACTIVE).build());
        routeRepository.save(Route.builder().originHubId(1).destinationHubId(3).transitDays(1).mode(RouteMode.AIR).status(RouteStatus.ACTIVE).build());
        routeRepository.save(Route.builder().originHubId(2).destinationHubId(4).transitDays(5).mode(RouteMode.SEA).status(RouteStatus.ACTIVE).build());
        routeRepository.save(Route.builder().originHubId(3).destinationHubId(4).transitDays(3).mode(RouteMode.RAIL).status(RouteStatus.ACTIVE).build());
        log.info("Seeded 4 routes");
    }
 
    private void seedRateCards() {
        if (rateCardRepository.count() != 0) {
            return;
        }

        List<Carrier> carriers = carrierRepository.findAll();
        List<Route> routes = routeRepository.findAll();

        if (carriers.isEmpty() || routes.isEmpty()) {
            return;
        }

        for (Route route : routes) {
            Carrier matchingCarrier = carriers.stream()
                    .filter(carrier -> carrier.getMode() == route.getMode())
                    .filter(carrier -> carrier.getStatus() == CarrierStatus.ACTIVE)
                    .findFirst()
                    .orElse(null);

            if (matchingCarrier == null) {
                log.warn("No matching carrier found for routeId={} and mode={}",
                        route.getRouteId(),
                        route.getMode());
                continue;
            }

            rateCardRepository.save(
                    RateCard.builder()
                            .carrier(matchingCarrier)
                            .route(route)
                            .baseRate(new BigDecimal("50.00"))
                            .weightSlab("0-100kg")
                            .effectiveDate(LocalDate.now().minusDays(1))
                            .expiryDate(LocalDate.now().plusYears(1))
                            .status(RateCardStatus.ACTIVE)
                            .build()
            );
        }

        log.info("Seeded rate cards with matching carrier and route modes");
    }
 
    private void seedSuppliers() {
        if (supplierRepository.count() != 0) {
            return;
        }
        supplierRepository.save(Supplier.builder().name("Acme Electronics").category("Electronics").contactDetails("acme@suppliers.com").leadTimeDays(7).status(SupplierStatus.ACTIVE).build());
        supplierRepository.save(Supplier.builder().name("DailyGoods FMCG").category("FMCG").contactDetails("dailygoods@suppliers.com").leadTimeDays(3).status(SupplierStatus.ACTIVE).build());
        supplierRepository.save(Supplier.builder().name("AutoParts Co").category("Automotive").contactDetails("autoparts@suppliers.com").leadTimeDays(14).status(SupplierStatus.ACTIVE).build());
        log.info("Seeded 3 suppliers");
    }
 
    private void seedInventory() {
        if (inventoryRepository.count() != 0) {
            return;
        }
        for (int i = 1; i <= 10; i++) {
            int warehouseId = (i % 2 == 0) ? 2 : 1;
            inventoryRepository.save(WarehouseInventory.builder().sku("SKU-" + String.format("%04d", i)).productName("Product " + i).warehouseId(warehouseId).binLocation("A-" + i).quantityOnHand(100 + i * 10).quantityReserved(0).build());
        }
        log.info("Seeded 10 inventory items");
    }
 
    private void seedNotifications() {
        if (notificationRepository.count() != 0) {
            return;
        }
        notificationRepository.save(Notification.builder().userId(1).message("Welcome to LogiTrack").category(NotificationCategory.SHIPMENT).status(NotificationStatus.UNREAD).build());
        notificationRepository.save(Notification.builder().userId(2).message("New freight order assigned").category(NotificationCategory.SHIPMENT).status(NotificationStatus.UNREAD).build());
        notificationRepository.save(Notification.builder().userId(3).message("Inventory low in warehouse 1").category(NotificationCategory.WAREHOUSE).status(NotificationStatus.UNREAD).build());
        notificationRepository.save(Notification.builder().userId(4).message("Delivery scheduled for tomorrow").category(NotificationCategory.CARRIER).status(NotificationStatus.UNREAD).build());
        notificationRepository.save(Notification.builder().userId(5).message("Compliance document pending review").category(NotificationCategory.COMPLIANCE).status(NotificationStatus.UNREAD).build());
        log.info("Seeded 5 notifications");
    }
    
    private BigDecimal calculateSeedFreightCost(FreightOrder freightOrder, RateCard rateCard) {
        if (freightOrder.getWeight() == null || rateCard.getBaseRate() == null) {
            return BigDecimal.ZERO;
        }

        return rateCard.getBaseRate().multiply(freightOrder.getWeight());
    }

    // New Seedings
    private void seedFreightOrders() {
        if (freightOrderRepository.count() != 0) {
            return;
        }

        User shipper = userRepository.findAll()
                .stream()
                .filter(u -> u.getRole() == Role.SHIPPER)
                .findFirst()
                .orElse(null);

        if (shipper == null) {
            return;
        }

        Route route1 = routeRepository
                .findFirstByOriginHubIdAndDestinationHubIdAndStatus(
                        1,
                        2,
                        RouteStatus.ACTIVE
                )
                .orElse(null);

        Route route2 = routeRepository
                .findFirstByOriginHubIdAndDestinationHubIdAndStatus(
                        2,
                        4,
                        RouteStatus.ACTIVE
                )
                .orElse(null);

        if (route1 == null || route2 == null) {
            log.warn("Skipping freight order seeding because required active routes are missing");
            return;
        }

        freightOrderRepository.save(
                FreightOrder.builder()
                        .shipperId(shipper.getUserId())
                        .originLocationId(1)
                        .destinationLocationId(2)
                        .route(route1)
                        .cargoDescription("Electronic Components")
                        .weight(new BigDecimal("250.50"))
                        .volume(new BigDecimal("12.00"))
                        .requiredDeliveryDate(LocalDate.now().plusDays(5))
                        .status(FreightOrderStatus.BOOKED)
                        .build()
        );

        freightOrderRepository.save(
                FreightOrder.builder()
                        .shipperId(shipper.getUserId())
                        .originLocationId(2)
                        .destinationLocationId(4)
                        .route(route2)
                        .cargoDescription("Automotive Parts")
                        .weight(new BigDecimal("500.00"))
                        .volume(new BigDecimal("25.50"))
                        .requiredDeliveryDate(LocalDate.now().plusDays(8))
                        .status(FreightOrderStatus.INTRANSIT)
                        .build()
        );

        log.info("Seeded freight orders with linked routes");
    }
    private void seedShipments() {
        if (shipmentRepository.count() != 0) {
            return;
        }

        List<FreightOrder> orders = freightOrderRepository.findAll();

        User driver = userRepository.findAll()
                .stream()
                .filter(u -> u.getRole() == Role.DRIVER)
                .findFirst()
                .orElse(null);

        if (orders.isEmpty() || driver == null) {
            return;
        }

        for (FreightOrder order : orders) {

            if (order.getRoute() == null) {
                log.warn("Skipping shipment seed for freightOrderId={} because route is missing",
                        order.getFreightOrderId());
                continue;
            }

            RateCard rateCard = rateCardRepository
                    .findByRoute_RouteId(order.getRoute().getRouteId())
                    .stream()
                    .filter(rc -> rc.getStatus() == RateCardStatus.ACTIVE)
                    .filter(rc -> rc.getCarrier() != null)
                    .filter(rc -> rc.getCarrier().getStatus() == CarrierStatus.ACTIVE)
                    .findFirst()
                    .orElse(null);

            if (rateCard == null) {
                log.warn("Skipping shipment seed for freightOrderId={} because no active rate card found for routeId={}",
                        order.getFreightOrderId(),
                        order.getRoute().getRouteId());
                continue;
            }

            Carrier carrier = rateCard.getCarrier();

            BigDecimal freightCost = calculateSeedFreightCost(order, rateCard);

            shipmentRepository.save(
                    Shipment.builder()
                            .freightOrder(order)
                            .carrier(carrier)
                            .vehicleId(100 + order.getFreightOrderId())
                            .driverId(driver.getUserId())
                            .rateCard(rateCard)
                            .freightCost(freightCost)
                            .dispatchDate(LocalDate.now().minusDays(1))
                            .estimatedArrival(LocalDate.now().plusDays(order.getRoute().getTransitDays()))
                            .status(ShipmentStatus.INTRANSIT)
                            .build()
            );
        }

        log.info("Seeded shipments with carrier, rate card and freight cost");
    }

    private void seedDeliveryEvents() {
 
        if (deliveryEventRepository.count() != 0) {
            return;
        }
 
        List<Shipment> shipments = shipmentRepository.findAll();
 
        if (shipments.isEmpty()) {
            return;
        }
 
        deliveryEventRepository.save(
                DeliveryEvent.builder()
                        .shipment(shipments.get(0))
                        .eventType(EventType.PICKUP)
                        .locationId(1)
                        .notes("Shipment picked up from origin.")
                        .build());
 
        deliveryEventRepository.save(
                DeliveryEvent.builder()
                        .shipment(shipments.get(0))
                        .eventType(EventType.INTRANSIT)
                        .locationId(2)
                        .notes("Shipment in transit.")
                        .build());
    }
    private void seedPurchaseOrders() {
 
        if (purchaseOrderRepository.count() != 0) {
            return;
        }
 
        List<Supplier> suppliers = supplierRepository.findAll();
 
        if (suppliers.isEmpty()) {
            return;
        }
 
        purchaseOrderRepository.save(
                PurchaseOrder.builder()
                        .supplier(suppliers.get(0))
                        .warehouseId(1)
                        .lineItems("[{\"sku\":\"SKU-0001\",\"qty\":50}]")
                        .totalValue(new BigDecimal("15000.00"))
                        .orderDate(LocalDate.now())
                        .expectedDelivery(LocalDate.now().plusDays(7))
                        .status(POStatus.PLACED)
                        .build());
 
        purchaseOrderRepository.save(
                PurchaseOrder.builder()
                        .supplier(suppliers.get(1))
                        .warehouseId(2)
                        .lineItems("[{\"sku\":\"SKU-0002\",\"qty\":100}]")
                        .totalValue(new BigDecimal("22000.00"))
                        .orderDate(LocalDate.now())
                        .expectedDelivery(LocalDate.now().plusDays(5))
                        .status(POStatus.PLACED)
                        .build());
    }
    private void seedInboundReceipts() {
 
        if (inboundReceiptRepository.count() != 0) {
            return;
        }
 
        List<PurchaseOrder> orders = purchaseOrderRepository.findAll();
 
        if (orders.isEmpty()) {
            return;
        }
 
        inboundReceiptRepository.save(
                InboundReceipt.builder()
                        .supplierOrderId(orders.get(0).getPoId())
                        .warehouseId(1)
                        .receivedBy(3)
                        .receivedDate(LocalDate.now())
                        .status(ReceiptStatus.RECEIVED)
                        .build());
    }
    private void seedPickLists() {
 
        if (pickListRepository.count() != 0) {
            return;
        }
 
        List<FreightOrder> orders = freightOrderRepository.findAll();
 
        if (orders.isEmpty()) {
            return;
        }
 
        pickListRepository.save(
                PickList.builder()
                        .freightOrderId(orders.get(0).getFreightOrderId())
                        .warehouseId(1)
                        .assignedTo(3)
                        .status(PickListStatus.COMPLETED)
                        .createdDate(LocalDate.now())
                        .build());
 
        pickListRepository.save(
                PickList.builder()
                        .freightOrderId(orders.get(1).getFreightOrderId())
                        .warehouseId(2)
                        .assignedTo(3)
                        .status(PickListStatus.INPROGRESS)
                        .createdDate(LocalDate.now())
                        .build());
    }
    private void seedShipmentDocuments() {
 
        if (shipmentDocumentRepository.count() != 0) {
            return;
        }
 
        List<Shipment> shipments = shipmentRepository.findAll();
 
        if (shipments.isEmpty()) {
            return;
        }
 
        shipmentDocumentRepository.save(
                ShipmentDocument.builder()
                        .shipmentId(shipments.get(0).getShipmentId())
                        .documentType(DocumentType.BOL)
                        .filePath("/docs/bol-001.pdf")
                        .submittedDate(LocalDate.now())
                        .status(DocumentStatus.APPROVED)
                        .build());
 
        shipmentDocumentRepository.save(
                ShipmentDocument.builder()
                        .shipmentId(shipments.get(0).getShipmentId())
                        .documentType(DocumentType.COMMERCIALINVOICE)
                        .filePath("/docs/invoice-001.pdf")
                        .submittedDate(LocalDate.now())
                        .status(DocumentStatus.SUBMITTED)
                        .build());
    }

    private void seedComplianceFlags() {
 
        if (complianceFlagRepository.count() != 0) {
            return;
        }
 
        List<Shipment> shipments = shipmentRepository.findAll();
 
        if (shipments.isEmpty()) {
            return;
        }
 
        complianceFlagRepository.save(
                ComplianceFlag.builder()
                        .shipment(shipments.get(0))
                        .flagType("Missing Customs Declaration")
                        .severity(FlagSeverity.HIGH)
                        .status(FlagStatus.OPEN)
                        .build());
 
        if (shipments.size() > 1) {
            complianceFlagRepository.save(
                    ComplianceFlag.builder()
                            .shipment(shipments.get(1))
                            .flagType("Packaging Review")
                            .severity(FlagSeverity.LOW)
                            .status(FlagStatus.RESOLVED)
                            .build());
        }
 
        log.info("Seeded compliance flags");
    }
    private void seedLogisticsReports() {
 
        if (logisticsReportRepository.count() != 0) {
            return;
        }
 
        logisticsReportRepository.save(
                LogisticsReport.builder()
                        .scope("GLOBAL")
                        .metrics("""
                            {
                              "shipmentCount":25,
                              "onTimeRate":96.5,
                              "avgTransitDays":3.8,
                              "freightCost":125000,
                              "exceptionRate":2.5
                            }
                            """)
                        .build());
 
        logisticsReportRepository.save(
                LogisticsReport.builder()
                        .scope("CARRIER")
                        .metrics("""
                            {
                              "shipmentCount":10,
                              "onTimeRate":94.2
                            }
                            """)
                        .build());
    }

}