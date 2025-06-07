package com.flighttracker.flight_tracker_ss.constant;

import java.text.MessageFormat;

public enum ResponseMessage {
    FLIGHTS_NOT_FOUND("Flight(s) with input {0} not found");

    private final String message;

    ResponseMessage(String message){
        this.message = message;
    }

    public String getMessage(Object... args) {
        return MessageFormat.format(message, args);
    }
}
