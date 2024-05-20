package com.krisnovak.springboot.demo.planttracker.entity;

/**
 * Exception that is thrown if an account provided to the database has one or more invalid fields
 */
public class InvalidAccountException extends RuntimeException{
    public InvalidAccountException(String message) {
        super(message);
    }

    public InvalidAccountException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidAccountException(Throwable cause) {
        super(cause);
    }
}
