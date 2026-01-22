package com.gettgi.mvp.telemetry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gettgi.mvp.dto.telemetry.TelemetryIngestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TelemetryMessageHandlerTest {

    @Mock
    private TelemetryIngestionService telemetryIngestionService;

    private TelemetryMessageHandler handler;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        handler = new TelemetryMessageHandler(objectMapper, telemetryIngestionService);
    }

    @Test
    void shouldExtractImeiFromTopicWhenMissingFromPayload() {
        String payload = """
                {
                  "timestamp": "2025-10-17T16:00:00Z",
                  "position": { "latitude": 14.706, "longitude": -17.467 }
                }
                """;

        Message<String> message = MessageBuilder.withPayload(payload)
                .setHeader(MqttHeaders.RECEIVED_TOPIC, "collars/123456789012345/telemetry")
                .build();

        handler.handleMessage(message);

        ArgumentCaptor<TelemetryIngestDto> captor = ArgumentCaptor.forClass(TelemetryIngestDto.class);
        verify(telemetryIngestionService).ingest(captor.capture());
        assertThat(captor.getValue().deviceImei()).isEqualTo("123456789012345");
    }

    @Test
    void shouldNotCallIngestWhenImeiIsMissingAndNoTopic() {
        String payload = """
                {
                  "timestamp": "2025-10-17T16:00:00Z",
                  "position": { "latitude": 14.706, "longitude": -17.467 }
                }
                """;

        Message<String> message = MessageBuilder.withPayload(payload).build();

        handler.handleMessage(message);

        verify(telemetryIngestionService, never()).ingest(org.mockito.ArgumentMatchers.any());
    }
}

