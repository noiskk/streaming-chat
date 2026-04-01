package com.chat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
            .codecs(ClientCodecConfigurer::defaultCodecs)
            .build();

        strategies.messageWriters(); // trigger initialization

        return WebClient.builder()
            .exchangeStrategies(ExchangeStrategies.builder()
                .codecs(config -> config.defaultCodecs()
                    .maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build())
            .build();
    }
}
