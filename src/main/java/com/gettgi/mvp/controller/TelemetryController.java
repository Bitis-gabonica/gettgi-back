package com.gettgi.mvp.controller;

import com.gettgi.mvp.controller.validation.PaginationValidator;
import com.gettgi.mvp.dto.telemetry.AlertNotificationDto;
import com.gettgi.mvp.dto.telemetry.RealtimePositionDto;
import com.gettgi.mvp.dto.telemetry.TelemetryPointDto;
import com.gettgi.mvp.telemetry.TelemetryQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/telemetry")
@RequiredArgsConstructor
public class TelemetryController {

    private final TelemetryQueryService telemetryQueryService;

    @GetMapping("/animals/{animalId}/latest")
    public ResponseEntity<RealtimePositionDto> getLatestPosition(
            @PathVariable UUID animalId,
            @AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
        String telephone = principal.getUsername();
        RealtimePositionDto dto = telemetryQueryService.getLatestPosition(animalId, telephone);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/animals/{animalId}/history")
    public ResponseEntity<Page<TelemetryPointDto>> getHistory(
            @PathVariable UUID animalId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }

        validateDateRange(start, end);
        int[] validated = PaginationValidator.validateAndNormalize(page, size);

        Instant effectiveEnd = end != null ? end : Instant.now();
        Instant effectiveStart = start != null ? start : effectiveEnd.minus(1, ChronoUnit.DAYS);

        Pageable pageable = PageRequest.of(validated[0], validated[1]);
        String telephone = principal.getUsername();

        Page<TelemetryPointDto> history = telemetryQueryService.getHistory(animalId, telephone, effectiveStart, effectiveEnd, pageable);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/animals/{animalId}/alerts/active")
    public ResponseEntity<List<AlertNotificationDto>> getActiveAlerts(
            @PathVariable UUID animalId,
            @AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
        String telephone = principal.getUsername();
        List<AlertNotificationDto> alerts = telemetryQueryService.getActiveAlerts(animalId, telephone);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/animals/{animalId}/alerts/history")
    public ResponseEntity<Page<AlertNotificationDto>> getAlertHistory(
            @PathVariable UUID animalId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }

        validateDateRange(start, end);
        int[] validated = PaginationValidator.validateAndNormalize(page, size);

        Instant effectiveEnd = end != null ? end : Instant.now();
        Instant effectiveStart = start != null ? start : effectiveEnd.minus(7, ChronoUnit.DAYS);

        Pageable pageable = PageRequest.of(validated[0], validated[1]);
        String telephone = principal.getUsername();

        Page<AlertNotificationDto> history = telemetryQueryService.getAlertHistory(animalId, telephone, effectiveStart, effectiveEnd, pageable);
        return ResponseEntity.ok(history);
    }

    private void validateDateRange(Instant start, Instant end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Start date must be before or equal to end date"
            );
        }
        if (start != null && start.isAfter(Instant.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Start date cannot be in the future"
            );
        }
        if (end != null && end.isAfter(Instant.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "End date cannot be in the future"
            );
        }
    }
}
