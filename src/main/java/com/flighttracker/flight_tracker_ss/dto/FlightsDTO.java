package com.flighttracker.flight_tracker_ss.dto;

import lombok.Data;

@Data
public class FlightsDTO {
    private String airlineIata;
    private String flightNumber;
    private String depIata;
    private String arrIata;
    private long duration;
    private String depScheduled;
    private String arrScheduled;
    private String depEstimated;
    private String arrEstimated;
    private String aircraftIata;
    private String status;
}
