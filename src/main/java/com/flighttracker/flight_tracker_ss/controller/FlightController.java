package com.flighttracker.flight_tracker_ss.controller;

import com.flighttracker.flight_tracker_ss.dto.AviationStackResponse;
import com.flighttracker.flight_tracker_ss.exception.FlightNotFoundException;
import com.flighttracker.flight_tracker_ss.service.AviationStackService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class FlightController {

    private final AviationStackService aviationStackService;

    public FlightController(AviationStackService aviationStackService) {
        this.aviationStackService = aviationStackService;
    }

    @GetMapping("/flights/{airlineCode}")
    public Mono<AviationStackResponse> getFlightByAirline(
            @PathVariable String airlineCode,
            @RequestParam(value = "flightNumber", required = false) String flightNumber
    ) {
        String input = flightNumber != null ? airlineCode + flightNumber : airlineCode;
        return aviationStackService.getFlight(input);
    }

    @ExceptionHandler(FlightNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<String> handleFlightNotFound(FlightNotFoundException ex) {
        return Mono.just(ex.getMessage());
    }
}
