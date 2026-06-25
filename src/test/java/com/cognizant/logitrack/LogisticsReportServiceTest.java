package com.cognizant.logitrack;

import com.cognizant.logitrack.exception.ResourceNotFoundException;
import com.cognizant.logitrack.dto.LogisticsReportDTO;
import com.cognizant.logitrack.dto.ReportRequestDTO;
import com.cognizant.logitrack.entity.LogisticsReport;
import com.cognizant.logitrack.repository.LogisticsReportRepository;
import com.cognizant.logitrack.service.LogisticsReportService;
import com.cognizant.logitrack.serviceImplementation.LogisticsReportServiceImpl;
import com.cognizant.logitrack.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogisticsReportServiceTest {

    @Mock
    private LogisticsReportRepository reportRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private LogisticsReportServiceImpl reportService;

    @Test
    void generateReport_returnsReportWithMetrics() {
        when(auditLogRepository.findByTimestampBetween(any(), any())).thenReturn(Collections.emptyList());
        when(reportRepository.save(any(LogisticsReport.class))).thenAnswer(inv -> {
            LogisticsReport r = inv.getArgument(0);
            r.setReportId(1);
            return r;
        });

        ReportRequestDTO req = new ReportRequestDTO("GLOBAL", LocalDateTime.now().minusDays(7), LocalDateTime.now());
        LogisticsReportDTO result = reportService.generateReport(req);

        assertNotNull(result);
        assertEquals("GLOBAL", result.getScope());
        assertNotNull(result.getMetrics());
        assertTrue(result.getMetrics().contains("shipmentCount"));
    }

    @Test
    void getReportById_invalid_throwsResourceNotFoundException() {
        when(reportRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reportService.getReportById(99));
    }
}
