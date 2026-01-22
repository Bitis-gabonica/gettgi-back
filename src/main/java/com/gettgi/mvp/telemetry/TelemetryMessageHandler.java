package com.gettgi.mvp.telemetry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gettgi.mvp.dto.telemetry.TelemetryIngestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelemetryMessageHandler implements MessageHandler {

    private static final Pattern IMEI_PATTERN = Pattern.compile("\\d{15}");

    private final ObjectMapper objectMapper;
    private final TelemetryIngestionService telemetryIngestionService;

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        String topic = resolveTopic(message);
        Object rawPayload = message.getPayload();
        String payloadAsString;
        if (rawPayload instanceof byte[] bytes) {
            payloadAsString = new String(bytes, StandardCharsets.UTF_8);
        } else {
            payloadAsString = String.valueOf(rawPayload);
        }
        try {
            JsonNode root = objectMapper.readTree(payloadAsString);
            if (!(root instanceof ObjectNode objectNode)) {
                log.warn("Telemetry dropped: payload is not a JSON object (topic={})", topic);
                return;
            }

            enrichDeviceImei(objectNode, topic);

            JsonNode imeiNode = objectNode.get("deviceImei");
            String imei = imeiNode != null ? imeiNode.asText(null) : null;
            if (imei == null || imei.isBlank()) {
                log.warn("Telemetry dropped: missing deviceImei (topic={})", topic);
                return;
            }

            TelemetryIngestDto dto = objectMapper.treeToValue(objectNode, TelemetryIngestDto.class);
            telemetryIngestionService.ingest(dto);
        } catch (Exception ex) {
            log.error("Failed to process telemetry payload (topic={}): {}", topic, payloadAsString, ex);
        }
    }

    private String resolveTopic(Message<?> message) {
        Object header = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
        if (header instanceof String topic && !topic.isBlank()) {
            return topic;
        }
        return null;
    }

    private void enrichDeviceImei(ObjectNode payload, String topic) {
        String extracted = extractImeiFromTopic(topic);
        if (extracted == null) {
            return;
        }

        JsonNode providedNode = payload.get("deviceImei");
        String provided = providedNode != null ? providedNode.asText(null) : null;
        if (provided == null || provided.isBlank()) {
            payload.put("deviceImei", extracted);
            return;
        }

        if (!provided.equals(extracted) && log.isWarnEnabled()) {
            log.warn("Telemetry payload deviceImei={} does not match topic imei={} (topic={})", provided, extracted, topic);
        }
    }

    private String extractImeiFromTopic(String topic) {
        if (topic == null || topic.isBlank()) {
            return null;
        }
        String[] segments = topic.split("/");
        if (segments.length < 3) {
            return null;
        }

        for (int index = 0; index <= segments.length - 3; index++) {
            if (!"collars".equals(segments[index])) {
                continue;
            }
            if (!"telemetry".equals(segments[index + 2])) {
                continue;
            }
            String candidate = segments[index + 1];
            if (candidate != null && IMEI_PATTERN.matcher(candidate).matches()) {
                return candidate;
            }
        }

        return null;
    }
}
