package com.flighttracker.flight_tracker_ss.controller;

import com.flighttracker.flight_tracker_ss.dto.FlightStateDto;
import com.flighttracker.flight_tracker_ss.exception.FlightNotFoundException;
import com.flighttracker.flight_tracker_ss.service.FlightService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class FlightController {

    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @GetMapping("/flights/{callsign}")
    public Mono<FlightStateDto> getFlightByCallsign(@PathVariable String callsign) {
        return flightService.getFlightByCallsign(callsign);
    }

    @ExceptionHandler(FlightNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<String> handleFlightNotFound(FlightNotFoundException ex) {
        return Mono.just(ex.getMessage());
    }
}
