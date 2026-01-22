package com.gettgi.mvp.telemetry;

import com.gettgi.mvp.dto.telemetry.AlertNotificationDto;
import com.gettgi.mvp.dto.telemetry.RealtimePositionDto;

public interface RealtimeMessagingService {

    void publishPosition(String userTelephone, RealtimePositionDto position);

    void publishAlert(String userTelephone, AlertNotificationDto alert);
}
