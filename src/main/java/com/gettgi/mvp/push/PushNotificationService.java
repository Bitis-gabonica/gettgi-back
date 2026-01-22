package com.gettgi.mvp.push;

import java.util.List;

public interface PushNotificationService {

    void send(PushMessage message, List<String> tokens);
}

