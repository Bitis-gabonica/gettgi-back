package com.gettgi.mvp.telemetry.impl;

import com.gettgi.mvp.dto.telemetry.AlertNotificationDto;
import com.gettgi.mvp.dto.telemetry.RealtimePositionDto;
import com.gettgi.mvp.push.AlertPublishedEvent;
import com.gettgi.mvp.telemetry.RealtimeMessagingService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;

@Service
@RequiredArgsConstructor
public class RealtimeMessagingServiceImpl implements RealtimeMessagingService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publishPosition(String userTelephone, RealtimePositionDto position) {
        if (userTelephone == null || userTelephone.isBlank() || position == null) {
            return;
        }

        messagingTemplate.convertAndSendToUser(userTelephone, "/queue/animals", position);
        messagingTemplate.convertAndSendToUser(userTelephone, "/queue/animals/" + position.animalId(), position);
    }

    @Override
    public void publishAlert(String userTelephone, AlertNotificationDto alert) {
        if (userTelephone == null || userTelephone.isBlank() || alert == null) {
            return;
        }

        messagingTemplate.convertAndSendToUser(userTelephone, "/queue/alerts", alert);
        messagingTemplate.convertAndSendToUser(userTelephone, "/queue/animals/" + alert.animalId() + "/alerts", alert);
        eventPublisher.publishEvent(new AlertPublishedEvent(alert));
    }
}
