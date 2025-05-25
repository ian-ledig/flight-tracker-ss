package com.flighttracker.flight_tracker_ss.service;

import com.flighttracker.flight_tracker_ss.dto.AdsbdbResponse;
import com.flighttracker.flight_tracker_ss.dto.FlightStateDto;
import com.flighttracker.flight_tracker_ss.exception.FlightNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class FlightService {

    private final WebClient adsbdbWebClient;

    public FlightService(WebClient adsbdbWebClient) {
        this.adsbdbWebClient = adsbdbWebClient;
    }

    public Mono<FlightStateDto> getFlightByCallsign(String callsign) {
        return adsbdbWebClient.get()
                .uri("/callsign/{callsign}", callsign)
                .retrieve()
                .bodyToMono(AdsbdbResponse.class)
                .flatMap(response -> {
                    if (response.getResponse() == null || response.getResponse().getFlightRoute() == null) {
                        return Mono.error(new FlightNotFoundException(callsign));
                    }
                    AdsbdbResponse.FlightRoute flightRoute = response.getResponse().getFlightRoute();
                    FlightStateDto dto = new FlightStateDto();
                    dto.setCallsign(flightRoute.getCallsignIata());
                    dto.setOriginCountry(flightRoute.getAirline() != null ? flightRoute.getAirline().getCountry() : null);
                    dto.setOriginAirport(flightRoute.getOrigin() != null ? flightRoute.getOrigin().getIataCode() : null);
                    dto.setDestinationAirport(flightRoute.getDestination() != null ? flightRoute.getDestination().getIataCode() : null);
                    return Mono.just(dto);
                })
                .onErrorResume(e -> Mono.error(new FlightNotFoundException(callsign)));
    }
}
