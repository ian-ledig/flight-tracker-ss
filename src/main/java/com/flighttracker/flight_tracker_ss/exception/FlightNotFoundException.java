package com.flighttracker.flight_tracker_ss.exception;

import com.flighttracker.flight_tracker_ss.constant.ResponseMessage;

public class FlightNotFoundException extends RuntimeException{

    public FlightNotFoundException(String callsign){
        super(ResponseMessage.FLIGHTS_NOT_FOUND.getMessage(callsign));
    }
}
