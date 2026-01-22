package com.gettgi.mvp.config;

import com.gettgi.mvp.push.PushNotificationService;
import com.gettgi.mvp.push.impl.FcmPushNotificationService;
import com.gettgi.mvp.push.impl.NoopPushNotificationService;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.FileInputStream;

@Slf4j
@Configuration
@EnableConfigurationProperties(PushProperties.class)
public class PushConfiguration {

    private static final String FIREBASE_APP_NAME = "gettgi-push";

    @Bean
    @ConditionalOnProperty(prefix = "app.push.fcm", name = "enabled", havingValue = "true")
    public PushNotificationService fcmPushNotificationService(PushProperties pushProperties) {
        String credentialsPath = pushProperties.getFcm().getCredentialsPath();
        if (!StringUtils.hasText(credentialsPath)) {
            throw new IllegalStateException("app.push.fcm.credentials-path is required when FCM push is enabled");
        }

        FirebaseApp app = getOrCreateFirebaseApp(credentialsPath.trim());
        FirebaseMessaging messaging = FirebaseMessaging.getInstance(app);
        return new FcmPushNotificationService(messaging, pushProperties.getFcm().isDryRun());
    }

    @Bean
    @ConditionalOnMissingBean(PushNotificationService.class)
    public PushNotificationService noopPushNotificationService() {
        return new NoopPushNotificationService();
    }

    private FirebaseApp getOrCreateFirebaseApp(String credentialsPath) {
        try {
            return FirebaseApp.getInstance(FIREBASE_APP_NAME);
        } catch (IllegalStateException ignored) {
            // continue and initialize
        }

        try (FileInputStream serviceAccount = new FileInputStream(credentialsPath)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            return FirebaseApp.initializeApp(options, FIREBASE_APP_NAME);
        } catch (Exception ex) {
            log.error("Unable to initialize Firebase App for push notifications", ex);
            throw new IllegalStateException("Unable to initialize Firebase App for push notifications", ex);
        }
    }
}

