package com.org.livelocationsharing.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a sessionId does not exist in the database.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class SessionNotFoundException extends RuntimeException{
    public SessionNotFoundException(String sessionId){
        super("Session not found: " + sessionId);
    }
}
