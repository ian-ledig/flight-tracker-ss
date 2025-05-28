package com.flighttracker.flight_tracker_ss.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${aviationstack.url}")
    private String aviationStackUrl;

    @Bean
    public WebClient aviationStackWebClient() {
        return WebClient.builder()
                .baseUrl(aviationStackUrl)
                .codecs(configurer -> {
                    ClientCodecConfigurer.ClientDefaultCodecs codecs = configurer.defaultCodecs();
                    codecs.maxInMemorySize(1024 * 1024);
                })
                .build();
    }
}
