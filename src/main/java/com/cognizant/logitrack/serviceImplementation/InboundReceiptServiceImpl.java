package com.cognizant.logitrack.serviceImplementation;

import com.cognizant.logitrack.service.InboundReceiptService;
import com.cognizant.logitrack.exception.ResourceNotFoundException;
import com.cognizant.logitrack.dto.InboundReceiptDTO;
import com.cognizant.logitrack.entity.InboundReceipt;
import com.cognizant.logitrack.enums.ReceiptStatus;
import com.cognizant.logitrack.repository.InboundReceiptRepository;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InboundReceiptServiceImpl implements InboundReceiptService {
    private final InboundReceiptRepository inboundReceiptRepository;

    public InboundReceiptServiceImpl(InboundReceiptRepository inboundReceiptRepository) {
        this.inboundReceiptRepository = inboundReceiptRepository;
    }

    @Override
    public InboundReceiptDTO createReceipt(InboundReceiptDTO dto) {
        InboundReceipt receipt = InboundReceipt.builder().supplierOrderId(dto.getSupplierOrderId()).warehouseId(dto.getWarehouseId()).receivedDate(dto.getReceivedDate()).receivedBy(dto.getReceivedBy()).status(ReceiptStatus.PENDING).build();
        InboundReceipt saved = inboundReceiptRepository.save(receipt);
        log.info("Inbound receipt created: id={}", saved.getReceiptId());
        return toDTO(saved);
    }

    @Override
    public List<InboundReceiptDTO> getByWarehouse(Integer warehouseId) {
        return inboundReceiptRepository.findByWarehouseId(warehouseId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public InboundReceiptDTO updateStatus(Integer id, ReceiptStatus status) {
        InboundReceipt receipt = inboundReceiptRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Inbound receipt not found with id: " + id));
        receipt.setStatus(status);
        return toDTO(inboundReceiptRepository.save(receipt));
    }

    private InboundReceiptDTO toDTO(InboundReceipt r) {
        return InboundReceiptDTO.builder().receiptId(r.getReceiptId()).supplierOrderId(r.getSupplierOrderId()).warehouseId(r.getWarehouseId()).receivedDate(r.getReceivedDate()).receivedBy(r.getReceivedBy()).status(r.getStatus()).build();
    }
}
