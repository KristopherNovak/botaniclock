package com.krisnovak.springboot.demo.planttracker.exception;

import com.krisnovak.springboot.demo.planttracker.rest.HTTPResponseBody;
import com.krisnovak.springboot.demo.planttracker.entity.InvalidAccountException;
import com.krisnovak.springboot.demo.planttracker.entity.InvalidPlantException;
import com.krisnovak.springboot.demo.planttracker.entity.InvalidSessionException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

/**
 * Class responsible for handling all REST-relevant exceptions
 */
@RestControllerAdvice
public class RestExceptionHandlerRespository{

    //Service Layer Exceptions

    /**
     * Function that returns a bad request HTTP message if an InvalidAccountException is thrown
     * @param e The InvalidAccountException
     * @return An HTTP message indicating a bad request message
     */
    @ExceptionHandler
    public ResponseEntity<HTTPResponseBody> handleInvalidAccountException(InvalidAccountException e){

        HTTPResponseBody httpResponseBody = HTTPResponseBody.newInstance(HttpStatus.BAD_REQUEST, "INVALID_ACCOUNT");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(httpResponseBody);
    }

    /**
     * Function that returns a not found HTTP message if an InvalidPlantException is thrown
     * @param e The InvalidPlantException
     * @return An HTTP message indicating a not found message
     */
    @ExceptionHandler
    public ResponseEntity<HTTPResponseBody> handleInvalidPlantException(InvalidPlantException e){

        HTTPResponseBody httpResponseBody = HTTPResponseBody.newInstance(HttpStatus.NOT_FOUND, "PLANT_NOT_FOUND");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(httpResponseBody);
    }

    /**
     * Function that returns a not found HTTP message if an InvalidSessionException is thrown
     * @param e The InvalidSessionException
     * @return An HTTP message indicating a forbidden message
     */
    @ExceptionHandler
    public ResponseEntity<HTTPResponseBody> handleInvalidSessionException(InvalidSessionException e){

        HTTPResponseBody httpResponseBody = HTTPResponseBody.newInstance(HttpStatus.FORBIDDEN, "INVALID_COOKIE");

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(httpResponseBody);
    }

    /**
     * Function that returns a bad request HTTP message if an IO Exception is thrown without being caught
     * @param e The IOException
     * @return An HTTP message indicating a bad request
     */
    @ExceptionHandler
    public ResponseEntity<HTTPResponseBody> handleIOException(IOException e){

        HTTPResponseBody httpResponseBody = HTTPResponseBody.newInstance(HttpStatus.BAD_REQUEST, "IO_EXCEPTION");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(httpResponseBody);
    }

    /**
     * Function that determines an HTTP message to send depending on the type of DataIntegrityViolationException
     * @param e The DataIntegrityViolationException e
     * @return An HTTP message corresponding to each type of DataIntegrityViolationException
     */
    @ExceptionHandler
    public ResponseEntity<HTTPResponseBody> handleDataIntegrityViolationException(DataIntegrityViolationException e){

        String errorMessage = e.getMessage();
        HTTPResponseBody httpResponseBody;

        //Triggered if one of the parameters provided to the database was too long for the database
        if(errorMessage.contains("ERROR: value too long for type")) {
            httpResponseBody = HTTPResponseBody.newInstance(HttpStatus.BAD_REQUEST, "TOO_LONG");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(httpResponseBody);
        }

        //Triggered if one of the parameters provided to the database was null when the corresponding
        //space in the database was non-nullable
        if(errorMessage.contains("ERROR: null value in column")) {
            httpResponseBody = HTTPResponseBody.newInstance(HttpStatus.BAD_REQUEST, "NULL_FIELD");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(httpResponseBody);
        }

        //Triggered if an empty email or empty password is provided to the database for an Account
        if(errorMessage.contains("empty_credential")){
            httpResponseBody = HTTPResponseBody.newInstance(HttpStatus.BAD_REQUEST, "EMPTY_FIELD");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(httpResponseBody);
        }

        //Provided if an Account already exists in the database
        if (errorMessage.contains("no_duplicate_account")) {
            httpResponseBody = HTTPResponseBody.newInstance(HttpStatus.BAD_REQUEST, "DUPLICATE_ACCOUNT");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(httpResponseBody);
        }

        throw new RuntimeException("Unaccounted database failure");
    }

}
