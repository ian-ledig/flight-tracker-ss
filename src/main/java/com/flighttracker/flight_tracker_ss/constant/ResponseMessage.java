package com.flighttracker.flight_tracker_ss.constant;

import java.text.MessageFormat;

public enum ResponseMessage {
    FLIGHT_NOT_FOUND("Flight with callsign {0} not found");

    private final String message;

    ResponseMessage(String message){
        this.message = message;
    }

    public String getMessage(Object... args) {
        return MessageFormat.format(message, args);
    }
}
