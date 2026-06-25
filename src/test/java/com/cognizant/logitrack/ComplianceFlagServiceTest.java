package com.cognizant.logitrack;

import com.cognizant.logitrack.dto.ComplianceFlagDTO;
import com.cognizant.logitrack.entity.ComplianceFlag;
import com.cognizant.logitrack.enums.FlagSeverity;
import com.cognizant.logitrack.enums.FlagStatus;
import com.cognizant.logitrack.repository.ComplianceFlagRepository;
import com.cognizant.logitrack.service.ComplianceFlagService;
import com.cognizant.logitrack.serviceImplementation.ComplianceFlagServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComplianceFlagServiceTest {

    @Mock
    private ComplianceFlagRepository flagRepository;

    @InjectMocks
    private ComplianceFlagServiceImpl flagService;

    @Test
    void raiseFlag_savedAsOpen() {
        when(flagRepository.save(any(ComplianceFlag.class))).thenAnswer(inv -> {
            ComplianceFlag f = inv.getArgument(0);
            f.setFlagId(1);
            return f;
        });

        ComplianceFlagDTO dto = ComplianceFlagDTO.builder().shipmentId(1).flagType("MISSING_DOC").severity(FlagSeverity.HIGH).build();
        ComplianceFlagDTO result = flagService.raiseFlag(dto);

        assertNotNull(result);
        assertEquals(FlagStatus.OPEN, result.getStatus());
    }

    @Test
    void resolveFlag_openToResolved() {
        ComplianceFlag flag = ComplianceFlag.builder().flagId(1).status(FlagStatus.OPEN).build();
        when(flagRepository.findById(1)).thenReturn(Optional.of(flag));
        when(flagRepository.save(any(ComplianceFlag.class))).thenAnswer(inv -> inv.getArgument(0));

        ComplianceFlagDTO result = flagService.resolveFlag(1);

        assertEquals(FlagStatus.RESOLVED, result.getStatus());
    }
}
