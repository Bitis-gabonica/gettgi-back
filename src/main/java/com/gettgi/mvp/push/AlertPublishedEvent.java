package com.gettgi.mvp.push;

import com.gettgi.mvp.dto.telemetry.AlertNotificationDto;

public record AlertPublishedEvent(AlertNotificationDto alert) {
}

