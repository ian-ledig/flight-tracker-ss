package com.flighttracker.flight_tracker_ss.service;

import com.flighttracker.flight_tracker_ss.constant.ResponseMessage;
import com.flighttracker.flight_tracker_ss.dto.AdsbdbResponse;
import com.flighttracker.flight_tracker_ss.dto.FlightStateDto;
import com.flighttracker.flight_tracker_ss.exception.FlightNotFoundException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

@Service
public class FlightService {

    private static final Logger logger = LoggerFactory.getLogger(FlightService.class);
    private final Cache<String, FlightStateDto> flightCache;
    private final WebClient adsbdbWebClient;

    public FlightService(WebClient adsbdbWebClient) {
        this.adsbdbWebClient = adsbdbWebClient;
        this.flightCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .maximumSize(100)
                .build();
    }

    public Mono<FlightStateDto> getFlightByCallsign(String callsign) {
        FlightStateDto cached = flightCache.getIfPresent(callsign);
        if (cached != null) {
            logger.info("Returning cached flight data for callsign: {}", callsign);
            return Mono.just(cached);
        }
        return adsbdbWebClient.get()
                .uri("/callsign/{callsign}", callsign)
                .retrieve()
                .bodyToMono(AdsbdbResponse.class)
                .flatMap(response -> {
                    if (response.getResponse() == null || response.getResponse().getFlightRoute() == null) {
                        logger.warn(ResponseMessage.FLIGHT_NOT_FOUND.getMessage(callsign));
                        return Mono.error(new FlightNotFoundException(callsign));
                    }
                    FlightStateDto dto = getFlightStateDto(response);
                    flightCache.put(callsign, dto);
                    return Mono.just(dto);
                })
                .onErrorResume(e -> {
                    logger.error("Error fetching ADSBdb data for callsign {}: {}", callsign, e.getMessage());
                    return Mono.error(new FlightNotFoundException(callsign));
                });
    }

    private static FlightStateDto getFlightStateDto(AdsbdbResponse response) {
        AdsbdbResponse.FlightRoute flightRoute = response.getResponse().getFlightRoute();
        FlightStateDto dto = new FlightStateDto();
        dto.setCallsign(flightRoute.getCallsignIata());
        dto.setOriginCountry(flightRoute.getAirline() != null ? flightRoute.getAirline().getCountry() : null);
        dto.setOriginAirport(flightRoute.getOrigin() != null ? flightRoute.getOrigin().getIataCode() : null);
        dto.setDestinationAirport(flightRoute.getDestination() != null ? flightRoute.getDestination().getIataCode() : null);
        return dto;
    }
}
