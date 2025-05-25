package com.flighttracker.flight_tracker_ss.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient adsbdbWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.adsbdb.com/v0")
                .codecs(configurer -> {
                    ClientCodecConfigurer.ClientDefaultCodecs codecs = configurer.defaultCodecs();
                    codecs.maxInMemorySize(1024 * 1024);
                })
                .build();
    }
}
