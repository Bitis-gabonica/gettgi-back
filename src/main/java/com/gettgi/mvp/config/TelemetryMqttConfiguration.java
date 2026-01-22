package com.gettgi.mvp.config;

import com.gettgi.mvp.telemetry.TelemetryMessageHandler;
import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.util.StringUtils;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(TelemetryMqttProperties.class)
@ConditionalOnProperty(prefix = "app.telemetry.mqtt", name = "broker-url")
public class TelemetryMqttConfiguration {

    private final TelemetryMqttProperties properties;

    @Bean
    public MqttConnectOptions telemetryMqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{properties.getBrokerUrl()});
        if (StringUtils.hasText(properties.getUsername())) {
            options.setUserName(properties.getUsername());
        }
        if (StringUtils.hasText(properties.getPassword())) {
            options.setPassword(properties.getPassword().toCharArray());
        }
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        return options;
    }

    @Bean
    public MqttPahoClientFactory telemetryMqttClientFactory(MqttConnectOptions telemetryMqttConnectOptions) {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(telemetryMqttConnectOptions);
        return factory;
    }

    @Bean
    public MessageChannel telemetryInboundChannel() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow telemetryMqttInboundFlow(MqttPahoClientFactory telemetryMqttClientFactory,
                                                    TelemetryMessageHandler telemetryMessageHandler,
                                                    MessageChannel telemetryInboundChannel) {
        String[] topics = properties.getTopics().isEmpty()
                ? new String[]{"collars/+/telemetry"}
                : properties.getTopics().toArray(String[]::new);
        var adapter = new MqttPahoMessageDrivenChannelAdapter(
                properties.getClientId(),
                telemetryMqttClientFactory,
                topics
        );
        adapter.setCompletionTimeout(properties.getCompletionTimeout().toMillis());
        DefaultPahoMessageConverter converter = new DefaultPahoMessageConverter();
        converter.setPayloadAsBytes(false);
        adapter.setConverter(converter);
        adapter.setQos(properties.getQos());
        adapter.setAutoStartup(properties.isAutoStartup());
        adapter.setBeanName("telemetryMqttInboundAdapter");
        adapter.setManualAcks(false);
        return IntegrationFlow.from(adapter)
                .channel(telemetryInboundChannel)
                .handle(telemetryMessageHandler)
                .get();
    }
}
