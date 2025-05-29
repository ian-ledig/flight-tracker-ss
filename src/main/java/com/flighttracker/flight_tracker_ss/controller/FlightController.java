package com.flighttracker.flight_tracker_ss.controller;

import com.flighttracker.flight_tracker_ss.dto.FlightsDTO;
import com.flighttracker.flight_tracker_ss.exception.FlightNotFoundException;
import com.flighttracker.flight_tracker_ss.mapper.FlightMapper;
import com.flighttracker.flight_tracker_ss.service.AviationStackService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api")
public class FlightController {

    private final AviationStackService aviationStackService;
    private final FlightMapper flightMapper;

    public FlightController(AviationStackService aviationStackService, FlightMapper flightMapper) {
        this.aviationStackService = aviationStackService;
        this.flightMapper = flightMapper;
    }

    @GetMapping("/flights/{airlineIata}")
    public Mono<List<FlightsDTO>> getFlights(
            @PathVariable String airlineIata,
            @RequestParam(value = "flightNumber", required = false) String flightNumber
    ) {
        String input = flightNumber != null ? airlineIata + flightNumber : airlineIata;
        return aviationStackService.getFlights(input)
                .map(response -> response.getData().stream()
                        .map(flightMapper::toFlightsDTO)
                        .toList());
    }

    @ExceptionHandler(FlightNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<String> handleFlightNotFound(FlightNotFoundException ex) {
        return Mono.just(ex.getMessage());
    }
}
