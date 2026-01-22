package com.gettgi.mvp.push.impl;

import com.gettgi.mvp.push.PushMessage;
import com.gettgi.mvp.push.PushNotificationService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class NoopPushNotificationService implements PushNotificationService {

    @Override
    public void send(PushMessage message, List<String> tokens) {
        if (message == null || tokens == null || tokens.isEmpty()) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Push disabled: would send title='{}' to {} device(s)", message.title(), tokens.size());
        }
    }
}

