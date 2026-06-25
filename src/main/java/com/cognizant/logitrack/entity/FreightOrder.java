package com.cognizant.logitrack.entity;

import com.cognizant.logitrack.enums.FreightOrderStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "freight_orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FreightOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer freightOrderId;

    private Integer shipperId;

    private Integer originLocationId;

    private Integer destinationLocationId;

    @ManyToOne
    @JoinColumn(name = "RouteID")
    private Route route;

    private String cargoDescription;

    private BigDecimal weight;

    private BigDecimal volume;

    private LocalDate requiredDeliveryDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FreightOrderStatus status = FreightOrderStatus.DRAFT;
}