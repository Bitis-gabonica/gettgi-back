package com.gettgi.mvp.push;

import com.gettgi.mvp.dto.telemetry.AlertNotificationDto;
import com.gettgi.mvp.entity.Alerte;
import com.gettgi.mvp.entity.enums.TypeAlerte;
import com.gettgi.mvp.repository.AlerteRepository;
import com.gettgi.mvp.repository.PushTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertPushListener {

    private static final EnumSet<TypeAlerte> PUSH_WORTHY_TYPES = EnumSet.of(
            TypeAlerte.COLLAR_CUT,
            TypeAlerte.GEOFENCE_EXIT,
            TypeAlerte.COLLAR_TAMPERING,
            TypeAlerte.DEVICE_OFFLINE,
            TypeAlerte.SPEED_SUSPECT,
            TypeAlerte.DANGER,
            TypeAlerte.VOL
    );

    private final AlerteRepository alerteRepository;
    private final PushTokenRepository pushTokenRepository;
    private final PushNotificationService pushNotificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onAlertPublished(AlertPublishedEvent event) {
        if (event == null || event.alert() == null) {
            return;
        }

        AlertNotificationDto alert = event.alert();
        if (Boolean.TRUE.equals(alert.resolved())) {
            return;
        }
        if (alert.type() == null || !PUSH_WORTHY_TYPES.contains(alert.type())) {
            return;
        }

        UUID alertId = alert.alertId();
        if (alertId == null) {
            return;
        }

        Alerte persisted = alerteRepository.findById(alertId).orElse(null);
        if (persisted == null || persisted.getUser() == null || persisted.getUser().getId() == null) {
            log.debug("Push skipped: unable to resolve user for alertId={}", alertId);
            return;
        }

        UUID userId = persisted.getUser().getId();
        List<String> tokens = pushTokenRepository.findAllByUser_Id(userId).stream()
                .map(t -> t.getToken())
                .filter(t -> t != null && !t.isBlank())
                .distinct()
                .toList();
        if (tokens.isEmpty()) {
            log.debug("Push skipped: no registered tokens for userId={} alertId={}", userId, alertId);
            return;
        }

        PushMessage message = toPushMessage(alert);
        pushNotificationService.send(message, tokens);
    }

    private PushMessage toPushMessage(AlertNotificationDto alert) {
        String title = switch (alert.type()) {
            case COLLAR_CUT -> "Alerte vol";
            case COLLAR_TAMPERING -> "Suspicion de vol";
            case DEVICE_OFFLINE -> "Tracker hors ligne";
            case GEOFENCE_EXIT -> "Animal hors zone";
            case SPEED_SUSPECT -> "Vitesse suspecte";
            case VOL -> "Vol";
            case DANGER -> "Danger";
            default -> "Alerte";
        };

        Map<String, String> data = new HashMap<>();
        data.put("alertId", alert.alertId() != null ? alert.alertId().toString() : "");
        data.put("animalId", alert.animalId() != null ? alert.animalId().toString() : "");
        data.put("deviceId", alert.deviceId() != null ? alert.deviceId().toString() : "");
        data.put("type", alert.type() != null ? alert.type().name() : "");
        data.put("ts", alert.raisedAt() != null ? alert.raisedAt().toString() : "");

        String body = alert.message() != null ? alert.message() : "Nouvelle alerte";

        return new PushMessage(title, body, data);
    }
}
