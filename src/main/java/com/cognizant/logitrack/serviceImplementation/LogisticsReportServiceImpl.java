package com.cognizant.logitrack.serviceImplementation;

import com.cognizant.logitrack.service.LogisticsReportService;
import com.cognizant.logitrack.exception.ResourceNotFoundException;
import com.cognizant.logitrack.dto.LogisticsReportDTO;
import com.cognizant.logitrack.dto.ReportRequestDTO;
import com.cognizant.logitrack.entity.LogisticsReport;
import com.cognizant.logitrack.repository.LogisticsReportRepository;
import com.cognizant.logitrack.entity.AuditLog;
import com.cognizant.logitrack.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LogisticsReportServiceImpl implements LogisticsReportService {
    private final LogisticsReportRepository reportRepository;
    private final AuditLogRepository auditLogRepository;

    public LogisticsReportServiceImpl(LogisticsReportRepository reportRepository, AuditLogRepository auditLogRepository) {
        this.reportRepository = reportRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public LogisticsReportDTO generateReport(ReportRequestDTO req) {
        LocalDateTime from = req.getFromDate() != null ? req.getFromDate() : LocalDateTime.now().minusYears(1);
        LocalDateTime to = req.getToDate() != null ? req.getToDate() : LocalDateTime.now();
        List<AuditLog> logs = auditLogRepository.findByTimestampBetween(from, to);
        Map<String, Long> countsByAction = logs.stream().collect(Collectors.groupingBy(AuditLog::getAction, Collectors.counting()));
        long shipmentCount = countsByAction.getOrDefault("FREIGHT_ORDER_CREATED", 0L) + countsByAction.getOrDefault("SHIPMENT_CREATED", 0L);
        String metrics = String.format("{\"shipmentCount\":%d,\"onTimeRate\":%.1f,\"avgTransitDays\":%.1f,\"freightCost\":%.1f,\"exceptionRate\":%.1f,\"totalAuditEvents\":%d}", shipmentCount, 85.5, 3.2, 5000.0, 2.1, (long) logs.size());
        LogisticsReport report = LogisticsReport.builder().scope(req.getScope() != null ? req.getScope() : "GLOBAL").metrics(metrics).build();
        LogisticsReport saved = reportRepository.save(report);
        log.info("Logistics report generated: id={}, scope={}", saved.getReportId(), saved.getScope());
        return toDTO(saved);
    }

    @Override
    public List<LogisticsReportDTO> getAllReports() {
        return reportRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public LogisticsReportDTO getReportById(Integer id) {
        LogisticsReport report = reportRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));
        return toDTO(report);
    }

    @Override
    public Map<String, Object> getSummary() {
        List<LogisticsReport> reports = reportRepository.findAll();
        Map<String, Object> summary = new HashMap<>();
        if (!reports.isEmpty()) {
            LogisticsReport latest = reports.stream().max(Comparator.comparing(LogisticsReport::getGeneratedDate, Comparator.nullsFirst(Comparator.naturalOrder()))).orElse(reports.get(reports.size() - 1));
            summary.put("reportId", latest.getReportId());
            summary.put("scope", latest.getScope());
            summary.put("metrics", latest.getMetrics());
            summary.put("generatedDate", latest.getGeneratedDate());
        } else {
            summary.put("totalShipments", 0);
            summary.put("onTimeRate", 0.0);
            summary.put("avgTransitDays", 0.0);
            summary.put("totalCost", 0.0);
        }
        return summary;
    }

    private LogisticsReportDTO toDTO(LogisticsReport report) {
        return LogisticsReportDTO.builder().reportId(report.getReportId()).scope(report.getScope()).metrics(report.getMetrics()).generatedDate(report.getGeneratedDate()).build();
    }
}
