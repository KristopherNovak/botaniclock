package com.krisnovak.springboot.demo.planttracker.entity;

/**
 * Exception that is thrown if a plant provided to the database has one or more invalid fields
 */
public class InvalidSessionException extends RuntimeException{
    public InvalidSessionException(String message) {
        super(message);
    }

    public InvalidSessionException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidSessionException(Throwable cause) {
        super(cause);
    }
}
