package com.krisnovak.springboot.demo.planttracker.entity;

/**
 * Exception that is thrown if a plant provided to the database has one or more invalid fields
 */
public class InvalidPlantException extends RuntimeException{

    public InvalidPlantException(String message) {
        super(message);
    }

    public InvalidPlantException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidPlantException(Throwable cause) {
        super(cause);
    }
}
