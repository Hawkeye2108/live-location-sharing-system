package com.org.livelocationsharing.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a host tries to update location on an expired or terminated session.
 */
@ResponseStatus(HttpStatus.GONE)
public class SessionExpiredException extends RuntimeException{
    public SessionExpiredException(String sessionId){
        super("Session has expired or been terminated: " + sessionId);
    }
}
