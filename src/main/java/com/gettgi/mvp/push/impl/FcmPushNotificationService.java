package com.gettgi.mvp.push.impl;

import com.gettgi.mvp.push.PushMessage;
import com.gettgi.mvp.push.PushNotificationService;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class FcmPushNotificationService implements PushNotificationService {

    private final FirebaseMessaging firebaseMessaging;
    private final boolean dryRun;

    @Override
    public void send(PushMessage message, List<String> tokens) {
        if (message == null || tokens == null || tokens.isEmpty()) {
            return;
        }

        Map<String, String> safeData = new HashMap<>();
        if (message.data() != null) {
            safeData.putAll(message.data());
        }
        safeData.values().removeIf(v -> v == null);

        MulticastMessage.Builder builder = MulticastMessage.builder()
                .addAllTokens(tokens)
                .putAllData(safeData);

        String title = message.title() != null ? message.title().trim() : "";
        String body = message.body() != null ? message.body().trim() : "";
        if (!title.isEmpty() || !body.isEmpty()) {
            builder.setNotification(Notification.builder().setTitle(title).setBody(body).build());
        }

        try {
            MulticastMessage multicastMessage = builder.build();
            BatchResponse response = firebaseMessaging.sendEachForMulticast(multicastMessage, dryRun);
            if (log.isDebugEnabled()) {
                log.debug("Push sent: success={} failure={} tokens={}", response.getSuccessCount(), response.getFailureCount(), tokens.size());
            }
        } catch (Exception ex) {
            log.warn("Failed to send push notification to {} token(s)", tokens.size(), ex);
        }
    }
}
