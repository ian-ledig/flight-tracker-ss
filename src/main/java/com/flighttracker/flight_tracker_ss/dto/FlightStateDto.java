package com.flighttracker.flight_tracker_ss.dto;

import lombok.Data;

@Data
public class FlightStateDto {
    private String callsign;
    private String originCountry;
    private String originAirport;
    private String destinationAirport;
}
