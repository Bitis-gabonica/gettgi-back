package com.gettgi.mvp.controller;

import com.gettgi.mvp.dto.request.ManualAlertCreateRequestDto;
import com.gettgi.mvp.dto.telemetry.AlertNotificationDto;
import com.gettgi.mvp.entity.Alerte;
import com.gettgi.mvp.entity.Animal;
import com.gettgi.mvp.repository.AlerteRepository;
import com.gettgi.mvp.repository.AnimalRepository;
import com.gettgi.mvp.repository.UserRepository;
import com.gettgi.mvp.telemetry.RealtimeMessagingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertsController {

    private final AnimalRepository animalRepository;
    private final UserRepository userRepository;
    private final AlerteRepository alerteRepository;
    private final RealtimeMessagingService realtimeMessagingService;

    @PostMapping
    public ResponseEntity<AlertNotificationDto> createManualAlert(
            @Valid @RequestBody ManualAlertCreateRequestDto request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String telephone = principal.getUsername();
        Animal animal = animalRepository.findByIdAndUserTelephone(request.animalId(), telephone)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Animal not found"));

        Alerte alert = new Alerte();
        alert.setTypeAlerte(request.type());
        alert.setTs(Instant.now());
        alert.setResolved(false);
        alert.setResolvedAt(null);
        alert.setUser(animal.getUser());
        alert.setAnimal(animal);
        alert.setDevice(animal.getDevice());
        alert.setMessage(defaultMessage(animal, request.message(), request.type().name()));

        Alerte saved = alerteRepository.save(alert);
        AlertNotificationDto dto = toDto(saved);
        realtimeMessagingService.publishAlert(telephone, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/active")
    public ResponseEntity<List<AlertNotificationDto>> getUserActiveAlerts(
            @AuthenticationPrincipal UserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String telephone = principal.getUsername();
        UUID userId = userRepository.findByTelephone(telephone)
                .map(u -> u.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Pageable pageable = PageRequest.of(0, 200, Sort.by(Sort.Direction.DESC, "ts"));

        return ResponseEntity.ok(
                alerteRepository.findAllByUserIdAndResolved(userId, false, pageable)
                        .getContent()
                        .stream()
                        .map(this::toDto)
                        .toList()
        );
    }

    private AlertNotificationDto toDto(Alerte alert) {
        UUID deviceId = alert.getDevice() != null ? alert.getDevice().getId() : null;
        return new AlertNotificationDto(
                alert.getId(),
                alert.getAnimal().getId(),
                deviceId,
                alert.getTypeAlerte(),
                alert.getMessage(),
                alert.getTs(),
                alert.isResolved(),
                alert.getResolvedAt()
        );
    }

    private String defaultMessage(Animal animal, String requestedMessage, String typeLabel) {
        String msg = requestedMessage != null ? requestedMessage.trim() : "";
        if (!msg.isEmpty()) return msg;

        String animalLabel = animal != null ? Objects.requireNonNullElse(animal.getNom(), "animal") : "animal";
        return "Alerte manuelle (%s) pour %s".formatted(typeLabel, animalLabel);
    }
}
