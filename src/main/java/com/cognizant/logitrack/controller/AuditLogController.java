package com.cognizant.logitrack.controller;

import com.cognizant.logitrack.dto.AuditLogDTO;

import com.cognizant.logitrack.service.AuditLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@CrossOrigin(origins = "http://localhost:3000")

public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<?> getLogs(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            Pageable pageable) {

        if (userId != null) {
            return ResponseEntity.ok(auditLogService.getByUserId(userId));
        }
        if (action != null) {
            return ResponseEntity.ok(auditLogService.getByAction(action));
        }
        if (from != null && to != null) {
            return ResponseEntity.ok(auditLogService.getByDateRange(from, to));
        }
        Page<AuditLogDTO> page = auditLogService.getAllLogs(pageable);
        return ResponseEntity.ok(page);
    }

    @PostMapping
    public ResponseEntity<AuditLogDTO> logAction(@RequestBody AuditLogDTO dto) {
        AuditLogDTO created = auditLogService.logAction(
                dto.getUserId(), dto.getAction(), dto.getEntityType());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
