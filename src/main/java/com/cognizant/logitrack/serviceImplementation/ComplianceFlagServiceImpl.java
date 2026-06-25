package com.cognizant.logitrack.serviceImplementation;
 
import com.cognizant.logitrack.service.ComplianceFlagService;
import com.cognizant.logitrack.exception.BadRequestException;
import com.cognizant.logitrack.exception.ResourceNotFoundException;
import com.cognizant.logitrack.dto.ComplianceFlagDTO;
import com.cognizant.logitrack.entity.ComplianceFlag;
import com.cognizant.logitrack.entity.Shipment;
import com.cognizant.logitrack.enums.FlagStatus;
import com.cognizant.logitrack.repository.ComplianceFlagRepository;
import com.cognizant.logitrack.repository.ShipmentRepository;
 
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.stream.Collectors;
 
@Service
@Slf4j
public class ComplianceFlagServiceImpl implements ComplianceFlagService {
    private final ComplianceFlagRepository flagRepository;
    private final ShipmentRepository shipmentRepository;
 
    public ComplianceFlagServiceImpl(ComplianceFlagRepository flagRepository, ShipmentRepository shipmentRepository) {
        this.flagRepository = flagRepository;
        this.shipmentRepository = shipmentRepository;
    }
 
    @Override
    public ComplianceFlagDTO raiseFlag(ComplianceFlagDTO dto) {
    	Shipment shipment = shipmentRepository.findById(dto.getShipmentId()).orElseThrow(() -> new BadRequestException("Shipment not found: " + dto.getShipmentId()));
        ComplianceFlag flag = ComplianceFlag.builder().shipment(shipment).flagType(dto.getFlagType()).severity(dto.getSeverity()).status(FlagStatus.OPEN).build();
        ComplianceFlag saved = flagRepository.save(flag);
        log.info("Compliance flag raised: id={}, shipmentId={}", saved.getFlagId(), saved.getShipment());
        return toDTO(saved);
    }
 
    @Override
    public ComplianceFlagDTO resolveFlag(Integer id) {
        ComplianceFlag flag = findEntity(id);
        flag.setStatus(FlagStatus.RESOLVED);
        log.info("Compliance flag {} resolved", id);
        return toDTO(flagRepository.save(flag));
    }
 
    @Override
    public List<ComplianceFlagDTO> getFlagsByShipment(Integer shipmentId) {
        return flagRepository.findByShipment_ShipmentId(shipmentId).stream().map(this::toDTO).collect(Collectors.toList());
    }
 
    @Override
    public List<ComplianceFlagDTO> getOpenFlags() {
        return flagRepository.findByStatus(FlagStatus.OPEN).stream().map(this::toDTO).collect(Collectors.toList());
    }
    
	@Override
	public List<ComplianceFlagDTO> getResolvedFlags() {
		return flagRepository.findByStatus(FlagStatus.RESOLVED).stream().map(this::toDTO).collect(Collectors.toList());
	}
 
    @Override
    public ComplianceFlagDTO getById(Integer id) {
        return toDTO(findEntity(id));
    }
 
    private ComplianceFlag findEntity(Integer id) {
        return flagRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Compliance flag not found with id: " + id));
    }
 
    private ComplianceFlagDTO toDTO(ComplianceFlag f) {
        return ComplianceFlagDTO.builder().flagId(f.getFlagId()).shipmentId(f.getShipment().getShipmentId()).flagType(f.getFlagType()).severity(f.getSeverity()).raisedDate(f.getRaisedDate()).status(f.getStatus()).build();
    }
 
 
}