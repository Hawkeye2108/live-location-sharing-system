package com.org.livelocationsharing.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a viewer requests location but no snapshot exists in Redis yet
 * (host hasn't sent any update since the session was created).
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class LocationNotFoundException extends RuntimeException{
    public LocationNotFoundException(String sessionId){
        super("No location data available yet for session: " + sessionId);
    }
}
